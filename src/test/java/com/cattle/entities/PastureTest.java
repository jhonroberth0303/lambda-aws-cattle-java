package com.cattle.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Pasture Entity
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Entities
 */
@Tag("unit")
@Tag("entities")
class PastureTest {

    // ==================== Builder Tests ====================

    @Test
    void builder_allFields_createsValidEntity() {
        // Arrange & Act
        Pasture pasture = Pasture.builder()
                .pk("PASTURE#farm-001#potrero-1")
                .gsi1pk("kikuyo")
                .gsi2pk("farm-001#OPEN")
                .gsi1sk(20250120)
                .gsi2sk(20250125)
                .farmId("farm-001")
                .id("potrero-1")
                .name("Potrero Norte")
                .species("kikuyo")
                .status("OPEN")
                .substatus(null)
                .areaHa(5.5)
                .currentHeightCm(25)
                .lastUseAt("2025-01-10")
                .notes("Near the river")
                .blockReason(null)
                .establishmentDate("2020-01-01")
                .holdUntil("2025-01-25")
                .build();

        // Assert
        assertEquals("PASTURE#farm-001#potrero-1", pasture.getPk());
        assertEquals("kikuyo", pasture.getGsi1pk());
        assertEquals("farm-001#OPEN", pasture.getGsi2pk());
        assertEquals(20250120, pasture.getGsi1sk());
        assertEquals(20250125, pasture.getGsi2sk());
        assertEquals("farm-001", pasture.getFarmId());
        assertEquals("potrero-1", pasture.getId());
        assertEquals("Potrero Norte", pasture.getName());
        assertEquals("kikuyo", pasture.getSpecies());
        assertEquals("OPEN", pasture.getStatus());
        assertNull(pasture.getSubstatus());
        assertEquals(5.5, pasture.getAreaHa());
        assertEquals(25, pasture.getCurrentHeightCm());
        assertEquals("2025-01-10", pasture.getLastUseAt());
        assertEquals("Near the river", pasture.getNotes());
        assertNull(pasture.getBlockReason());
        assertEquals("2020-01-01", pasture.getEstablishmentDate());
        assertEquals("2025-01-25", pasture.getHoldUntil());
    }

    @Test
    void builder_minimalFields_createsValidEntity() {
        // Arrange & Act
        Pasture pasture = Pasture.builder()
                .pk("PASTURE#farm#pot")
                .id("pot")
                .name("Test")
                .status("CLOSED")
                .build();

        // Assert
        assertEquals("PASTURE#farm#pot", pasture.getPk());
        assertEquals("pot", pasture.getId());
        assertEquals("Test", pasture.getName());
        assertEquals("CLOSED", pasture.getStatus());
        assertNull(pasture.getFarmId());
        assertNull(pasture.getAreaHa());
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        Pasture pasture = new Pasture();

        // Act
        pasture.setPk("PASTURE#updated");
        pasture.setGsi1pk("rye grass");
        pasture.setGsi2pk("farm-002#MAINTENANCE");
        pasture.setGsi1sk(20250201);
        pasture.setGsi2sk(20250210);
        pasture.setFarmId("farm-002");
        pasture.setId("potrero-updated");
        pasture.setName("Updated Pasture");
        pasture.setSpecies("rye grass");
        pasture.setStatus("MAINTENANCE");
        pasture.setSubstatus("FERTILIZING");
        pasture.setAreaHa(10.0);
        pasture.setCurrentHeightCm(15);
        pasture.setLastUseAt("2025-02-01");
        pasture.setNotes("Updated notes");
        pasture.setBlockReason("Maintenance needed");
        pasture.setEstablishmentDate("2021-06-15");
        pasture.setHoldUntil("2025-02-15");

        // Assert
        assertEquals("PASTURE#updated", pasture.getPk());
        assertEquals("rye grass", pasture.getGsi1pk());
        assertEquals("farm-002#MAINTENANCE", pasture.getGsi2pk());
        assertEquals(20250201, pasture.getGsi1sk());
        assertEquals(20250210, pasture.getGsi2sk());
        assertEquals("farm-002", pasture.getFarmId());
        assertEquals("potrero-updated", pasture.getId());
        assertEquals("Updated Pasture", pasture.getName());
        assertEquals("rye grass", pasture.getSpecies());
        assertEquals("MAINTENANCE", pasture.getStatus());
        assertEquals("FERTILIZING", pasture.getSubstatus());
        assertEquals(10.0, pasture.getAreaHa());
        assertEquals(15, pasture.getCurrentHeightCm());
        assertEquals("2025-02-01", pasture.getLastUseAt());
        assertEquals("Updated notes", pasture.getNotes());
        assertEquals("Maintenance needed", pasture.getBlockReason());
        assertEquals("2021-06-15", pasture.getEstablishmentDate());
        assertEquals("2025-02-15", pasture.getHoldUntil());
    }

    @Test
    void noArgsConstructor_createsEmptyEntity() {
        // Act
        Pasture pasture = new Pasture();

        // Assert
        assertNull(pasture.getPk());
        assertNull(pasture.getId());
        assertNull(pasture.getName());
        assertNull(pasture.getStatus());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        // Act
        Pasture pasture = new Pasture(
                "pk", "sk", "gsi1pk", "gsi2pk", 1, 2,
                "farm", "id", "name", "species", "status", "substatus",
                5.0, 20, "lastUse", "notes", "blockReason",
                "establishment", "holdUntil"
        );

        // Assert
        assertEquals("pk", pasture.getPk());
        assertEquals("gsi1pk", pasture.getGsi1pk());
        assertEquals("gsi2pk", pasture.getGsi2pk());
        assertEquals(1, pasture.getGsi1sk());
        assertEquals(2, pasture.getGsi2sk());
        assertEquals("farm", pasture.getFarmId());
        assertEquals("id", pasture.getId());
        assertEquals("name", pasture.getName());
        assertEquals("species", pasture.getSpecies());
        assertEquals("status", pasture.getStatus());
        assertEquals("substatus", pasture.getSubstatus());
        assertEquals(5.0, pasture.getAreaHa());
        assertEquals(20, pasture.getCurrentHeightCm());
    }
}
