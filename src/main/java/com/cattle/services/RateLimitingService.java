package com.cattle.services;

import com.google.common.util.concurrent.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Rate Limiting por usuario/finca.
 * Implementa el algoritmo Token Bucket usando Guava RateLimiter.
 * Protege contra ataques DoS limitando requests por hora.
 */
@Slf4j
@Service
public class RateLimitingService {

    // Límite de requests por hora por farmId
    @Value("${rate.limit.requests.per.hour:100}")
    private double requestsPerHour;

    // Cache de rate limiters por farmId
    private final Map<String, RateLimiterInfo> limiters = new ConcurrentHashMap<>();

    /**
     * Verifica si se permite una nueva request para el farmId dado.
     * 
     * @param farmId ID de la finca
     * @return true si la request es permitida, false si excede el límite
     */
    public boolean allowRequest(String farmId) {
        if (farmId == null || farmId.isBlank()) {
            farmId = "ANONYMOUS";
        }
        
        RateLimiterInfo limiterInfo = getOrCreateLimiter(farmId);
        boolean allowed = limiterInfo.getRateLimiter().tryAcquire();
        
        if (!allowed) {
            log.warn("Rate limit exceeded for farmId: {}. Limit: {} requests/hour", 
                    farmId, requestsPerHour);
            limiterInfo.incrementRejected();
        } else {
            limiterInfo.incrementAccepted();
        }
        
        return allowed;
    }

    /**
     * Obtiene información del rate limit para un farmId.
     * 
     * @param farmId ID de la finca
     * @return Información de límites y uso
     */
    public RateLimitInfo getRateLimitInfo(String farmId) {
        if (farmId == null || farmId.isBlank()) {
            farmId = "ANONYMOUS";
        }
        
        RateLimiterInfo limiterInfo = limiters.get(farmId);
        
        if (limiterInfo == null) {
            return new RateLimitInfo(
                    (int) requestsPerHour,
                    (int) requestsPerHour,
                    Instant.now().plusSeconds(3600).getEpochSecond()
            );
        }
        
        // Calcular remaining aproximado
        int used = limiterInfo.getAcceptedCount();
        int remaining = Math.max(0, (int) requestsPerHour - used);
        
        return new RateLimitInfo(
                (int) requestsPerHour,
                remaining,
                limiterInfo.getResetTime().getEpochSecond()
        );
    }

    /**
     * Resetea el rate limiter para un farmId específico.
     * Útil para testing o casos especiales.
     * 
     * @param farmId ID de la finca
     */
    public void resetLimiter(String farmId) {
        limiters.remove(farmId);
        log.info("Rate limiter reset for farmId: {}", farmId);
    }

    /**
     * Limpia todos los rate limiters.
     * Útil para mantenimiento.
     */
    public void clearAllLimiters() {
        limiters.clear();
        log.info("All rate limiters cleared");
    }

    /**
     * Obtiene o crea un rate limiter para el farmId.
     */
    private RateLimiterInfo getOrCreateLimiter(String farmId) {
        return limiters.computeIfAbsent(farmId, id -> {
            // Convertir requests/hora a permits/segundo
            double permitsPerSecond = requestsPerHour / 3600.0;
            RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);
            
            log.debug("Created new rate limiter for farmId: {} with {} permits/second", 
                    id, permitsPerSecond);
            
            return new RateLimiterInfo(rateLimiter, Instant.now().plusSeconds(3600));
        });
    }

    /**
     * Información interna del rate limiter por usuario.
     */
    private static class RateLimiterInfo {
        private final RateLimiter rateLimiter;
        private final Instant resetTime;
        private int acceptedCount = 0;
        private int rejectedCount = 0;

        public RateLimiterInfo(RateLimiter rateLimiter, Instant resetTime) {
            this.rateLimiter = rateLimiter;
            this.resetTime = resetTime;
        }

        public RateLimiter getRateLimiter() {
            return rateLimiter;
        }

        public Instant getResetTime() {
            return resetTime;
        }

        public int getAcceptedCount() {
            return acceptedCount;
        }

        public void incrementAccepted() {
            acceptedCount++;
        }

        public void incrementRejected() {
            rejectedCount++;
        }
    }

    /**
     * DTO con información del rate limit para incluir en headers de respuesta.
     */
    @Getter
    @AllArgsConstructor
    public static class RateLimitInfo {
        /**
         * Límite máximo de requests por hora.
         */
        private final int limit;
        
        /**
         * Requests restantes en la ventana actual.
         */
        private final int remaining;
        
        /**
         * Timestamp (epoch seconds) cuando se resetea el límite.
         */
        private final long resetTimestamp;
    }
}
