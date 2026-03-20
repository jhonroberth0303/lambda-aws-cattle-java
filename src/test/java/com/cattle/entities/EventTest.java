package com.cattle.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Event entity
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Entities
 */
@Tag("unit")
@Tag("entity")
class EventTest {

    @Test
    void event_builder_createsInstance() {
        // Act
        Event event = Event.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("eventAt#2025-09-24T10:00:00Z#GRAZING_END")
                .eventType("GRAZING_END")
                .eventAt("2025-09-24T10:00:00Z")
                .animals(5)
                .residualCm(7)
                .user("jhon")
                .build();

        // Assert
        assertNotNull(event);
        assertEquals("farm#F001#pasture#P-01", event.getPk());
        assertEquals("GRAZING_END", event.getEventType());
        assertEquals(5, event.getAnimals());
    }

    @Test
    void event_noArgsConstructor_createsInstance() {
        // Act
        Event event = new Event();

        // Assert
        assertNotNull(event);
        assertNull(event.getPk());
    }

    @Test
    void event_gsiKeys_setCorrectly() {
        // Arrange
        Event event = Event.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("eventAt#2025-09-24T10:00:00Z#GRAZING_END")
                .gsi1pk("farm#F001#type#GRAZING_END")
                .gsi1sk("2025-09-24T10:00:00Z")
                .build();

        // Assert
        assertEquals("farm#F001#type#GRAZING_END", event.getGsi1pk());
        assertEquals("2025-09-24T10:00:00Z", event.getGsi1sk());
    }

    @Test
    void event_settersAndGetters_work() {
        // Arrange
        Event event = new Event();

        // Act
        event.setPk("farm#F002#pasture#P-02");
        event.setSk("eventAt#2025-10-01T08:00:00Z#GRAZING_START");
        event.setEventType("GRAZING_START");
        event.setEventAt("2025-10-01T08:00:00Z");
        event.setAnimals(10);
        event.setResidualCm(15);
        event.setUser("maria");

        // Assert
        assertEquals("farm#F002#pasture#P-02", event.getPk());
        assertEquals("GRAZING_START", event.getEventType());
        assertEquals(10, event.getAnimals());
        assertEquals("maria", event.getUser());
    }
}
