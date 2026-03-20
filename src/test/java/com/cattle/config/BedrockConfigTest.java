package com.cattle.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClientBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para BedrockConfig
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Config
 */
@Tag("unit")
@Tag("config")
class BedrockConfigTest {

    @Test
    void bedrockRuntimeClient_createsClientBean() {
        // Arrange
        BedrockConfig config = new BedrockConfig();
        BedrockRuntimeClient mockClient = mock(BedrockRuntimeClient.class);
        BedrockRuntimeClientBuilder mockBuilder = mock(BedrockRuntimeClientBuilder.class);
        
        try (MockedStatic<BedrockRuntimeClient> mockedStatic = Mockito.mockStatic(BedrockRuntimeClient.class)) {
            mockedStatic.when(BedrockRuntimeClient::builder).thenReturn(mockBuilder);
            when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
            when(mockBuilder.credentialsProvider(any(EnvironmentVariableCredentialsProvider.class))).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockClient);
            
            // Act
            BedrockRuntimeClient result = config.bedrockRuntimeClient();
            
            // Assert
            assertNotNull(result);
            assertEquals(mockClient, result);
        }
    }

    @Test
    void bedrockConfig_canBeInstantiated() {
        // Act
        BedrockConfig config = new BedrockConfig();
        
        // Assert
        assertNotNull(config);
    }
}
