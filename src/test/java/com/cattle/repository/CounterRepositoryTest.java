package com.cattle.repository;

import com.cattle.config.LambdaContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para CountersRepository
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Repository
 */
@Tag("unit")
@Tag("repository")
class CounterRepositoryTest {

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbClient dynamoDbClient;

    private CounterRepository counterRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        counterRepository = new CounterRepository(lambdaContext, enhancedClient, dynamoDbClient);
    }

    // ==================== getNextId Tests ====================

    @Test
    void getNextId_firstCall_returnsOne() {
        // Arrange
        String entityName = "bovine";
        Map<String, AttributeValue> attributes = Map.of(
                "nextId", AttributeValue.builder().n("1").build()
        );
        UpdateItemResponse response = UpdateItemResponse.builder()
                .attributes(attributes)
                .build();
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(response);

        // Act
        String result = counterRepository.getNextId(entityName);

        // Assert
        assertEquals("1", result);
        verify(dynamoDbClient, times(1)).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void getNextId_multipleCall_returnsIncrementedValue() {
        // Arrange
        String entityName = "bovine";
        Map<String, AttributeValue> attributes = Map.of(
                "nextId", AttributeValue.builder().n("42").build()
        );
        UpdateItemResponse response = UpdateItemResponse.builder()
                .attributes(attributes)
                .build();
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(response);

        // Act
        String result = counterRepository.getNextId(entityName);

        // Assert
        assertEquals("42", result);
    }

    @Test
    void getNextId_differentEntities_callsWithCorrectEntity() {
        // Arrange
        String entityName = "milking";
        Map<String, AttributeValue> attributes = Map.of(
                "nextId", AttributeValue.builder().n("100").build()
        );
        UpdateItemResponse response = UpdateItemResponse.builder()
                .attributes(attributes)
                .build();
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(response);

        // Act
        String result = counterRepository.getNextId(entityName);

        // Assert
        assertEquals("100", result);
    }

    @Test
    void getNextId_dynamoDbException_propagatesException() {
        // Arrange
        String entityName = "bovine";
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

        // Act & Assert
        assertThrows(DynamoDbException.class, () -> counterRepository.getNextId(entityName));
    }
}
