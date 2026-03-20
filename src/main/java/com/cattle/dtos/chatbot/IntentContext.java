package com.cattle.dtos.chatbot;

import com.cattle.enums.QueryIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Contexto de intención detectada en una consulta de usuario.
 * Contiene la intención clasificada y las entidades extraídas del mensaje.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentContext {
    
    /**
     * Intención detectada
     */
    private QueryIntent intent;
    
    /**
     * Categoría del bovino: "cow", "bull", "calf", "heifer", "steer"
     */
    private String category;
    
    /**
     * Género: "male", "female"
     */
    private String gender;
    
    /**
     * Estado: "pregnant", "lactating", "dry", "open"
     */
    private String status;
    
    /**
     * Filtros adicionales extraídos del mensaje
     */
    @Builder.Default
    private Map<String, String> filters = new HashMap<>();
    
    /**
     * Score de confianza de la detección (0.0 a 1.0)
     */
    private Double confidenceScore;
}
