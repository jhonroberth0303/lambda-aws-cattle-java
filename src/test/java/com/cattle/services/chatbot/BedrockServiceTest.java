package com.cattle.services.chatbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BedrockService
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase services.chatbot
 */
@Tag("unit")
@Tag("chatbot")
class BedrockServiceTest {

    @Mock
    private BedrockRuntimeClient bedrockClient;

    private BedrockService bedrockService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        bedrockService = new BedrockService(bedrockClient);
    }

    // ==================== invokeModel Tests ====================

    @Test
    void invokeModel_validPrompt_returnsResponse() {
        // Arrange
        String enrichedPrompt = "Contexto: 50 vacas\nPregunta: ¿Cuántas vacas hay?";
        String expectedResponse = "Hay 50 vacas en tu finca.";

        ContentBlock contentBlock = ContentBlock.fromText(expectedResponse);
        Message outputMessage = Message.builder()
                .role(ConversationRole.ASSISTANT)
                .content(contentBlock)
                .build();
        
        ConverseOutput converseOutput = ConverseOutput.builder()
                .message(outputMessage)
                .build();
        
        ConverseResponse converseResponse = ConverseResponse.builder()
                .output(converseOutput)
                .build();

        when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(converseResponse);

        // Act
        String result = bedrockService.invokeModel(enrichedPrompt);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(bedrockClient, times(1)).converse(any(ConverseRequest.class));
    }

    @Test
    void invokeModel_bedrockException_throwsRuntimeException() {
        // Arrange
        String enrichedPrompt = "Test prompt";
        
        when(bedrockClient.converse(any(ConverseRequest.class)))
                .thenThrow(new RuntimeException("Bedrock error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> bedrockService.invokeModel(enrichedPrompt));
        
        assertTrue(exception.getMessage().contains("Failed to invoke Bedrock"));
    }

    @Test
    void invokeModel_emptyPrompt_callsBedrock() {
        // Arrange
        String enrichedPrompt = "";
        String expectedResponse = "No entendí tu pregunta.";

        ContentBlock contentBlock = ContentBlock.fromText(expectedResponse);
        Message outputMessage = Message.builder()
                .role(ConversationRole.ASSISTANT)
                .content(contentBlock)
                .build();
        
        ConverseOutput converseOutput = ConverseOutput.builder()
                .message(outputMessage)
                .build();
        
        ConverseResponse converseResponse = ConverseResponse.builder()
                .output(converseOutput)
                .build();

        when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(converseResponse);

        // Act
        String result = bedrockService.invokeModel(enrichedPrompt);

        // Assert
        assertNotNull(result);
        verify(bedrockClient, times(1)).converse(any(ConverseRequest.class));
    }

    @Test
    void invokeModel_longPrompt_callsBedrock() {
        // Arrange
        String enrichedPrompt = "A".repeat(5000); // Prompt largo
        String expectedResponse = "Respuesta procesada.";

        ContentBlock contentBlock = ContentBlock.fromText(expectedResponse);
        Message outputMessage = Message.builder()
                .role(ConversationRole.ASSISTANT)
                .content(contentBlock)
                .build();
        
        ConverseOutput converseOutput = ConverseOutput.builder()
                .message(outputMessage)
                .build();
        
        ConverseResponse converseResponse = ConverseResponse.builder()
                .output(converseOutput)
                .build();

        when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(converseResponse);

        // Act
        String result = bedrockService.invokeModel(enrichedPrompt);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void invokeModel_responseWithWhitespace_returnsTrimmed() {
        // Arrange
        String enrichedPrompt = "Test prompt";
        String responseWithWhitespace = "  Respuesta con espacios  \n";

        ContentBlock contentBlock = ContentBlock.fromText(responseWithWhitespace);
        Message outputMessage = Message.builder()
                .role(ConversationRole.ASSISTANT)
                .content(contentBlock)
                .build();
        
        ConverseOutput converseOutput = ConverseOutput.builder()
                .message(outputMessage)
                .build();
        
        ConverseResponse converseResponse = ConverseResponse.builder()
                .output(converseOutput)
                .build();

        when(bedrockClient.converse(any(ConverseRequest.class))).thenReturn(converseResponse);

        // Act
        String result = bedrockService.invokeModel(enrichedPrompt);

        // Assert
        assertEquals("Respuesta con espacios", result);
    }

    @Test
    void bedrockService_canBeInstantiated() {
        // Assert
        assertNotNull(bedrockService);
    }
}
