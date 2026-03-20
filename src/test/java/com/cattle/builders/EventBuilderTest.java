package com.cattle.builders;

import com.cattle.entities.Event;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para EventBuilder
 * Fase 9 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Builders 22% → 80%
 * Tests: 10
 */
@Tag("unit")
@Tag("builder")
class EventBuilderTest {

    @Test
    void build_withRequiredFields_createsEvent() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T10:00:00Z")
                .eventType("GRAZING_START")
                .eventAt("2026-01-20T10:00:00Z")
                .build();

        // Act
        Event result = builder.build();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#pasture#P-01", result.getPk());
        assertEquals("event#2026-01-20T10:00:00Z", result.getSk());
        assertEquals("GRAZING_START", result.getEventType());
        assertEquals("2026-01-20T10:00:00Z", result.getEventAt());
    }

    @Test
    void build_withAllFields_createsCompleteEvent() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T10:00:00Z")
                .gsi1pk("farm#F001#type#GRAZING_END")
                .gsi1sk("2026-01-20T14:00:00Z")
                .eventType("GRAZING_END")
                .eventAt("2026-01-20T14:00:00Z")
                .animals(25)
                .residualCm(8)
                .user("john@farm.com")
                .lotId("LOT-001")
                .build();

        // Act
        Event result = builder.build();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#pasture#P-01", result.getPk());
        assertEquals("event#2026-01-20T10:00:00Z", result.getSk());
        assertEquals("farm#F001#type#GRAZING_END", result.getGsi1pk());
        assertEquals("2026-01-20T14:00:00Z", result.getGsi1sk());
        assertEquals("GRAZING_END", result.getEventType());
        assertEquals("2026-01-20T14:00:00Z", result.getEventAt());
        assertEquals(25, result.getAnimals());
        assertEquals(8, result.getResidualCm());
        assertEquals("john@farm.com", result.getUser());
    }

    @Test
    void build_withoutGsi1pk_generatesDefaultGsi1pk() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T10:00:00Z")
                .eventType("MEASUREMENT")
                .eventAt("2026-01-20T10:00:00Z")
                .build();

        // Act
        Event result = builder.build();

        // Assert
        assertEquals("farm#UNKNOWN#type#MEASUREMENT", result.getGsi1pk());
    }

    @Test
    void build_withoutGsi1sk_generatesDefaultGsi1sk() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T10:00:00Z")
                .eventType("GRAZING_START")
                .eventAt("2026-01-20T10:00:00Z")
                .build();

        // Act
        Event result = builder.build();

        // Assert
        assertEquals("2026-01-20T10:00:00Z", result.getGsi1sk());
    }

    @Test
    void build_withoutPk_throwsException() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .sk("event#2026-01-20T10:00:00Z")
                .eventType("GRAZING_START")
                .eventAt("2026-01-20T10:00:00Z")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(exception.getMessage().contains("pk"));
    }

    @Test
    void build_withoutSk_throwsException() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .eventType("GRAZING_START")
                .eventAt("2026-01-20T10:00:00Z")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(exception.getMessage().contains("sk"));
    }

    @Test
    void build_withoutEventType_throwsException() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T10:00:00Z")
                .eventAt("2026-01-20T10:00:00Z")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(exception.getMessage().contains("eventType"));
    }

    @Test
    void build_withoutEventAt_throwsException() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T10:00:00Z")
                .eventType("GRAZING_START")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(exception.getMessage().contains("eventAt"));
    }

    @Test
    void defaultsForGrazingEnd_setsEventType() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T14:00:00Z")
                .eventAt("2026-01-20T14:00:00Z")
                .build();

        // Act
        builder.defaultsForGrazingEnd();
        Event result = builder.build();

        // Assert
        assertEquals("GRAZING_END", result.getEventType());
    }

    @Test
    void defaultsForGrazingEnd_doesNotOverrideExistingEventType() {
        // Arrange
        EventBuilder builder = EventBuilder.builder()
                .pk("farm#F001#pasture#P-01")
                .sk("event#2026-01-20T14:00:00Z")
                .eventType("CUSTOM_EVENT")
                .eventAt("2026-01-20T14:00:00Z")
                .build();

        // Act
        builder.defaultsForGrazingEnd();
        Event result = builder.build();

        // Assert
        assertEquals("CUSTOM_EVENT", result.getEventType());
    }
}
