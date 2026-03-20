package com.cattle.services.chatbot;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.chatbot.ChatRequestDTO;
import com.cattle.dtos.chatbot.ChatResponseDTO;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.enums.QueryIntent;
import com.cattle.services.ContextBuilderService;
import com.cattle.services.IntentDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para ChatbotService
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase services.chatbot
 * 
 * Cobertura objetivo: services.chatbot 12% → 60%
 */
@Tag("unit")
@Tag("chatbot")
class ChatbotServiceTest {

    @Mock
    private IntentDetectionService intentDetectionService;

    @Mock
    private ContextBuilderService contextBuilderService;

    @Mock
    private BedrockService bedrockService;

    @Mock
    private LambdaContext lambdaContext;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        chatbotService = new ChatbotService(
                intentDetectionService,
                contextBuilderService,
                bedrockService,
                lambdaContext
        );
    }

    // ==================== chat Tests ====================

    @Test
    void chat_validRequest_returnsResponse() {
        // Arrange
        String farmId = "farm-001";
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("¿Cuántas vacas hay en la finca?")
                .build();

        IntentContext intentContext = IntentContext.builder()
                .intent(QueryIntent.COUNT_BOVINES)
                .confidenceScore(0.95)
                .build();

        when(intentDetectionService.detectIntent(anyString())).thenReturn(intentContext);
        when(contextBuilderService.buildContext(any(IntentContext.class), anyString()))
                .thenReturn("Contexto: 50 vacas en la finca");
        when(contextBuilderService.buildPrompt(anyString(), anyString()))
                .thenReturn("Prompt enriquecido");
        when(bedrockService.invokeModel(anyString()))
                .thenReturn("Hay 50 vacas en tu finca.");

        // Act
        ChatResponseDTO result = chatbotService.chat(farmId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Hay 50 vacas en tu finca.", result.getResponse());
        assertEquals("COUNT_BOVINES", result.getIntent());
        assertNotNull(result.getDurationMs());
        assertNotNull(result.getTimestamp());

        verify(intentDetectionService, times(1)).detectIntent(anyString());
        verify(contextBuilderService, times(1)).buildContext(any(), anyString());
        verify(bedrockService, times(1)).invokeModel(anyString());
    }

    @Test
    void chat_milkingQuery_returnsCorrectIntent() {
        // Arrange
        String farmId = "farm-001";
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("¿Cuántos litros de leche se produjeron hoy?")
                .build();

        IntentContext intentContext = IntentContext.builder()
                .intent(QueryIntent.AGGREGATE_MILKING)
                .confidenceScore(0.92)
                .build();

        when(intentDetectionService.detectIntent(anyString())).thenReturn(intentContext);
        when(contextBuilderService.buildContext(any(IntentContext.class), anyString()))
                .thenReturn("Producción de leche: 200 litros");
        when(contextBuilderService.buildPrompt(anyString(), anyString()))
                .thenReturn("Prompt enriquecido");
        when(bedrockService.invokeModel(anyString()))
                .thenReturn("Hoy se produjeron 200 litros de leche.");

        // Act
        ChatResponseDTO result = chatbotService.chat(farmId, request);

        // Assert
        assertNotNull(result);
        assertEquals("AGGREGATE_MILKING", result.getIntent());
    }

    @Test
    void chat_intentDetectionFails_returnsErrorResponse() {
        // Arrange
        String farmId = "farm-001";
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("Test message")
                .build();

        when(intentDetectionService.detectIntent(anyString()))
                .thenThrow(new RuntimeException("Intent detection failed"));

        // Act
        ChatResponseDTO result = chatbotService.chat(farmId, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getResponse().contains("error"));
        assertEquals("ERROR", result.getIntent());
        verify(lambdaContext, times(1)).logException(any(), anyString(), any());
    }

    @Test
    void chat_bedrockFails_returnsErrorResponse() {
        // Arrange
        String farmId = "farm-001";
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("Test message")
                .build();

        IntentContext intentContext = IntentContext.builder()
                .intent(QueryIntent.COUNT_BOVINES)
                .confidenceScore(0.90)
                .build();

        when(intentDetectionService.detectIntent(anyString())).thenReturn(intentContext);
        when(contextBuilderService.buildContext(any(), anyString())).thenReturn("context");
        when(contextBuilderService.buildPrompt(anyString(), anyString())).thenReturn("prompt");
        when(bedrockService.invokeModel(anyString()))
                .thenThrow(new RuntimeException("Bedrock invocation failed"));

        // Act
        ChatResponseDTO result = chatbotService.chat(farmId, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getResponse().contains("error"));
        assertEquals("ERROR", result.getIntent());
    }

    @Test
    void chat_validRequest_measuresDuration() {
        // Arrange
        String farmId = "farm-001";
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("Test")
                .build();

        IntentContext intentContext = IntentContext.builder()
                .intent(QueryIntent.GENERAL_QUERY)
                .confidenceScore(0.8)
                .build();

        when(intentDetectionService.detectIntent(anyString())).thenReturn(intentContext);
        when(contextBuilderService.buildContext(any(), anyString())).thenReturn("context");
        when(contextBuilderService.buildPrompt(anyString(), anyString())).thenReturn("prompt");
        when(bedrockService.invokeModel(anyString())).thenReturn("Response");

        // Act
        ChatResponseDTO result = chatbotService.chat(farmId, request);

        // Assert
        assertNotNull(result.getDurationMs());
        assertTrue(result.getDurationMs() >= 0);
    }

    @Test
    void chat_logsAllSteps() {
        // Arrange
        String farmId = "farm-001";
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("Test")
                .build();

        IntentContext intentContext = IntentContext.builder()
                .intent(QueryIntent.GENERAL_QUERY)
                .confidenceScore(0.8)
                .build();

        when(intentDetectionService.detectIntent(anyString())).thenReturn(intentContext);
        when(contextBuilderService.buildContext(any(), anyString())).thenReturn("context");
        when(contextBuilderService.buildPrompt(anyString(), anyString())).thenReturn("prompt");
        when(bedrockService.invokeModel(anyString())).thenReturn("Response");

        // Act
        chatbotService.chat(farmId, request);

        // Assert - verifica que se loguearon los pasos
        verify(lambdaContext, atLeast(5)).logInfo(any(), anyString());
    }
}
