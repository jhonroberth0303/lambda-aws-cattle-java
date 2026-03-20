package com.cattle.events;

import com.cattle.enums.EventType;
import com.cattle.enums.PastureSubstatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para eventos (records)
 * Fase 10 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Events 36% → 70%
 * Tests: 8
 */
@Tag("unit")
@Tag("events")
class PastureEventsTest {

    // ==================== OpenEvent Tests ====================

    @Test
    void openEvent_creation_hasCorrectValues() {
        // Arrange & Act
        OpenEvent event = new OpenEvent("john@farm.com", "LOT-001", 25);

        // Assert
        assertEquals("john@farm.com", event.user());
        assertEquals("LOT-001", event.lotId());
        assertEquals(25, event.animals());
    }

    @Test
    void openEvent_type_returnsOpen() {
        // Arrange & Act
        OpenEvent event = new OpenEvent("user", "lot", 10);

        // Assert
        assertEquals(EventType.OPEN, event.type());
    }

    @Test
    void openEvent_withNullValues_allowsNulls() {
        // Arrange & Act
        OpenEvent event = new OpenEvent(null, null, null);

        // Assert
        assertNull(event.user());
        assertNull(event.lotId());
        assertNull(event.animals());
        assertEquals(EventType.OPEN, event.type());
    }

    // ==================== CloseEvent Tests ====================

    @Test
    void closeEvent_creation_hasCorrectValues() {
        // Arrange & Act
        CloseEvent event = new CloseEvent("jane@farm.com", "LOT-002", 20, 8);

        // Assert
        assertEquals("jane@farm.com", event.user());
        assertEquals("LOT-002", event.lotId());
        assertEquals(20, event.animals());
        assertEquals(8, event.residualCm());
    }

    @Test
    void closeEvent_type_returnsClose() {
        // Arrange & Act
        CloseEvent event = new CloseEvent("user", "lot", 15, 7);

        // Assert
        assertEquals(EventType.CLOSE, event.type());
    }

    @Test
    void closeEvent_withNullValues_allowsNulls() {
        // Arrange & Act
        CloseEvent event = new CloseEvent(null, null, null, null);

        // Assert
        assertNull(event.user());
        assertNull(event.lotId());
        assertNull(event.animals());
        assertNull(event.residualCm());
        assertEquals(EventType.CLOSE, event.type());
    }

    // ==================== MaintenanceSetEvent Tests ====================

    @Test
    void maintenanceSetEvent_creation_hasCorrectValues() {
        // Arrange & Act
        MaintenanceSetEvent event = new MaintenanceSetEvent(
                "admin@farm.com",
                PastureSubstatus.ESTABLECIMIENTO,
                "2026-02-15"
        );

        // Assert
        assertEquals("admin@farm.com", event.user());
        assertEquals(PastureSubstatus.ESTABLECIMIENTO, event.substatus());
        assertEquals("2026-02-15", event.holdUntil());
    }

    @Test
    void maintenanceSetEvent_type_returnsMaintenanceSet() {
        // Arrange & Act
        MaintenanceSetEvent event = new MaintenanceSetEvent(
                "user", 
                PastureSubstatus.FERTILIZACION, 
                "2026-02-15"
        );

        // Assert
        assertEquals(EventType.MAINTENANCE_SET, event.type());
    }

    // ==================== MaintenanceClearEvent Tests ====================

    @Test
    void maintenanceClearEvent_creation_hasCorrectValues() {
        // Arrange & Act
        MaintenanceClearEvent event = new MaintenanceClearEvent("admin@farm.com");

        // Assert
        assertEquals("admin@farm.com", event.user());
    }

    @Test
    void maintenanceClearEvent_type_returnsMaintenanceClear() {
        // Arrange & Act
        MaintenanceClearEvent event = new MaintenanceClearEvent("user");

        // Assert
        assertEquals(EventType.MAINTENANCE_CLEAR, event.type());
    }
}
