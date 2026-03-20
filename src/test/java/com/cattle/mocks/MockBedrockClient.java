package com.cattle.mocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock de BedrockRuntimeClient para tests.
 * Proporciona respuestas predefinidas sin invocar AWS Bedrock real.
 */
public class MockBedrockClient {
    
    private final Map<String, String> responses = new HashMap<>();
    private boolean shouldThrowTimeout = false;
    private boolean shouldThrowError = false;
    
    public MockBedrockClient() {
        // Respuestas por defecto
        responses.put("count", "La finca tiene un total de 15 bovinos.");
        responses.put("pregnancy", "Actualmente hay 3 bovinos preñados en la finca.");
        responses.put("production", "La producción promedio mensual es de 25.5 litros.");
        responses.put("pasture", "Hay 3 potreros disponibles para rotación.");
        responses.put("default", "Basado en la información proporcionada, aquí está la respuesta a su consulta.");
    }
    
    /**
     * Simula invocación de modelo Bedrock
     */
    public String invokeModel(String prompt) throws Exception {
        if (shouldThrowTimeout) {
            throw new Exception("Simulated timeout exception");
        }
        
        if (shouldThrowError) {
            throw new Exception("Simulated Bedrock error");
        }
        
        // Retornar respuesta basada en palabras clave del prompt
        if (prompt.contains("cuánto") || prompt.contains("cantidad") || prompt.contains("total")) {
            return responses.get("count");
        } else if (prompt.contains("preñ") || prompt.contains("gestante")) {
            return responses.get("pregnancy");
        } else if (prompt.contains("producción") || prompt.contains("leche")) {
            return responses.get("production");
        } else if (prompt.contains("potrero")) {
            return responses.get("pasture");
        }
        
        return responses.get("default");
    }
    
    /**
     * Configura respuesta personalizada
     */
    public void setResponse(String key, String response) {
        responses.put(key, response);
    }
    
    /**
     * Simula timeout en la siguiente invocación
     */
    public void simulateTimeout() {
        this.shouldThrowTimeout = true;
    }
    
    /**
     * Simula error en la siguiente invocación
     */
    public void simulateError() {
        this.shouldThrowError = true;
    }
    
    /**
     * Reset del mock a estado inicial
     */
    public void reset() {
        this.shouldThrowTimeout = false;
        this.shouldThrowError = false;
    }
}
