package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineSummary;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("repository")
class BovineSummaryRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<BovineSummary> table;

    @Mock
    private DynamoDbIndex<BovineSummary> gsi1Index;

    @Mock
    private PageIterable<BovineSummary> pageIterable;

    @Mock
    private Page<BovineSummary> page;

    private BovineSummaryRepository repository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new BovineSummaryRepository(lambdaContext, enhancedClient);
    }

    @Test
    void findAll_withResults_returnsAccumulatedList() {
        List<BovineSummary> items = List.of(createSummary(167), createSummary(168));

        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(page.items()).thenReturn(items);
        org.mockito.Mockito.doAnswer(invocation -> {
            java.util.function.Consumer<Page<BovineSummary>> consumer = invocation.getArgument(0);
            consumer.accept(page);
            return null;
        }).when(pageIterable).forEach(any());

        List<BovineSummary> result = repository.findAll();

        assertEquals(2, result.size());
        assertEquals(167, result.get(0).getBovineId());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("findAll summaries: 2 records found"));
    }

    @Test
    void findAll_resourceNotFound_returnsEmptyList() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing index").build());

        List<BovineSummary> result = repository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("BovineSummary table/index not found"), any(ResourceNotFoundException.class));
    }

    @Test
    void findAll_dynamoDbException_throwsRepositoryException() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("query failed").build());

        assertThrows(RepositoryException.class, () -> repository.findAll());

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding summaries"), any(DynamoDbException.class));
    }

    @Test
    void findAllPaginated_withResults_returnsFirstPage() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(List.of(createSummary(167)));

        Page<BovineSummary> result = repository.findAllPaginated(10, null);

        assertSame(page, result);
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("findAllPaginated: 1 records found"));
    }

    @Test
    void findAllPaginated_withoutResults_returnsEmptyPage() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(Collections.<Page<BovineSummary>>emptyList().iterator());

        Page<BovineSummary> result = repository.findAllPaginated(10, null);

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        verify(lambdaContext, never()).logInfo(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void findAllPaginated_withLastEvaluatedKey_appliesExclusiveStartKey() {
        Map<String, AttributeValue> lastEvaluatedKey = Map.of(
                "PK", AttributeValue.builder().s("BOVINE#167").build(),
                "SK", AttributeValue.builder().s("SUMMARY").build()
        );
        ArgumentCaptor<QueryEnhancedRequest> captor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);

        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(captor.capture())).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(Collections.<Page<BovineSummary>>emptyList().iterator());

        repository.findAllPaginated(25, lastEvaluatedKey);

        QueryEnhancedRequest request = captor.getValue();
        assertEquals(25, request.limit());
        assertEquals(lastEvaluatedKey, request.exclusiveStartKey());
    }

    @Test
    void findAllPaginated_dynamoDbException_throwsRepositoryException() {
        when(table.index(anyString())).thenReturn(gsi1Index);
        when(gsi1Index.query(any(QueryEnhancedRequest.class)))
                .thenThrow(DynamoDbException.builder().message("page failed").build());

        assertThrows(RepositoryException.class, () -> repository.findAllPaginated(10, null));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error finding paginated summaries"), any(DynamoDbException.class));
    }

    @Test
    void findById_string_existingItem_returnsOptional() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        BovineSummary expected = createSummary(167);
        when(table.getItem(captor.capture())).thenReturn(expected);

        Optional<BovineSummary> result = repository.findById("167");

        assertTrue(result.isPresent());
        assertSame(expected, result.get());
        assertEquals("BOVINE#167", captor.getValue().partitionKeyValue().s());
        assertTrue(captor.getValue().sortKeyValue().isPresent());
        assertEquals("SUMMARY", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void findById_string_missingItem_returnsEmptyOptional() {
        when(table.getItem(any(Key.class))).thenReturn(null);

        Optional<BovineSummary> result = repository.findById("167");

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_string_resourceNotFound_returnsEmptyOptional() {
        when(table.getItem(any(Key.class)))
                .thenThrow(ResourceNotFoundException.builder().message("not found").build());

        Optional<BovineSummary> result = repository.findById("167");

        assertTrue(result.isEmpty());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("BovineSummary not found for id: 167"), any(ResourceNotFoundException.class));
    }

    @Test
    void findById_string_unexpectedException_throwsRepositoryException() {
        when(table.getItem(any(Key.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(RepositoryException.class, () -> repository.findById("167"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("findById summary error for id: 167"), any(RuntimeException.class));
    }

    @Test
    void findById_integer_buildsKeyCorrectly() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);
        when(table.getItem(captor.capture())).thenReturn(createSummary(42));

        Optional<BovineSummary> result = repository.findById(42);

        assertTrue(result.isPresent());
        assertEquals("BOVINE#42", captor.getValue().partitionKeyValue().s());
        assertTrue(captor.getValue().sortKeyValue().isPresent());
        assertEquals("SUMMARY", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    @Test
    void save_validEntity_returnsSameEntity() {
        BovineSummary entity = createSummary(167);
        doNothing().when(table).putItem(entity);

        BovineSummary result = repository.save(entity);

        assertSame(entity, result);
        verify(table).putItem(entity);
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("BovineSummary saved for: BOVINE#167"));
    }

    @Test
    void save_dynamoDbException_throwsRepositoryException() {
        BovineSummary entity = createSummary(167);
        doThrow(DynamoDbException.builder().message("save failed").build()).when(table).putItem(entity);

        assertThrows(RepositoryException.class, () -> repository.save(entity));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving BovineSummary"), any(DynamoDbException.class));
    }

    @Test
    void saveAll_validEntities_returnsSize() {
        List<BovineSummary> entities = List.of(createSummary(167), createSummary(168), createSummary(169));

        int result = repository.saveAll(entities);

        assertEquals(3, result);
        verify(table, times(3)).putItem(any(BovineSummary.class));
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("BovineSummary batch saved: 3 records"));
    }

    @Test
    void saveAll_dynamoDbException_throwsRepositoryException() {
        List<BovineSummary> entities = List.of(createSummary(167), createSummary(168));
        doNothing().doThrow(DynamoDbException.builder().message("batch failed").build())
                .when(table).putItem(any(BovineSummary.class));

        assertThrows(RepositoryException.class, () -> repository.saveAll(entities));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error saving BovineSummary batch"), any(DynamoDbException.class));
    }

    @Test
    void delete_string_buildsKeyCorrectly() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);

        repository.delete("167");

        verify(table).deleteItem(captor.capture());
        assertEquals("BOVINE#167", captor.getValue().partitionKeyValue().s());
        assertTrue(captor.getValue().sortKeyValue().isPresent());
        assertEquals("SUMMARY", captor.getValue().sortKeyValue().orElseThrow().s());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), eq("BovineSummary deleted for: BOVINE#167"));
    }

    @Test
    void delete_string_dynamoDbException_throwsRepositoryException() {
        doThrow(DynamoDbException.builder().message("delete failed").build()).when(table).deleteItem(any(Key.class));

        assertThrows(RepositoryException.class, () -> repository.delete("167"));

        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error deleting BovineSummary"), any(DynamoDbException.class));
    }

    @Test
    void delete_integer_buildsKeyCorrectly() {
        ArgumentCaptor<Key> captor = ArgumentCaptor.forClass(Key.class);

        repository.delete(167);

        verify(table).deleteItem(captor.capture());
        assertEquals("BOVINE#167", captor.getValue().partitionKeyValue().s());
        assertTrue(captor.getValue().sortKeyValue().isPresent());
        assertEquals("SUMMARY", captor.getValue().sortKeyValue().orElseThrow().s());
    }

    private BovineSummary createSummary(Integer bovineId) {
        return BovineSummary.builder()
                .pk("BOVINE#" + bovineId)
                .sk("SUMMARY")
                .gsi1pk("SUMMARY")
                .gsi1sk("BOVINE#" + bovineId)
                .bovineId(bovineId)
                .name("Bovine " + bovineId)
                .status("OPEN")
                .build();
    }
}