package com.cattle.services.chatbot;

import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.enums.QueryIntent;
import com.cattle.services.ContextBuilderService;

import java.util.Set;

/**
 * Contrato para los componentes que saben construir una sección de contexto
 * del chatbot para uno o más {@link QueryIntent}.
 * <p>
 * Cada dominio (Bovinos, Ordeño, Potreros) implementa esta interfaz directamente
 * en su QueryService existente. {@link ContextBuilderService} no conoce ninguna
 * implementación concreta: solo enruta por {@code supportedIntents()}.
 */
public interface ChatbotContextProvider {

    /**
     * Intenciones que este provider sabe resolver. Debe ser un conjunto fijo
     * (no depende del estado ni de la finca consultada).
     */
    Set<QueryIntent> supportedIntents();

    /**
     * Construye el fragmento de contexto (texto plano, listo para insertarse
     * en el prompt) para la intención dada.
     *
     * @param intent contexto de intención detectada (incluye filtros como género/estado)
     * @param farmId finca sobre la que se consulta
     * @return contexto formateado; nunca null (usar cadena vacía si no hay datos)
     */
    String buildContext(IntentContext intent, String farmId);
}
