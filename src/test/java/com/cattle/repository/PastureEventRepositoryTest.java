package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Tests unitarios para PastureEventRepository.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class PastureEventRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<PastureEventItem> table;

    @Mock
    private PageIterable<PastureEventItem> pageIterable;

    private PastureEventRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new PastureEventRepository(lambdaContext, enhancedClient, new ObjectMapper());
    }

    private PastureEventItem event(String id) {
        PastureEventItem item = new PastureEventItem();
        item.setPk("PASTURE#" + id);
        item.setSk("EVT#2026-01-01T00:00:00Z#OPEN#" + id);
        item.setFarmId("F001");
        item.setEventId(id);
        item.setEventType("OPEN");
        return item;
    }

    @SuppressWarnings("unchecked")
    private void stubQuery(List<PastureEventItem> items) {
        when(table.query(any(Consumer.class))).thenReturn(pageIterable);
        SdkIterable<PastureEventItem> iterable = items::iterator;
        when(pageIterable.items()).thenReturn(iterable);
    }

    // ==================== save ====================

    @Test
    void save_delegatesToTablePutItem() {
        PastureEventItem item = event("P1");
        repository.save(item);
        verify(table).putItem(item);
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), contains("Saving pasture event"));
    }

    @Test
    void save_resourceNotFound_throwsRepositoryException() {
        PastureEventItem item = event("P1");
        doThrow(ResourceNotFoundException.builder().message("x").build()).when(table).putItem(item);

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.save(item));
        assertTrue(ex.getMessage().contains("Events table not found while saving pasture event"));
        assertTrue(ex.getMessage().contains("pk=PASTURE#P1"));
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        PastureEventItem item = event("P1");
        doThrow(DynamoDbException.builder().message("x").build()).when(table).putItem(item);
        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.save(item));
        assertTrue(ex.getMessage().contains("DynamoDB error saving pasture event"));
    }

    @Test
    void save_unexpectedException_throwsRepositoryException() {
        PastureEventItem item = event("P1");
        doThrow(new RuntimeException("x")).when(table).putItem(item);
        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.save(item));
        assertTrue(ex.getMessage().contains("Unexpected error saving pasture event"));
    }

    // ==================== findByPasture ====================

    @Test
    void findByPasture_returnsItemsCappedByLimit() {
        List<PastureEventItem> stored = IntStream.range(0, 6).mapToObj(i -> event("P" + i)).toList();
        stubQuery(stored);

        List<PastureEventItem> result = repository.findByPasture("P1", 4);

        assertEquals(4, result.size());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), contains("Finding events for pastureId: P1"));
    }

    @Test
    void findByPasture_empty_returnsEmptyList() {
        stubQuery(List.of());
        assertTrue(repository.findByPasture("P1", 10).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByPasture_resourceNotFound_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(ResourceNotFoundException.builder().message("m").build());
        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.findByPasture("P1", 10));
        assertTrue(ex.getMessage().contains("Events table not found while querying pasture events"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByPasture_dynamoDbException_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(DynamoDbException.builder().message("m").build());
        assertThrows(RepositoryException.class, () -> repository.findByPasture("P1", 10));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), contains("DynamoDB error querying pasture events"), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByPasture_unexpectedException_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(new IllegalStateException("m"));
        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.findByPasture("P1", 10));
        assertTrue(ex.getMessage().contains("Unexpected error querying pasture events"));
    }
}
