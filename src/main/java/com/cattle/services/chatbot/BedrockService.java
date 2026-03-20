package com.cattle.services.chatbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

/**
 * Servicio de integración con Amazon Bedrock (Claude 3 Haiku).
 * Usa la Messages API (Converse API) requerida por modelos Claude 3.
 */
@Slf4j
@Service
public class BedrockService {

    private final BedrockRuntimeClient bedrockClient;
    
    @Value("${bedrock.model.id:anthropic.claude-3-haiku-20240307-v1:0}")
    private String modelId;

    public BedrockService(BedrockRuntimeClient bedrockClient) {
        this.bedrockClient = bedrockClient;
    }

    /**
     * Invoca el modelo Bedrock con un prompt enriquecido usando la Messages API
     */
    public String invokeModel(String enrichedPrompt) {
        log.debug("Invoking Bedrock model: {}", modelId);
        long startTime = System.currentTimeMillis();
        
        try {
            // Crear el mensaje del usuario usando la Messages API
            Message userMessage = Message.builder()
                    .role(ConversationRole.USER)
                    .content(ContentBlock.fromText(enrichedPrompt))
                    .build();
            
            // Construir el request usando Converse API (requerido para Claude 3)
            ConverseRequest request = ConverseRequest.builder()
                    .modelId(modelId)
                    .messages(userMessage)
                    .inferenceConfig(config -> config
                            .maxTokens(500)
                            .temperature(0.7F)
                            .topP(0.9F))
                    .build();
            
            // Invocar el modelo
            ConverseResponse response = bedrockClient.converse(request);
            
            // Extraer la respuesta
            String aiResponse = response.output()
                    .message()
                    .content()
                    .get(0)
                    .text();
            
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Bedrock invocation completed in {}ms", duration);
            
            return aiResponse.trim();
            
        } catch (Exception e) {
            log.error("Error invoking Bedrock model", e);
            throw new RuntimeException("Failed to invoke Bedrock: " + e.getMessage(), e);
        }
    }
}
