package com.cattle.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RepositoryConfig
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Config
 */
@Tag("unit")
@Tag("config")
class RepositoryConfigTest {

    @Test
    void dynamoDbClient_createsClientBean() {
        // Arrange
        RepositoryConfig config = new RepositoryConfig();
        DynamoDbClient mockClient = mock(DynamoDbClient.class);
        DynamoDbClientBuilder mockBuilder = mock(DynamoDbClientBuilder.class);
        
        try (MockedStatic<DynamoDbClient> mockedStatic = Mockito.mockStatic(DynamoDbClient.class)) {
            mockedStatic.when(DynamoDbClient::builder).thenReturn(mockBuilder);
            when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockClient);
            
            // Act
            DynamoDbClient result = config.dynamoDbClient();
            
            // Assert
            assertNotNull(result);
            assertEquals(mockClient, result);
        }
    }

    @Test
    void enhancedClient_withDynamoDbClient_createsEnhancedClient() {
        // Arrange
        RepositoryConfig config = new RepositoryConfig();
        DynamoDbClient mockClient = mock(DynamoDbClient.class);
        DynamoDbEnhancedClient mockEnhancedClient = mock(DynamoDbEnhancedClient.class);
        DynamoDbEnhancedClient.Builder mockBuilder = mock(DynamoDbEnhancedClient.Builder.class);
        
        try (MockedStatic<DynamoDbEnhancedClient> mockedStatic = Mockito.mockStatic(DynamoDbEnhancedClient.class)) {
            mockedStatic.when(DynamoDbEnhancedClient::builder).thenReturn(mockBuilder);
            when(mockBuilder.dynamoDbClient(any(DynamoDbClient.class))).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockEnhancedClient);
            
            // Act
            DynamoDbEnhancedClient result = config.enhancedClient(mockClient);
            
            // Assert
            assertNotNull(result);
            assertEquals(mockEnhancedClient, result);
        }
    }

    @Test
    void repositoryConfig_canBeInstantiated() {
        // Act
        RepositoryConfig config = new RepositoryConfig();
        
        // Assert
        assertNotNull(config);
    }
}
