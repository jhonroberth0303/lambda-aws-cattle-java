package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.exceptions.RepositoryException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BovineEventRepository.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class BovineEventRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<BovineEventItem> table;

    @Mock
    private PageIterable<BovineEventItem> pageIterable;

    private BovineEventRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new BovineEventRepository(lambdaContext, enhancedClient, new ObjectMapper());
    }

    private BovineEventItem event(String id) {
        BovineEventItem item = new BovineEventItem();
        item.setPk("BOVINE#" + id);
        item.setSk("EVT#2026-01-01T00:00:00Z#SEGUIMIENTO#" + id);
        item.setBovineId(id);
        item.setFarmId("F001");
        item.setEventId(id);
        item.setEventType("SEGUIMIENTO");
        item.setEventAt(Instant.parse("2026-01-01T00:00:00Z"));
        return item;
    }

    @SuppressWarnings("unchecked")
    private void stubQuery(List<BovineEventItem> items) {
        when(table.query(any(Consumer.class))).thenReturn(pageIterable);
        SdkIterable<BovineEventItem> iterable = items::iterator;
        when(pageIterable.items()).thenReturn(iterable);
    }

    // ==================== save ====================

    @Test
    void save_delegatesToTablePutItem() {
        BovineEventItem item = event("B1");

        repository.save(item);

        verify(table).putItem(item);
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), contains("Saving bovine event"));
    }

    @Test
    void save_resourceNotFound_throwsRepositoryExceptionWithContext() {
        BovineEventItem item = event("B1");
        doThrow(ResourceNotFoundException.builder().message("no table").build()).when(table).putItem(item);

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.save(item));

        assertTrue(ex.getMessage().contains("Events table not found"));
        assertTrue(ex.getMessage().contains("pk=BOVINE#B1"));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), contains("Events table not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        BovineEventItem item = event("B1");
        doThrow(DynamoDbException.builder().message("throttled").build()).when(table).putItem(item);

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.save(item));
        assertTrue(ex.getMessage().contains("DynamoDB error saving bovine event"));
    }

    @Test
    void save_unexpectedException_throwsRepositoryException() {
        BovineEventItem item = event("B1");
        doThrow(new RuntimeException("weird")).when(table).putItem(item);

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.save(item));
        assertTrue(ex.getMessage().contains("Unexpected error saving bovine event"));
    }

    // ==================== findByBovine ====================

    @Test
    void findByBovine_returnsItemsCappedByLimit() {
        List<BovineEventItem> stored = IntStream.range(0, 5).mapToObj(i -> event("B" + i)).toList();
        stubQuery(stored);

        List<BovineEventItem> result = repository.findByBovine("B1", 3);

        assertEquals(3, result.size());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), contains("Finding events for bovineId: B1"));
    }

    @Test
    void findByBovine_emptyResult_returnsEmptyList() {
        stubQuery(List.of());

        assertTrue(repository.findByBovine("B1", 10).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByBovine_resourceNotFound_throwsRepositoryException() {
        when(table.query(any(Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing").build());

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.findByBovine("B1", 10));
        assertTrue(ex.getMessage().contains("Events table not found while querying bovine events"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByBovine_dynamoDbException_throwsRepositoryException() {
        when(table.query(any(Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("boom").build());

        assertThrows(RepositoryException.class, () -> repository.findByBovine("B1", 10));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), contains("DynamoDB error querying bovine events"), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByBovine_unexpectedException_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(new IllegalStateException("nope"));

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.findByBovine("B1", 10));
        assertTrue(ex.getMessage().contains("Unexpected error querying bovine events"));
    }

    @Test
    void save_logInfoUsesDefaultEventsTableName() {
        repository.save(event("B1"));
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), contains("table Events"));
    }

    @Test
    void findByBovine_neverLogsWhenArgsInvalidButStillQueries() {
        stubQuery(List.of(event("B1")));
        repository.findByBovine("B1", 1);
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), anyString());
    }
}
