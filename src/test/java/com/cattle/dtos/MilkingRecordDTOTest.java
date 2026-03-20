package com.cattle.dtos;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para MilkingDTO
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase DTOs
 */
@Tag("unit")
@Tag("dtos")
class MilkingRecordDTOTest {

    // ==================== Builder Tests ====================

    @Test
    void builder_allFields_createsValidDTO() {
        // Arrange & Act
        MilkingDTO dto = MilkingDTO.builder()
                .bovineId(123)
                .date("2025-01-15")
                .shift("AM")
                .liters(12.5)
                .status("completo")
                .observations("Normal milking")
                .recordedBy("user-001")
                .build();

        // Assert
        assertEquals(123, dto.getBovineId());
        assertEquals("2025-01-15", dto.getDate());
        assertEquals("AM", dto.getShift());
        assertEquals(12.5, dto.getLiters());
        assertEquals("completo", dto.getStatus());
        assertEquals("Normal milking", dto.getObservations());
        assertEquals("user-001", dto.getRecordedBy());
    }

    @Test
    void builder_minimalFields_createsValidDTO() {
        // Arrange & Act
        MilkingDTO dto = MilkingDTO.builder()
                .bovineId(1)
                .date("2025-01-01")
                .shift("PM")
                .liters(8.0)
                .build();

        // Assert
        assertEquals(1, dto.getBovineId());
        assertEquals("2025-01-01", dto.getDate());
        assertEquals("PM", dto.getShift());
        assertEquals(8.0, dto.getLiters());
        assertNull(dto.getStatus());
        assertNull(dto.getObservations());
    }

    // ==================== toString Tests ====================

    @Test
    void toString_allFields_containsAllValues() {
        // Arrange
        MilkingDTO dto = MilkingDTO.builder()
                .bovineId(123)
                .date("2025-01-15")
                .shift("AM")
                .liters(12.5)
                .status("completo")
                .observations("Test observation")
                .recordedBy("user-001")
                .build();

        // Act
        String result = dto.toString();

        // Assert
        assertTrue(result.contains("bovineId:123"));
        assertTrue(result.contains("date:2025-01-15"));
        assertTrue(result.contains("shift:AM"));
        assertTrue(result.contains("liters:12.5"));
        assertTrue(result.contains("status:completo"));
        assertTrue(result.contains("observations:Test observation"));
        assertTrue(result.contains("recordedBy:user-001"));
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        MilkingDTO dto = new MilkingDTO();

        // Act
        dto.setBovineId(999);
        dto.setDate("2025-02-20");
        dto.setShift("PM");
        dto.setLiters(15.0);
        dto.setStatus("parcial");
        dto.setObservations("Updated observations");
        dto.setRecordedBy("user-002");

        // Assert
        assertEquals(999, dto.getBovineId());
        assertEquals("2025-02-20", dto.getDate());
        assertEquals("PM", dto.getShift());
        assertEquals(15.0, dto.getLiters());
        assertEquals("parcial", dto.getStatus());
        assertEquals("Updated observations", dto.getObservations());
        assertEquals("user-002", dto.getRecordedBy());
    }

    @Test
    void noArgsConstructor_createsEmptyDTO() {
        // Act
        MilkingDTO dto = new MilkingDTO();

        // Assert
        assertNull(dto.getBovineId());
        assertNull(dto.getDate());
        assertNull(dto.getShift());
        assertNull(dto.getLiters());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        // Act
        MilkingDTO dto = new MilkingDTO(123, "2025-01-15", "AM", 12.5, "completo", "notes", "user", 1);

        // Assert
        assertEquals(123, dto.getBovineId());
        assertEquals("2025-01-15", dto.getDate());
        assertEquals("AM", dto.getShift());
        assertEquals(12.5, dto.getLiters());
        assertEquals("completo", dto.getStatus());
        assertEquals("notes", dto.getObservations());
        assertEquals("user", dto.getRecordedBy());
        assertEquals(1, dto.getLactationNumber());
    }
}
