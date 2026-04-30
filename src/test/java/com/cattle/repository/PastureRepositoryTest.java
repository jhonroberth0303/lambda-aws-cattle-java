package com.cattle.repository;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Pasture;
import com.cattle.enums.LogType;
import com.cattle.events.EntityPatch;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para PastureRepository
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
    private DynamoDbIndex<Pasture> gsi2Index;

    @Mock
    private PageIterable<Pasture> pageIterable;

    @Mock
    private Page<Pasture> page;

    private PastureRepository pastureRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        pastureRepository = new PastureRepository(lambdaContext, enhancedClient, dynamoDbClient);
    }

    // ==================== findPastures Tests ====================

    @Test
    void findPastures_withPastures_returnsList() {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = createPastureList(farmId, 5);
        
        when(table.index(anyString())).thenReturn(gsi2Index);
        when(gsi2Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(pastures);

        // Act
        Optional<List<Pasture>> result = pastureRepository.findPastures(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get().size());
    }

    @Test
    void findPastures_emptyResult_returnsEmptyList() {
        // Arrange
        String farmId = "farm-empty";
        
        when(table.index(anyString())).thenReturn(gsi2Index);
        when(gsi2Index.query(any(java.util.function.Consumer.class))).thenReturn(pageIterable);
        when(pageIterable.iterator()).thenReturn(List.of(page).iterator());
        when(page.items()).thenReturn(new ArrayList<>());

        // Act
        Optional<List<Pasture>> result = pastureRepository.findPastures(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void findPastures_dynamoDbException_throwsRepositoryException() {
        // Arrange
        String farmId = "farm-001";
        when(table.index(anyString())).thenReturn(gsi2Index);
        when(gsi2Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

        // Act & Assert
        assertThrows(RepositoryException.class, () -> pastureRepository.findPastures(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), anyString());
    }

    @Test
    void findPastures_resourceNotFound_throwsRepositoryException() {
        String farmId = "farm-001";
        when(table.index(anyString())).thenReturn(gsi2Index);
        when(gsi2Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(ResourceNotFoundException.builder().message("missing index").build());

        RepositoryException result = assertThrows(RepositoryException.class, () -> pastureRepository.findPastures(farmId));

        assertEquals("Pasture not exist in DynamoDB", result.getMessage());
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Pasture not exist in DynamoDB"));
    }

    @Test
    void findPastures_unexpectedException_throwsRepositoryException() {
        String farmId = "farm-001";
        when(table.index(anyString())).thenReturn(gsi2Index);
        when(gsi2Index.query(any(java.util.function.Consumer.class)))
                .thenThrow(new RuntimeException("boom"));

        RepositoryException result = assertThrows(RepositoryException.class, () -> pastureRepository.findPastures(farmId));

        assertTrue(result.getMessage().contains("Unexpected error"));
        verify(lambdaContext).logException(eq(LogType.REPOSITORY), eq("Unexpected error: boom"));
    }

    // ==================== applyPatch Tests ====================

    @Test
    void applyPatch_withSetValues_updatesSuccessfully() {
        // Arrange
        String pk = "PASTURE#farm-001#pot-1";
        EntityPatch patch = EntityPatch.of()
                .set("status", "OPEN")
                .set("currentHeightCm", 25);
        
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenReturn(UpdateItemResponse.builder().build());

        // Act & Assert - no exception thrown
        assertDoesNotThrow(() -> pastureRepository.applyPatch(pk, patch));
        verify(dynamoDbClient, times(1)).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void applyPatch_withComplexValues_buildsAttributeValues() {
        String pk = "PASTURE#farm-001#pot-1";
        EntityPatch patch = EntityPatch.of()
                .set("enabled", true)
                .set("notes", null)
                .set("rotations", List.of("A", "B"))
                .set("metadata", Map.of("priority", 3, "flag", false))
                .set("custom", new Object() {
                    @Override
                    public String toString() {
                        return "custom-value";
                    }
                });
        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);

        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenReturn(UpdateItemResponse.builder().build());

        pastureRepository.applyPatch(pk, patch);

        verify(dynamoDbClient).updateItem(captor.capture());
        Map<String, AttributeValue> values = captor.getValue().expressionAttributeValues();
        assertTrue(values.values().stream().anyMatch(value -> Boolean.TRUE.equals(value.bool())));
        assertTrue(values.values().stream().anyMatch(value -> Boolean.TRUE.equals(value.nul())));
        assertTrue(values.values().stream().anyMatch(value -> value.hasL() && value.l().size() == 2));
        assertTrue(values.values().stream().anyMatch(value -> value.hasM() && value.m().containsKey("priority") && value.m().containsKey("flag")));
        assertTrue(values.values().stream().anyMatch(value -> "custom-value".equals(value.s())));
    }

    @Test
    void applyPatch_nullPatch_doesNothing() {
        // Act
        pastureRepository.applyPatch("pk", null);

        // Assert
        verify(dynamoDbClient, never()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void applyPatch_emptyPatch_doesNothing() {
        // Arrange
        EntityPatch emptyPatch = EntityPatch.of();

        // Act
        pastureRepository.applyPatch("pk", emptyPatch);

        // Assert
        verify(dynamoDbClient, never()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void applyPatch_withRemoveValues_updatesSuccessfully() {
        // Arrange
        String pk = "PASTURE#farm-001#pot-1";
        EntityPatch patch = EntityPatch.of()
                .remove("substatus")
                .remove("blockReason");
        
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenReturn(UpdateItemResponse.builder().build());

        // Act & Assert
        assertDoesNotThrow(() -> pastureRepository.applyPatch(pk, patch));
        verify(dynamoDbClient, times(1)).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void applyPatch_dynamoDbException_throwsException() {
        // Arrange
        String pk = "PASTURE#farm-001#pot-1";
        EntityPatch patch = EntityPatch.of()
                .set("status", "OPEN");
        
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenThrow(DynamoDbException.builder().message("Update error").build());

        // Act & Assert - verifica que se normaliza a RepositoryException
        assertThrows(RepositoryException.class, () -> pastureRepository.applyPatch(pk, patch));
        verify(lambdaContext, times(1)).logException(eq(LogType.REPOSITORY), eq("Error applying pasture patch"), any(DynamoDbException.class));
    }

    // ==================== Helper Methods ====================

    private Pasture createPasture(String farmId, String id) {
        return Pasture.builder()
                .pk("PASTURE#" + farmId + "#" + id)
                .farmId(farmId)
                .id(id)
                .name("Potrero " + id)
                .species("kikuyo")
                .status("OPEN")
                .areaHa(5.5)
                .currentHeightCm(25)
                .gsi1pk("kikuyo")
                .gsi2pk(farmId + "#blocked#false")
                .build();
    }

    private List<Pasture> createPastureList(String farmId, int count) {
        List<Pasture> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createPasture(farmId, "pot-" + i));
        }
        return list;
    }
}
