package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de Audit Logging estructurado para operaciones de seguridad.
 * Registra eventos en formato JSON para integración con CloudWatch Insights.
 * NO loguea datos sensibles (passwords, tokens completos, PII).
 */
@Slf4j
@Service
public class AuditLoggingService {

    @Autowired
    private LambdaContext lambdaContext;

    /**
     * Registra un evento de chat del chatbot.
     * 
     * @param farmId ID de la finca
     * @param userId ID del usuario
     * @param intent Intent detectado
     * @param durationMs Duración en milisegundos
     * @param status Estado de la operación (SUCCESS, ERROR)
     */
    public void logChatEvent(String farmId, String userId, String intent, long durationMs, String status) {
        Map<String, Object> event = createBaseEvent("CHAT_REQUEST");
        event.put("farmId", sanitize(farmId));
        event.put("userId", sanitize(userId));
        event.put("intent", sanitize(intent));
        event.put("durationMs", durationMs);
        event.put("status", status);
        
        logEvent(event);
    }

    /**
     * Registra un intento de autenticación.
     * 
     * @param userId ID del usuario (o IP si no está autenticado)
     * @param success true si la autenticación fue exitosa
     * @param reason Razón del fallo (si aplica)
     */
    public void logAuthenticationAttempt(String userId, boolean success, String reason) {
        Map<String, Object> event = createBaseEvent("AUTHENTICATION");
        event.put("userId", sanitize(userId));
        event.put("success", success);
        if (!success && reason != null) {
            event.put("failureReason", sanitize(reason));
        }
        
        // Usar logInfo para ambos casos (LambdaContext no tiene logWarn)
        lambdaContext.logInfo(LogType.SECURITY, formatAsJson(event));
    }

    /**
     * Registra cuando se excede el rate limit.
     * 
     * @param farmId ID de la finca
     * @param limit Límite configurado
     */
    public void logRateLimitExceeded(String farmId, int limit) {
        Map<String, Object> event = createBaseEvent("RATE_LIMIT_EXCEEDED");
        event.put("farmId", sanitize(farmId));
        event.put("limit", limit);
        event.put("severity", "WARNING");
        
        lambdaContext.logInfo(LogType.SECURITY, formatAsJson(event));
    }

    /**
     * Registra un evento de seguridad genérico.
     * 
     * @param eventType Tipo de evento
     * @param farmId ID de la finca
     * @param details Detalles adicionales
     */
    public void logSecurityEvent(String eventType, String farmId, String details) {
        Map<String, Object> event = createBaseEvent(eventType);
        event.put("farmId", sanitize(farmId));
        event.put("details", sanitize(details));
        
        lambdaContext.logInfo(LogType.SECURITY, formatAsJson(event));
    }

    /**
     * Registra un error de seguridad.
     * 
     * @param eventType Tipo de evento
     * @param farmId ID de la finca
     * @param errorMessage Mensaje de error (sin stack trace)
     */
    public void logSecurityError(String eventType, String farmId, String errorMessage) {
        Map<String, Object> event = createBaseEvent(eventType);
        event.put("farmId", sanitize(farmId));
        event.put("error", sanitize(errorMessage));
        event.put("severity", "ERROR");
        
        lambdaContext.logInfo(LogType.SECURITY, formatAsJson(event));
    }

    /**
     * Registra un intento de input malicioso detectado.
     * 
     * @param farmId ID de la finca
     * @param inputPreview Primeros 50 caracteres del input (sanitizado)
     * @param detectedPattern Patrón detectado
     */
    public void logMaliciousInputDetected(String farmId, String inputPreview, String detectedPattern) {
        Map<String, Object> event = createBaseEvent("MALICIOUS_INPUT_DETECTED");
        event.put("farmId", sanitize(farmId));
        event.put("inputPreview", sanitize(inputPreview.substring(0, Math.min(50, inputPreview.length()))));
        event.put("detectedPattern", sanitize(detectedPattern));
        event.put("severity", "WARNING");
        
        lambdaContext.logInfo(LogType.SECURITY, formatAsJson(event));
    }

    /**
     * Registra acceso a datos de otra finca (intento de broken access control).
     * 
     * @param requestingFarmId FarmId del solicitante
     * @param targetFarmId FarmId del recurso solicitado
     * @param resource Recurso solicitado
     */
    public void logAccessControlViolation(String requestingFarmId, String targetFarmId, String resource) {
        Map<String, Object> event = createBaseEvent("ACCESS_CONTROL_VIOLATION");
        event.put("requestingFarmId", sanitize(requestingFarmId));
        event.put("targetFarmId", sanitize(targetFarmId));
        event.put("resource", sanitize(resource));
        event.put("severity", "CRITICAL");
        
        lambdaContext.logInfo(LogType.SECURITY, formatAsJson(event));
    }

    /**
     * Crea un evento base con campos comunes.
     */
    private Map<String, Object> createBaseEvent(String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("timestamp", Instant.now().toString());
        event.put("eventType", eventType);
        event.put("service", "cattle-chatbot");
        return event;
    }

    /**
     * Loguea un evento como JSON.
     */
    private void logEvent(Map<String, Object> event) {
        lambdaContext.logInfo(LogType.SECURITY, formatAsJson(event));
    }

    /**
     * Formatea un mapa como JSON simple.
     */
    private String formatAsJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else if (value == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Escapa caracteres especiales para JSON.
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Sanitiza un valor para logging (remueve info sensible).
     */
    private String sanitize(String value) {
        if (value == null) {
            return "null";
        }
        // Truncar valores muy largos
        if (value.length() > 200) {
            return value.substring(0, 200) + "...";
        }
        return value;
    }
}
