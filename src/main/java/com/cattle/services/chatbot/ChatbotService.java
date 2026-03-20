package com.cattle.services.chatbot;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.chatbot.ChatRequestDTO;
import com.cattle.dtos.chatbot.ChatResponseDTO;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.enums.LogType;
import com.cattle.services.ContextBuilderService;
import com.cattle.services.IntentDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio orquestador principal del chatbot.
 * Coordina: detección de intención → construcción de contexto → invocación de Bedrock.
 */
@Slf4j
@Service
public class ChatbotService {

    private final IntentDetectionService intentDetectionService;
    private final ContextBuilderService contextBuilderService;
    private final BedrockService bedrockService;
    private final LambdaContext lambdaContext;

    public ChatbotService(IntentDetectionService intentDetectionService,
                          ContextBuilderService contextBuilderService,
                          BedrockService bedrockService, LambdaContext lambdaContext) {
        this.intentDetectionService = intentDetectionService;
        this.contextBuilderService = contextBuilderService;
        this.bedrockService = bedrockService;
        this.lambdaContext = lambdaContext;
    }

    /**
     * Procesa un mensaje de chat del usuario y genera respuesta inteligente
     */
    public ChatResponseDTO chat(String farmId, ChatRequestDTO request) {
        lambdaContext.logInfo(LogType.SERVICE,"Processing chat request for farmId: " + farmId);
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Detectar intención
            lambdaContext.logInfo(LogType.SERVICE, "Step 1: Intent detection");
            IntentContext intentContext = intentDetectionService.detectIntent(request.getUserMessage());
            lambdaContext.logInfo(LogType.SERVICE,"Intent detected: " + intentContext.getIntent() + " with confidence: " + intentContext.getConfidenceScore());
            
            // 2. Construir contexto con datos reales
            lambdaContext.logInfo(LogType.SERVICE,"Step 2: Building context");
            String context = contextBuilderService.buildContext(intentContext, farmId);
            
            // 3. Construir prompt enriquecido
            lambdaContext.logInfo(LogType.SERVICE,"Step 3: Building enriched prompt");
            String enrichedPrompt = contextBuilderService.buildPrompt(request.getUserMessage(), context);
            
            // 4. Invocar Bedrock
            lambdaContext.logInfo(LogType.SERVICE,"Step 4: Invoking Bedrock");
            String aiResponse = bedrockService.invokeModel(enrichedPrompt);
            
            // 5. Construir respuesta
            long duration = System.currentTimeMillis() - startTime;
            lambdaContext.logInfo(LogType.SERVICE,"Chat request completed in" +duration+ "ms");
            
            return ChatResponseDTO.builder()
                    .response(aiResponse)
                    .intent(intentContext.getIntent().toString())
                    .durationMs(duration)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            lambdaContext.logException(LogType.SERVICE, "Error processing chat request", e);
            long duration = System.currentTimeMillis() - startTime;
            
            return ChatResponseDTO.builder()
                    .response("Lo siento, hubo un error procesando tu consulta. Por favor intenta de nuevo.")
                    .intent("ERROR")
                    .durationMs(duration)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}

