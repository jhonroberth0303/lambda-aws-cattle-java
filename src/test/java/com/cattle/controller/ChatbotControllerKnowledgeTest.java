package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.knowledge.KnowledgeRequestDTO;
import com.cattle.dtos.knowledge.KnowledgeResponseDTO;
import com.cattle.services.AuditLoggingService;
import com.cattle.services.InputValidationService;
import com.cattle.services.RateLimitingService;
import com.cattle.services.chatbot.ChatbotService;
import com.cattle.services.knowledge.KnowledgeBaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para el endpoint /api/chat/knowledge en ChatbotController.
 * HU-BEDROCK-AGENT-001 - Knowledge Base Integration
 */
@Tag("unit")
@Tag("chatbot")
class ChatbotControllerKnowledgeTest {

    @Mock
    private ChatbotService chatbotService;

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private InputValidationService inputValidationService;

    @Mock
    private AuditLoggingService auditLoggingService;

    private ChatbotController chatbotController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        chatbotController = new ChatbotController(
                chatbotService,
                knowledgeBaseService,
                lambdaContext,
                rateLimitingService,
                inputValidationService,
                auditLoggingService
        );
    }

    // ==================== queryKnowledge Tests ====================

    @Test
    void queryKnowledge_validRequest_returnsOk() {
        // Arrange
        KnowledgeRequestDTO request = KnowledgeRequestDTO.builder()
                .question("¿Cuál es el protocolo de vacunación para terneros?")
                .build();
        
        KnowledgeResponseDTO expectedResponse = KnowledgeResponseDTO.builder()
                .answer("El protocolo de vacunación incluye...")
                .citations(List.of())
                .sources(List.of())
                .durationMs(500L)
                .timestamp(LocalDateTime.now())
                .build();

        when(rateLimitingService.allowRequest(anyString())).thenReturn(true);
        when(inputValidationService.sanitize(anyString())).thenReturn(request.getQuestion());
        when(knowledgeBaseService.query(anyString())).thenReturn(expectedResponse);
        when(rateLimitingService.getRateLimitInfo(anyString()))
                .thenReturn(new RateLimitingService.RateLimitInfo(100, 99, System.currentTimeMillis() + 3600000));
        doNothing().when(auditLoggingService).logChatEvent(anyString(), anyString(), anyString(), anyLong(), anyString());

        // Act
        ResponseEntity<KnowledgeResponseDTO> response = chatbotController.queryKnowledge(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El protocolo de vacunación incluye...", response.getBody().getAnswer());
        verify(knowledgeBaseService, times(1)).query(anyString());
    }

    @Test
    void queryKnowledge_rateLimitExceeded_returns429() {
        // Arrange
        KnowledgeRequestDTO request = KnowledgeRequestDTO.builder()
                .question("Test question")
                .build();

        when(rateLimitingService.allowRequest(anyString())).thenReturn(false);
        when(rateLimitingService.getRateLimitInfo(anyString()))
                .thenReturn(new RateLimitingService.RateLimitInfo(100, 0, System.currentTimeMillis() + 3600000));
        doNothing().when(auditLoggingService).logRateLimitExceeded(any(), anyInt());

        // Act
        ResponseEntity<KnowledgeResponseDTO> response = chatbotController.queryKnowledge(request);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getAnswer().contains("límite"));
        verify(knowledgeBaseService, never()).query(anyString());
    }

    @Test
    void queryKnowledge_invalidInput_returns400() {
        // Arrange
        KnowledgeRequestDTO request = KnowledgeRequestDTO.builder()
                .question("<script>alert('xss')</script>")
                .build();

        when(rateLimitingService.allowRequest(anyString())).thenReturn(true);
        when(inputValidationService.sanitize(anyString()))
                .thenThrow(new IllegalArgumentException("Input contiene caracteres no permitidos"));
        doNothing().when(auditLoggingService).logSecurityError(any(), any(), any());

        // Act
        ResponseEntity<KnowledgeResponseDTO> response = chatbotController.queryKnowledge(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getAnswer().contains("no permitidos"));
    }

    @Test
    void queryKnowledge_serviceError_returns500() {
        // Arrange
        KnowledgeRequestDTO request = KnowledgeRequestDTO.builder()
                .question("Test question")
                .build();

        when(rateLimitingService.allowRequest(anyString())).thenReturn(true);
        when(inputValidationService.sanitize(anyString())).thenReturn(request.getQuestion());
        when(knowledgeBaseService.query(anyString()))
                .thenThrow(new RuntimeException("Knowledge Base unavailable"));
        doNothing().when(auditLoggingService).logChatEvent(anyString(), anyString(), anyString(), anyLong(), anyString());

        // Act
        ResponseEntity<KnowledgeResponseDTO> response = chatbotController.queryKnowledge(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getAnswer().contains("error"));
        verify(auditLoggingService, times(1)).logChatEvent(anyString(), anyString(), eq("KNOWLEDGE_ERROR"), anyLong(), eq("ERROR"));
    }
}
