package com.cattle.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PingController
 * Fase 7 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Controllers 35% → 75%
 * Tests: 3
 */
@Tag("unit")
@Tag("controller")
class PingControllerTest {

    private PingController pingController;

    @BeforeEach
    void setUp() {
        pingController = new PingController();
    }

    @Test
    void ping_returnsCorrectResponse() {
        // Act
        Map<String, String> response = pingController.ping();

        // Assert
        assertNotNull(response);
        assertTrue(response.containsKey("pong"));
        assertEquals("Hello, World!", response.get("pong"));
    }

    @Test
    void ping_returnsNonEmptyMap() {
        // Act
        Map<String, String> response = pingController.ping();

        // Assert
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }

    @Test
    void ping_multipleCallsReturnSameResponse() {
        // Act
        Map<String, String> response1 = pingController.ping();
        Map<String, String> response2 = pingController.ping();

        // Assert
        assertEquals(response1.get("pong"), response2.get("pong"));
        assertEquals("Hello, World!", response1.get("pong"));
        assertEquals("Hello, World!", response2.get("pong"));
    }
}
