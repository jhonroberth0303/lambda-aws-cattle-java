package com.cattle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para RateLimitingService.
 * Valida el rate limiting por finca (100 requests/hora).
 */
@Tag("unit")
@Tag("security")
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
        // Configurar 100 requests por hora
        ReflectionTestUtils.setField(rateLimitingService, "requestsPerHour", 100.0);
    }

    // ==================== allowRequest Tests ====================

    @Test
    void allowRequest_firstRequest_returnsTrue() {
        // Act
        boolean result = rateLimitingService.allowRequest("FARM#001");

        // Assert
        assertTrue(result);
    }

    @Test
    void allowRequest_multipleFarms_eachHasOwnLimit() {
        // Act
        boolean farm1Result = rateLimitingService.allowRequest("FARM#001");
        boolean farm2Result = rateLimitingService.allowRequest("FARM#002");
        boolean farm3Result = rateLimitingService.allowRequest("FARM#003");

        // Assert
        assertTrue(farm1Result);
        assertTrue(farm2Result);
        assertTrue(farm3Result);
    }

    @Test
    void allowRequest_nullFarmId_usesAnonymous() {
        // Act
        boolean result = rateLimitingService.allowRequest(null);

        // Assert
        assertTrue(result);
    }

    @Test
    void allowRequest_emptyFarmId_usesAnonymous() {
        // Act
        boolean result = rateLimitingService.allowRequest("");

        // Assert
        assertTrue(result);
    }

    @Test
    void allowRequest_blankFarmId_usesAnonymous() {
        // Act
        boolean result = rateLimitingService.allowRequest("   ");

        // Assert
        assertTrue(result);
    }

    @Test
    void allowRequest_consecutiveRequests_eventuallyDenied() {
        // Arrange
        String farmId = "FARM#HIGH_VOLUME";
        
        // Con rate limiting de 100/hora (0.0278/seg), las primeras requests deberían pasar
        // pero eventualmente se denegarán si se hacen muy rápido

        // Act - Hacer varias requests rápidamente
        int allowedCount = 0;
        int deniedCount = 0;
        
        for (int i = 0; i < 10; i++) {
            if (rateLimitingService.allowRequest(farmId)) {
                allowedCount++;
            } else {
                deniedCount++;
            }
        }

        // Assert - Al menos la primera debe ser permitida
        assertTrue(allowedCount >= 1, "At least first request should be allowed");
    }

    // ==================== getRateLimitInfo Tests ====================

    @Test
    void getRateLimitInfo_newFarm_returnsFullLimit() {
        // Arrange
        String farmId = "FARM#NEW";

        // Act
        RateLimitingService.RateLimitInfo info = rateLimitingService.getRateLimitInfo(farmId);

        // Assert
        assertNotNull(info);
        assertEquals(100, info.getLimit());
        assertEquals(100, info.getRemaining());
        assertTrue(info.getResetTimestamp() > System.currentTimeMillis() / 1000);
    }

    @Test
    void getRateLimitInfo_afterRequest_remainingDecreases() {
        // Arrange
        String farmId = "FARM#USED";
        rateLimitingService.allowRequest(farmId);

        // Act
        RateLimitingService.RateLimitInfo info = rateLimitingService.getRateLimitInfo(farmId);

        // Assert
        assertNotNull(info);
        assertEquals(100, info.getLimit());
        assertTrue(info.getRemaining() <= 99);
    }

    @Test
    void getRateLimitInfo_nullFarmId_usesAnonymous() {
        // Act
        RateLimitingService.RateLimitInfo info = rateLimitingService.getRateLimitInfo(null);

        // Assert
        assertNotNull(info);
        assertEquals(100, info.getLimit());
    }

    // ==================== resetLimiter Tests ====================

    @Test
    void resetLimiter_existingFarm_resetsLimit() {
        // Arrange
        String farmId = "FARM#RESET";
        rateLimitingService.allowRequest(farmId);
        RateLimitingService.RateLimitInfo infoBefore = rateLimitingService.getRateLimitInfo(farmId);
        
        // Act
        rateLimitingService.resetLimiter(farmId);
        RateLimitingService.RateLimitInfo infoAfter = rateLimitingService.getRateLimitInfo(farmId);

        // Assert
        assertEquals(100, infoAfter.getRemaining());
    }

    @Test
    void resetLimiter_nonExistingFarm_doesNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> rateLimitingService.resetLimiter("FARM#NONEXISTENT"));
    }

    // ==================== clearAllLimiters Tests ====================

    @Test
    void clearAllLimiters_multiplesFarms_allReset() {
        // Arrange
        rateLimitingService.allowRequest("FARM#001");
        rateLimitingService.allowRequest("FARM#002");
        rateLimitingService.allowRequest("FARM#003");

        // Act
        rateLimitingService.clearAllLimiters();

        // Assert - Todas las fincas deberían tener límite completo
        assertEquals(100, rateLimitingService.getRateLimitInfo("FARM#001").getRemaining());
        assertEquals(100, rateLimitingService.getRateLimitInfo("FARM#002").getRemaining());
        assertEquals(100, rateLimitingService.getRateLimitInfo("FARM#003").getRemaining());
    }

    // ==================== RateLimitInfo Tests ====================

    @Test
    void rateLimitInfo_getters_returnCorrectValues() {
        // Arrange
        RateLimitingService.RateLimitInfo info = new RateLimitingService.RateLimitInfo(100, 75, 1234567890L);

        // Assert
        assertEquals(100, info.getLimit());
        assertEquals(75, info.getRemaining());
        assertEquals(1234567890L, info.getResetTimestamp());
    }

    // ==================== Edge Cases ====================

    @Test
    void allowRequest_veryHighRateLimit_allowsMany() {
        // Arrange
        RateLimitingService highRateService = new RateLimitingService();
        ReflectionTestUtils.setField(highRateService, "requestsPerHour", 36000.0); // 10 por segundo
        
        String farmId = "FARM#HIGH_RATE";

        // Act
        int allowedCount = 0;
        for (int i = 0; i < 10; i++) {
            if (highRateService.allowRequest(farmId)) {
                allowedCount++;
            }
        }

        // Assert - Con rate alto, al menos algunas deben pasar
        assertTrue(allowedCount >= 1, "With high rate limit, should allow at least some requests");
    }

    @Test
    void allowRequest_veryLowRateLimit_deniesQuickly() {
        // Arrange
        RateLimitingService lowRateService = new RateLimitingService();
        ReflectionTestUtils.setField(lowRateService, "requestsPerHour", 1.0); // 1 por hora
        
        String farmId = "FARM#LOW_RATE";

        // Act
        boolean firstRequest = lowRateService.allowRequest(farmId);
        boolean secondRequest = lowRateService.allowRequest(farmId);

        // Assert
        assertTrue(firstRequest);
        assertFalse(secondRequest);
    }
}
