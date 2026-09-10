package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Pasture;
import com.cattle.enums.LogType;
import com.cattle.events.EntityPatch;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para PastureRepository.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class PastureRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Mock
    private DynamoDbTable<Pasture> table;

    @Mock
    private Page<Pasture> page;

    private PastureRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        repository = new PastureRepository(lambdaContext, enhancedClient, dynamoDbClient);
    }

    @SuppressWarnings("unchecked")
    private void stubQueryReturns(List<Pasture> items) {
        when(page.items()).thenReturn(items);
        PageIterable<Pasture> pageIterable = () -> List.of(page).iterator();
        when(table.query(any(Consumer.class))).thenReturn(pageIterable);
    }

    // ==================== findPastures2 ====================

    @Test
    void findPastures2_withResults_returnsList() {
        stubQueryReturns(List.of(pasture("P1"), pasture("P2")));

        Optional<List<Pasture>> result = repository.findPastures2("F001");

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(lambdaContext).logInfo(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void findPastures2_emptyPage_returnsEmptyList() {
        stubQueryReturns(List.of());

        Optional<List<Pasture>> result = repository.findPastures2("F001");

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findPastures2_resourceNotFound_throwsRepositoryExceptionWithDomainMessage() {
        when(table.query(any(Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing").build());

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.findPastures2("F001"));

        assertEquals("Pasture not exist in DynamoDB", ex.getMessage());
        verify(lambdaContext).logException(LogType.REPOSITORY, "Pasture not exist in DynamoDB");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findPastures2_dynamoDbException_throwsRepositoryException() {
        when(table.query(any(Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("ddb down").build());

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.findPastures2("F001"));

        assertTrue(ex.getMessage().startsWith("DynamoDB error:"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findPastures2_unexpectedException_throwsRepositoryException() {
        when(table.query(any(Consumer.class))).thenThrow(new RuntimeException("boom"));

        RepositoryException ex = assertThrows(RepositoryException.class, () -> repository.findPastures2("F001"));

        assertEquals("Unexpected error: boom", ex.getMessage());
    }

    // ==================== applyPatch ====================

    @Test
    void applyPatch_nullPatch_doesNothing() {
        repository.applyPatch("pk", "sk", null);
        verify(dynamoDbClient, never()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void applyPatch_emptyPatch_doesNothing() {
        repository.applyPatch("pk", "sk", EntityPatch.of());
        verify(dynamoDbClient, never()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void applyPatch_setValues_buildsSetExpressionAndTypedValues() {
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());
        EntityPatch patch = EntityPatch.of()
                .set("status", "OPEN")
                .set("height", 25)
                .set("blocked", true)
                .set("cleared", null);

        repository.applyPatch("PASTURE#F1#P1", "META", patch);

        UpdateItemRequest req = captureRequest();
        assertTrue(req.updateExpression().startsWith("SET "));
        assertEquals(4, req.expressionAttributeNames().size());
        assertEquals("status", req.expressionAttributeNames().get("#n0"));
        Map<String, AttributeValue> values = req.expressionAttributeValues();
        assertEquals("OPEN", values.get(":v0").s());
        assertEquals("25", values.get(":v1").n());
        assertEquals(true, values.get(":v2").bool());
        assertTrue(values.get(":v3").nul());
        assertEquals("PASTURE#F1#P1", req.key().get("pk").s());
        assertEquals("META", req.key().get("sk").s());
    }

    @Test
    void applyPatch_removeValues_buildsRemoveExpression() {
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());
        EntityPatch patch = EntityPatch.of().remove("blockReason").remove("substatus");

        repository.applyPatch("pk", "sk", patch);

        UpdateItemRequest req = captureRequest();
        assertEquals("REMOVE #r0, #r1", req.updateExpression());
        assertEquals("blockReason", req.expressionAttributeNames().get("#r0"));
        assertTrue(req.expressionAttributeValues() == null || req.expressionAttributeValues().isEmpty());
    }

    @Test
    void applyPatch_setAndRemove_combinesBothClauses() {
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());
        EntityPatch patch = EntityPatch.of().set("status", "REST").remove("blockReason");

        repository.applyPatch("pk", "sk", patch);

        UpdateItemRequest req = captureRequest();
        assertEquals("SET #n0 = :v0 REMOVE #r0", req.updateExpression());
    }

    @Test
    void applyPatch_blankSk_keyOnlyHasPartitionKey() {
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());

        repository.applyPatch("pk", "  ", EntityPatch.of().set("a", "b"));

        UpdateItemRequest req = captureRequest();
        assertTrue(req.key().containsKey("pk"));
        assertFalse(req.key().containsKey("sk"));
    }

    @Test
    void applyPatch_convertsListMapAndFallbackValues() {
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());
        Object custom = new Object() {
            @Override public String toString() { return "custom-value"; }
        };
        EntityPatch patch = EntityPatch.of()
                .set("rotations", List.of("A", "B"))
                .set("meta", Map.of("k", 1))
                .set("weird", custom);

        repository.applyPatch("pk", "sk", patch);

        Map<String, AttributeValue> values = captureRequest().expressionAttributeValues();
        assertTrue(values.values().stream().anyMatch(v -> v.hasL() && v.l().size() == 2));
        assertTrue(values.values().stream().anyMatch(v -> v.hasM() && v.m().containsKey("k")));
        assertTrue(values.values().stream().anyMatch(v -> "custom-value".equals(v.s())));
    }

    @Test
    void applyPatch_dynamoDbException_throwsRepositoryException() {
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenThrow(DynamoDbException.builder().message("nope").build());

        assertThrows(RepositoryException.class,
                () -> repository.applyPatch("pk", "sk", EntityPatch.of().set("a", "b")));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Error applying pasture patch"), any(DynamoDbException.class));
    }

    @Test
    void applyPatch_unexpectedException_throwsRepositoryException() {
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenThrow(new RuntimeException("kaboom"));

        RepositoryException ex = assertThrows(RepositoryException.class,
                () -> repository.applyPatch("pk", "sk", EntityPatch.of().set("a", "b")));
        assertEquals("Unexpected error applying pasture patch", ex.getMessage());
    }

    // ==================== helpers ====================

    private UpdateItemRequest captureRequest() {
        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(dynamoDbClient).updateItem(captor.capture());
        return captor.getValue();
    }

    private Pasture pasture(String id) {
        return Pasture.builder().pk("farm#F001").sk("pasture#" + id).farmId("F001").id(id).name("Potrero " + id).build();
    }
}
