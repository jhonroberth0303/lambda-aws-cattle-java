package com.cattle.services;

import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.enums.QueryIntent;
import com.cattle.services.chatbot.ChatbotContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio constructor de contexto para enriquecer prompts de Bedrock.
 * <p>
 * Depende únicamente de {@link ChatbotContextProvider}: no conoce BovineQueryService,
 * MilkingQueryService ni PastureQueryService. Spring inyecta automáticamente todos
 * los beans que implementan la interfaz (los 3 QueryService + GeneralContextProvider);
 * el enrutamiento por intención se resuelve una sola vez en el constructor.
 */
@Service
@Slf4j
public class ContextBuilderService {

    private static final int MAX_CONTEXT_LENGTH = 2000;

    private final Map<QueryIntent, ChatbotContextProvider> providersByIntent;
    private final ChatbotContextProvider fallbackProvider;

    public ContextBuilderService(List<ChatbotContextProvider> providers) {
        this.providersByIntent = providers.stream()
                .flatMap(p -> p.supportedIntents().stream().map(i -> Map.entry(i, p)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // GENERAL_QUERY actúa como fallback para intenciones no registradas o null,
        // igual que el "default" del switch original.
        this.fallbackProvider = providersByIntent.get(QueryIntent.GENERAL_QUERY);
    }

    /**
     * Construye el contexto completo basado en la intención detectada.
     */
    public String buildContext(IntentContext intent, String farmId) {
        QueryIntent queryIntent = intent.getIntent();
        log.info("Building context for intent: {} and farmId: {}", queryIntent, farmId);

        try {
            ChatbotContextProvider provider = providersByIntent.getOrDefault(queryIntent, fallbackProvider);

            String context = "=== CONTEXTO DE LA FINCA ===\n\n" + provider.buildContext(intent, farmId);

            String finalContext = truncateContext(context);
            log.info("Context built successfully, length: {}", finalContext.length());
            return finalContext;

        } catch (Exception e) {
            log.error("Error building context for intent: {}", queryIntent, e);
            return "Error al construir el contexto. Por favor intenta nuevamente.";
        }
    }

    /**
     * Trunca el contexto si excede el límite máximo.
     */
    private String truncateContext(String context) {
        if (context.length() <= MAX_CONTEXT_LENGTH) {
            return context;
        }
        log.warn("Context exceeds max length ({} > {}), truncating", context.length(), MAX_CONTEXT_LENGTH);
        return context.substring(0, MAX_CONTEXT_LENGTH - 50) + "\n\n[Contexto truncado...]";
    }

    /**
     * Construye el prompt enriquecido combinando el contexto con la pregunta del usuario.
     */
    public String buildPrompt(String userMessage, String context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Eres un asistente virtual especializado en gestión ganadera. ");
        prompt.append("Tienes acceso a la siguiente información de la finca:\n\n");
        prompt.append(context);
        prompt.append("\n\n");
        prompt.append("Con base en esta información, responde la siguiente pregunta del usuario de manera clara, ");
        prompt.append("precisa y profesional. Usa lenguaje sencillo y datos específicos cuando estén disponibles.\n\n");
        prompt.append("Pregunta del usuario: ");
        prompt.append(userMessage);

        return prompt.toString();
    }
}
