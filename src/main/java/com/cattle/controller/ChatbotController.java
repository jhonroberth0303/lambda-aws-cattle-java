package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.chatbot.ChatRequestDTO;
import com.cattle.dtos.chatbot.ChatResponseDTO;
import com.cattle.dtos.knowledge.KnowledgeRequestDTO;
import com.cattle.dtos.knowledge.KnowledgeResponseDTO;
import com.cattle.enums.LogType;
import com.cattle.security.FarmUserPrincipal;
import com.cattle.services.AuditLoggingService;
import com.cattle.services.InputValidationService;
import com.cattle.services.RateLimitingService;
import com.cattle.services.chatbot.ChatbotService;
import com.cattle.services.knowledge.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el chatbot inteligente con Amazon Bedrock.
 * Endpoints:
 * - POST /api/chat/message - Consultas sobre datos de la finca (DynamoDB)
 * - POST /api/chat/knowledge - Consultas sobre conocimiento técnico ganadero (Knowledge Base)
 * 
 * Seguridad implementada:
 * - Autenticación JWT obligatoria
 * - Rate limiting por finca (100 requests/hora)
 * - Validación y sanitización de input
 * - Audit logging de operaciones
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chatbot", description = "Asistente virtual inteligente con Amazon Bedrock")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final LambdaContext lambdaContext;
    private final RateLimitingService rateLimitingService;
    private final InputValidationService inputValidationService;
    private final AuditLoggingService auditLoggingService;

    public ChatbotController(ChatbotService chatbotService,
                            KnowledgeBaseService knowledgeBaseService,
                            LambdaContext lambdaContext,
                            RateLimitingService rateLimitingService,
                            InputValidationService inputValidationService,
                            AuditLoggingService auditLoggingService) {
        this.chatbotService = chatbotService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.lambdaContext = lambdaContext;
        this.rateLimitingService = rateLimitingService;
        this.inputValidationService = inputValidationService;
        this.auditLoggingService = auditLoggingService;
    }

    @Operation(
            summary = "Enviar mensaje al chatbot",
            description = "Envía un mensaje al asistente virtual y recibe una respuesta inteligente basada en los datos de la finca"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del chatbot obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Mensaje inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "429", description = "Rate limit excedido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error procesando la solicitud", content = @Content)
    })
    @PostMapping("/message")
    public ResponseEntity<ChatResponseDTO> sendMessage(@Valid @RequestBody ChatRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        // Extraer farmId del SecurityContext (inyectado por JwtAuthenticationFilter)
        String farmId = getFarmIdFromSecurityContext();
        String userId = getUserIdFromSecurityContext();
        
        lambdaContext.logInfo(LogType.CONTROLLER, "Received chat message from farmId: " + farmId);
        
        try {
            // Rate limiting check
            if (!rateLimitingService.allowRequest(farmId)) {
                auditLoggingService.logRateLimitExceeded(farmId, 100);
                
                RateLimitingService.RateLimitInfo rateLimitInfo = rateLimitingService.getRateLimitInfo(farmId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("X-RateLimit-Limit", String.valueOf(rateLimitInfo.getLimit()))
                        .header("X-RateLimit-Remaining", "0")
                        .header("Retry-After", "3600")
                        .body(ChatResponseDTO.builder()
                                .response("Has excedido el límite de consultas. Por favor, intenta de nuevo más tarde.")
                                .intent("RATE_LIMITED")
                                .build());
            }
            
            // Input sanitization
            String sanitizedMessage = inputValidationService.sanitize(request.getUserMessage());
            
            // Crear request sanitizado
            ChatRequestDTO sanitizedRequest = ChatRequestDTO.builder()
                    .userMessage(sanitizedMessage)
                    .conversationId(request.getConversationId())
                    .build();
            
            // Procesar chat
            ChatResponseDTO response = chatbotService.chat(farmId, sanitizedRequest);
            
            // Audit logging
            long duration = System.currentTimeMillis() - startTime;
            auditLoggingService.logChatEvent(farmId, userId, response.getIntent(), duration, "SUCCESS");
            
            // Agregar headers de rate limit en response exitoso
            RateLimitingService.RateLimitInfo rateLimitInfo = rateLimitingService.getRateLimitInfo(farmId);
            return ResponseEntity.ok()
                    .header("X-RateLimit-Limit", String.valueOf(rateLimitInfo.getLimit()))
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitInfo.getRemaining()))
                    .body(response);
                    
        } catch (IllegalArgumentException e) {
            // Error de validación de input
            lambdaContext.logInfo(LogType.CONTROLLER, "Invalid input: " + e.getMessage());
            auditLoggingService.logSecurityError("INVALID_INPUT", farmId, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(ChatResponseDTO.builder()
                            .response(e.getMessage())
                            .intent("VALIDATION_ERROR")
                            .build());
                            
        } catch (Exception e) {
            // Error genérico - no exponer detalles técnicos
            lambdaContext.logException(LogType.CONTROLLER, "Error in chat endpoint", e);
            auditLoggingService.logChatEvent(farmId, userId, "ERROR", 
                    System.currentTimeMillis() - startTime, "ERROR");
            
            return ResponseEntity.internalServerError()
                    .body(ChatResponseDTO.builder()
                            .response("Ha ocurrido un error procesando tu solicitud. Por favor, intenta de nuevo.")
                            .intent("ERROR")
                            .build());
        }
    }

    @Operation(
            summary = "Verificar salud del chatbot",
            description = "Endpoint para verificar que el servicio de chatbot está funcionando correctamente"
    )
    @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is healthy");
    }

    @Operation(
            summary = "Consultar Knowledge Base",
            description = "Consulta información técnica ganadera basada en documentos indexados en la base de conocimiento"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = KnowledgeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Pregunta inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "429", description = "Rate limit excedido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error procesando la solicitud", content = @Content)
    })
    @PostMapping("/knowledge")
    public ResponseEntity<KnowledgeResponseDTO> queryKnowledge(@Valid @RequestBody KnowledgeRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        // Extraer farmId del SecurityContext
        String farmId = getFarmIdFromSecurityContext();
        String userId = getUserIdFromSecurityContext();
        
        lambdaContext.logInfo(LogType.CONTROLLER, "Received knowledge query from farmId: " + farmId);
        
        try {
            // Rate limiting check
            if (!rateLimitingService.allowRequest(farmId)) {
                auditLoggingService.logRateLimitExceeded(farmId, 100);
                
                RateLimitingService.RateLimitInfo rateLimitInfo = rateLimitingService.getRateLimitInfo(farmId);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("X-RateLimit-Limit", String.valueOf(rateLimitInfo.getLimit()))
                        .header("X-RateLimit-Remaining", "0")
                        .header("Retry-After", "3600")
                        .body(KnowledgeResponseDTO.builder()
                                .answer("Has excedido el límite de consultas. Por favor, intenta de nuevo más tarde.")
                                .build());
            }
            
            // Input sanitization
            String sanitizedQuestion = inputValidationService.sanitize(request.getQuestion());
            
            // Consultar Knowledge Base
            KnowledgeResponseDTO response = knowledgeBaseService.query(sanitizedQuestion);
            
            // Audit logging
            long duration = System.currentTimeMillis() - startTime;
            auditLoggingService.logChatEvent(farmId, userId, "KNOWLEDGE_QUERY", duration, "SUCCESS");
            
            // Agregar headers de rate limit en response exitoso
            RateLimitingService.RateLimitInfo rateLimitInfo = rateLimitingService.getRateLimitInfo(farmId);
            return ResponseEntity.ok()
                    .header("X-RateLimit-Limit", String.valueOf(rateLimitInfo.getLimit()))
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitInfo.getRemaining()))
                    .body(response);
                    
        } catch (IllegalArgumentException e) {
            // Error de validación de input
            lambdaContext.logInfo(LogType.CONTROLLER, "Invalid input: " + e.getMessage());
            auditLoggingService.logSecurityError("INVALID_INPUT", farmId, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(KnowledgeResponseDTO.builder()
                            .answer(e.getMessage())
                            .build());
                            
        } catch (Exception e) {
            // Error genérico - no exponer detalles técnicos
            lambdaContext.logException(LogType.CONTROLLER, "Error in knowledge endpoint", e);
            auditLoggingService.logChatEvent(farmId, userId, "KNOWLEDGE_ERROR", 
                    System.currentTimeMillis() - startTime, "ERROR");
            
            return ResponseEntity.internalServerError()
                    .body(KnowledgeResponseDTO.builder()
                            .answer("Ha ocurrido un error procesando tu consulta. Por favor, intenta de nuevo.")
                            .build());
        }
    }
    
    /**
     * Extrae el farmId del SecurityContext.
     * El farmId fue inyectado por JwtAuthenticationFilter después de validar el token.
     */
    private String getFarmIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof FarmUserPrincipal) {
            FarmUserPrincipal principal = (FarmUserPrincipal) authentication.getPrincipal();
            return principal.getFarmId();
        }
        
        // Fallback para desarrollo/testing sin autenticación
        log.warn("No FarmUserPrincipal found in SecurityContext, using default farmId");
        return "FARM#001";
    }
    
    /**
     * Extrae el userId del SecurityContext.
     */
    private String getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof FarmUserPrincipal) {
            FarmUserPrincipal principal = (FarmUserPrincipal) authentication.getPrincipal();
            return principal.getUserId();
        }
        
        return "UNKNOWN";
    }
}
