package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.chatbot.ChatRequestDTO;
import com.cattle.dtos.chatbot.ChatResponseDTO;
import com.cattle.security.FarmUserPrincipal;
import com.cattle.services.AuditLoggingService;
import com.cattle.services.InputValidationService;
import com.cattle.services.RateLimitingService;
import com.cattle.services.chatbot.ChatbotService;
import com.cattle.services.knowledge.KnowledgeBaseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("chatbot")
class ChatbotControllerMessageTest {

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
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = openMocks(this);
        chatbotController = new ChatbotController(
                chatbotService,
                knowledgeBaseService,
                lambdaContext,
                rateLimitingService,
                inputValidationService,
                auditLoggingService
        );

        setAuthenticatedPrincipal("farm-test-001", "user-test-001");
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void sendMessage_validRequest_returnsOk() {
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("¿Cuántos bovinos tengo?")
                .conversationId("conv-123")
                .build();
        ChatResponseDTO expectedResponse = ChatResponseDTO.builder()
                .response("Tienes 120 bovinos registrados")
                .intent("COUNT_BOVINES")
                .durationMs(250L)
                .timestamp(LocalDateTime.now())
                .build();
        ArgumentCaptor<ChatRequestDTO> requestCaptor = ArgumentCaptor.forClass(ChatRequestDTO.class);

        when(rateLimitingService.allowRequest(anyString())).thenReturn(true);
        when(inputValidationService.sanitize(anyString())).thenReturn("Cuantos bovinos tengo");
        when(chatbotService.chat(eq("farm-test-001"), requestCaptor.capture())).thenReturn(expectedResponse);
        when(rateLimitingService.getRateLimitInfo(anyString()))
                .thenReturn(new RateLimitingService.RateLimitInfo(100, 99, System.currentTimeMillis() + 3600000));
        doNothing().when(auditLoggingService).logChatEvent(anyString(), anyString(), anyString(), anyLong(), anyString());

        ResponseEntity<ChatResponseDTO> response = chatbotController.sendMessage(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tienes 120 bovinos registrados", response.getBody().getResponse());
        assertEquals("100", response.getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("99", response.getHeaders().getFirst("X-RateLimit-Remaining"));
        assertEquals("Cuantos bovinos tengo", requestCaptor.getValue().getUserMessage());
        assertEquals("conv-123", requestCaptor.getValue().getConversationId());
        verify(chatbotService, times(1)).chat(eq("farm-test-001"), org.mockito.ArgumentMatchers.any(ChatRequestDTO.class));
    }

    @Test
    void sendMessage_rateLimitExceeded_returns429() {
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("Test question")
                .conversationId("conv-123")
                .build();

        when(rateLimitingService.allowRequest(anyString())).thenReturn(false);
        when(rateLimitingService.getRateLimitInfo(anyString()))
                .thenReturn(new RateLimitingService.RateLimitInfo(100, 0, System.currentTimeMillis() + 3600000));
        doNothing().when(auditLoggingService).logRateLimitExceeded(anyString(), eq(100));

        ResponseEntity<ChatResponseDTO> response = chatbotController.sendMessage(request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RATE_LIMITED", response.getBody().getIntent());
        assertTrue(response.getBody().getResponse().contains("límite"));
        assertEquals("0", response.getHeaders().getFirst("X-RateLimit-Remaining"));
        verify(chatbotService, never()).chat(anyString(), org.mockito.ArgumentMatchers.any(ChatRequestDTO.class));
    }

    @Test
    void sendMessage_invalidInput_returns400() {
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("<script>alert('xss')</script>")
                .conversationId("conv-123")
                .build();

        when(rateLimitingService.allowRequest(anyString())).thenReturn(true);
        when(inputValidationService.sanitize(anyString()))
                .thenThrow(new IllegalArgumentException("Input contiene caracteres no permitidos"));
        doNothing().when(auditLoggingService).logSecurityError(anyString(), anyString(), anyString());

        ResponseEntity<ChatResponseDTO> response = chatbotController.sendMessage(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getIntent());
        assertTrue(response.getBody().getResponse().contains("no permitidos"));
    }

    @Test
    void sendMessage_serviceError_returns500() {
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("Test question")
                .conversationId("conv-123")
                .build();

        when(rateLimitingService.allowRequest(anyString())).thenReturn(true);
        when(inputValidationService.sanitize(anyString())).thenReturn("Test question");
        when(chatbotService.chat(anyString(), org.mockito.ArgumentMatchers.any(ChatRequestDTO.class)))
                .thenThrow(new RuntimeException("Bedrock unavailable"));
        doNothing().when(auditLoggingService).logChatEvent(anyString(), anyString(), anyString(), anyLong(), anyString());

        ResponseEntity<ChatResponseDTO> response = chatbotController.sendMessage(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getIntent());
        assertTrue(response.getBody().getResponse().contains("error procesando"));
        verify(auditLoggingService).logChatEvent(anyString(), anyString(), eq("ERROR"), anyLong(), eq("ERROR"));
    }

    @Test
    void sendMessage_missingCredentials_returns401() {
        SecurityContextHolder.clearContext();
        ChatRequestDTO request = ChatRequestDTO.builder()
                .userMessage("Test question")
                .conversationId("conv-123")
                .build();

        ResponseEntity<ChatResponseDTO> response = chatbotController.sendMessage(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNAUTHORIZED", response.getBody().getIntent());
        assertTrue(response.getBody().getResponse().contains("Credenciales inválidas"));
        verify(rateLimitingService, never()).allowRequest(anyString());
    }

    @Test
    void health_returnsOk() {
        ResponseEntity<String> response = chatbotController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Chatbot service is healthy", response.getBody());
    }

    private void setAuthenticatedPrincipal(String farmId, String userId) {
        FarmUserPrincipal principal = new FarmUserPrincipal(farmId, userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }
}