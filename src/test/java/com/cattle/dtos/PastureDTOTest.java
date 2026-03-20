package com.cattle.dtos;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PastureDTO
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase DTOs
 */
@Tag("unit")
@Tag("dtos")
class PastureDTOTest {

    // ==================== Builder Tests ====================

    @Test
    void builder_allFields_createsValidDTO() {
        // Arrange & Act
        PastureDTO dto = PastureDTO.builder()
                .pk("PASTURE#farm-001#potrero-1")
                .farmId("farm-001")
                .id("potrero-1")
                .name("Potrero Norte")
                .notes("Near the river")
                .species("kikuyo")
                .status("OPEN")
                .substatus(null)
                .lastUseAt("2025-01-10")
                .areaHa(5.5)
                .daysRest(30)
                .currentHeightCm(25)
                .etaOpenDays(5)
                .gsi1pk("farm-001")
                .gsi2pk("OPEN")
                .gsi1sk(1)
                .gsi2sk(20250115)
                .build();

        // Assert
        assertEquals("PASTURE#farm-001#potrero-1", dto.getPk());
        assertEquals("farm-001", dto.getFarmId());
        assertEquals("potrero-1", dto.getId());
        assertEquals("Potrero Norte", dto.getName());
        assertEquals("Near the river", dto.getNotes());
        assertEquals("kikuyo", dto.getSpecies());
        assertEquals("OPEN", dto.getStatus());
        assertNull(dto.getSubstatus());
        assertEquals("2025-01-10", dto.getLastUseAt());
        assertEquals(5.5, dto.getAreaHa());
        assertEquals(30, dto.getDaysRest());
        assertEquals(25, dto.getCurrentHeightCm());
        assertEquals(5, dto.getEtaOpenDays());
    }

    @Test
    void builder_minimalFields_createsValidDTO() {
        // Arrange & Act
        PastureDTO dto = PastureDTO.builder()
                .id("potrero-1")
                .name("Test Pasture")
                .status("CLOSED")
                .build();

        // Assert
        assertEquals("potrero-1", dto.getId());
        assertEquals("Test Pasture", dto.getName());
        assertEquals("CLOSED", dto.getStatus());
        assertNull(dto.getFarmId());
        assertNull(dto.getAreaHa());
    }

    // ==================== Data Annotation Tests ====================

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        PastureDTO dto = PastureDTO.builder().build();

        // Act - Using setters from @Data
        dto.setPk("PASTURE#updated");
        dto.setFarmId("farm-002");
        dto.setId("potrero-updated");
        dto.setName("Updated Pasture");
        dto.setNotes("Updated notes");
        dto.setSpecies("rye grass");
        dto.setStatus("MAINTENANCE");
        dto.setSubstatus("FERTILIZING");
        dto.setLastUseAt("2025-02-01");
        dto.setAreaHa(10.0);
        dto.setDaysRest(45);
        dto.setCurrentHeightCm(15);
        dto.setEtaOpenDays(10);
        dto.setGsi1pk("farm-002");
        dto.setGsi2pk("MAINTENANCE");
        dto.setGsi1sk(2);
        dto.setGsi2sk(20250201);

        // Assert
        assertEquals("PASTURE#updated", dto.getPk());
        assertEquals("farm-002", dto.getFarmId());
        assertEquals("potrero-updated", dto.getId());
        assertEquals("Updated Pasture", dto.getName());
        assertEquals("Updated notes", dto.getNotes());
        assertEquals("rye grass", dto.getSpecies());
        assertEquals("MAINTENANCE", dto.getStatus());
        assertEquals("FERTILIZING", dto.getSubstatus());
        assertEquals("2025-02-01", dto.getLastUseAt());
        assertEquals(10.0, dto.getAreaHa());
        assertEquals(45, dto.getDaysRest());
        assertEquals(15, dto.getCurrentHeightCm());
        assertEquals(10, dto.getEtaOpenDays());
        assertEquals("farm-002", dto.getGsi1pk());
        assertEquals("MAINTENANCE", dto.getGsi2pk());
        assertEquals(2, dto.getGsi1sk());
        assertEquals(20250201, dto.getGsi2sk());
    }

    @Test
    void equals_sameValues_returnsTrue() {
        // Arrange
        PastureDTO dto1 = PastureDTO.builder()
                .id("potrero-1")
                .name("Test")
                .status("OPEN")
                .build();

        PastureDTO dto2 = PastureDTO.builder()
                .id("potrero-1")
                .name("Test")
                .status("OPEN")
                .build();

        // Assert - @Data provides equals
        assertEquals(dto1, dto2);
    }

    @Test
    void hashCode_sameValues_returnsSameHash() {
        // Arrange
        PastureDTO dto1 = PastureDTO.builder()
                .id("potrero-1")
                .name("Test")
                .build();

        PastureDTO dto2 = PastureDTO.builder()
                .id("potrero-1")
                .name("Test")
                .build();

        // Assert - @Data provides hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void toString_containsAllFields() {
        // Arrange
        PastureDTO dto = PastureDTO.builder()
                .id("potrero-1")
                .name("Test Pasture")
                .status("OPEN")
                .areaHa(5.5)
                .build();

        // Act
        String result = dto.toString();

        // Assert - @Data provides toString
        assertNotNull(result);
        assertTrue(result.contains("potrero-1"));
        assertTrue(result.contains("Test Pasture"));
        assertTrue(result.contains("OPEN"));
        assertTrue(result.contains("5.5"));
    }
}
