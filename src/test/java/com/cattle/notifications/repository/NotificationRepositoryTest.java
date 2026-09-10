package com.cattle.notifications.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.notifications.entity.NotificationItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para NotificationRepository.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class NotificationRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Mock
    private DynamoDbTable<NotificationItem> table;

    @Mock
    private PageIterable<NotificationItem> pageIterable;

    private NotificationRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new NotificationRepository(lambdaContext, enhancedClient, dynamoDbClient);
    }

    private NotificationItem notif(String id) {
        NotificationItem item = new NotificationItem();
        item.setPk(NotificationItem.partitionKey("001"));
        item.setSk(NotificationItem.sortKey(id));
        item.setNotificationId(id);
        item.setFarmId("001");
        return item;
    }

    @SuppressWarnings("unchecked")
    private void stubQuery(List<NotificationItem> items) {
        when(table.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        SdkIterable<NotificationItem> iterable = items::iterator;
        when(pageIterable.items()).thenReturn(iterable);
    }

    // ==================== save ====================

    @Test
    void save_delegatesToTable() {
        NotificationItem item = notif("n1");
        repository.save(item);
        verify(table).putItem(item);
    }

    @Test
    void save_resourceNotFound_wrapsWithContext() {
        NotificationItem item = notif("n1");
        doThrow(ResourceNotFoundException.builder().message("m").build()).when(table).putItem(item);

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.save(item));
        assertTrue(ex.getMessage().contains("Notifications table not found while saving notification"));
        assertTrue(ex.getMessage().contains("table=Notifications"));
        assertTrue(ex.getMessage().contains("pk=FARM#001"));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), contains("saving notification"), any());
    }

    @Test
    void save_dynamoDbException_wraps() {
        NotificationItem item = notif("n1");
        doThrow(DynamoDbException.builder().message("m").build()).when(table).putItem(item);
        assertTrue(assertThrows(RepositoryException.class, () -> repository.save(item))
                .getMessage().contains("DynamoDB error saving notification"));
    }

    @Test
    void save_unexpectedException_wraps() {
        NotificationItem item = notif("n1");
        doThrow(new RuntimeException("m")).when(table).putItem(item);
        assertTrue(assertThrows(RepositoryException.class, () -> repository.save(item))
                .getMessage().contains("Unexpected error saving notification"));
    }

    // ==================== tryClaimDedupe ====================

    @Test
    void tryClaimDedupe_firstClaim_writesConditionalSentinelAndReturnsTrue() {
        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());

        boolean claimed = repository.tryClaimDedupe("001", "milk-am-2026-01-01", 1_800_000_000L);

        assertTrue(claimed);
        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());
        PutItemRequest req = captor.getValue();
        assertEquals("Notifications", req.tableName());
        assertEquals("attribute_not_exists(pk)", req.conditionExpression());
        assertEquals("FARM#001", req.item().get("pk").s());
        assertEquals("DEDUPE#milk-am-2026-01-01", req.item().get("sk").s());
        assertEquals("milk-am-2026-01-01", req.item().get("dedupeKey").s());
        assertEquals("1800000000", req.item().get("ttl").n());
    }

    @Test
    void tryClaimDedupe_alreadyClaimed_returnsFalse() {
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().message("exists").build());

        assertFalse(repository.tryClaimDedupe("001", "k", 1L));
    }

    @Test
    void tryClaimDedupe_dynamoDbException_wraps() {
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(DynamoDbException.builder().message("m").build());

        assertTrue(assertThrows(RepositoryException.class, () -> repository.tryClaimDedupe("001", "k", 1L))
                .getMessage().contains("DynamoDB error claiming dedupe key"));
    }

    @Test
    void tryClaimDedupe_unexpectedException_wraps() {
        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenThrow(new RuntimeException("m"));

        assertTrue(assertThrows(RepositoryException.class, () -> repository.tryClaimDedupe("001", "k", 1L))
                .getMessage().contains("Unexpected error claiming dedupe key"));
    }

    // ==================== findByFarm / findUnreadByFarm ====================

    @Test
    void findByFarm_returnsItemsCappedByLimit() {
        stubQuery(IntStream.range(0, 5).mapToObj(i -> notif("n" + i)).toList());

        List<NotificationItem> result = repository.findByFarm("001", 3);

        assertEquals(3, result.size());
    }

    @Test
    void findUnreadByFarm_appliesUnreadFilterExpression() {
        stubQuery(List.of(notif("n1")));

        repository.findUnreadByFarm("001", 10);

        ArgumentCaptor<QueryEnhancedRequest> captor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(table).query(captor.capture());
        assertTrue(captor.getValue().filterExpression().expression().contains("#status = :unread"));
    }

    @Test
    void findByFarm_dynamoDbException_wraps() {
        when(table.query(any(QueryEnhancedRequest.class))).thenThrow(DynamoDbException.builder().message("m").build());
        assertTrue(assertThrows(RepositoryException.class, () -> repository.findByFarm("001", 10))
                .getMessage().contains("DynamoDB error querying notifications"));
    }

    @Test
    void findByFarm_resourceNotFound_wraps() {
        when(table.query(any(QueryEnhancedRequest.class))).thenThrow(ResourceNotFoundException.builder().message("m").build());
        assertTrue(assertThrows(RepositoryException.class, () -> repository.findByFarm("001", 10))
                .getMessage().contains("Notifications table not found while querying notifications"));
    }

    @Test
    void findByFarm_unexpectedException_wraps() {
        when(table.query(any(QueryEnhancedRequest.class))).thenThrow(new IllegalStateException("m"));
        assertTrue(assertThrows(RepositoryException.class, () -> repository.findByFarm("001", 10))
                .getMessage().contains("Unexpected error querying notifications"));
    }

    // ==================== findById ====================

    @Test
    @SuppressWarnings("unchecked")
    void findById_found_returnsItem() {
        NotificationItem stored = notif("n1");
        when(table.getItem(any(Consumer.class))).thenReturn(stored);

        Optional<NotificationItem> result = repository.findById("001", "n1");

        assertSame(stored, result.orElseThrow());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_missing_returnsEmpty() {
        when(table.getItem(any(Consumer.class))).thenReturn(null);
        assertTrue(repository.findById("001", "n1").isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_dynamoDbException_wraps() {
        when(table.getItem(any(Consumer.class))).thenThrow(DynamoDbException.builder().message("m").build());
        assertTrue(assertThrows(RepositoryException.class, () -> repository.findById("001", "n1"))
                .getMessage().contains("DynamoDB error reading notification"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_resourceNotFound_wraps() {
        when(table.getItem(any(Consumer.class))).thenThrow(ResourceNotFoundException.builder().message("m").build());
        assertTrue(assertThrows(RepositoryException.class, () -> repository.findById("001", "n1"))
                .getMessage().contains("Notifications table not found while reading notification"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findById_unexpectedException_wraps() {
        when(table.getItem(any(Consumer.class))).thenThrow(new IllegalStateException("m"));
        assertTrue(assertThrows(RepositoryException.class, () -> repository.findById("001", "n1"))
                .getMessage().contains("Unexpected error reading notification"));
    }
}
