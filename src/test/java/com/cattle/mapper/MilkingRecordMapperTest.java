package com.cattle.mapper;

import com.cattle.dtos.MilkingDTO;
import com.cattle.entities.MilkingRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para MilkingMapper
 * Fase 6 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Mappers → 80%
 * Tests: 6
 */
@Tag("unit")
@Tag("mapper")
class MilkingRecordMapperTest {

    private MilkingMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MilkingMapperImpl();
    }

    // ==================== toDTO Tests ====================

    @Test
    void toDTO_validFarmMilking_returnsMilkingDTO() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .pk("BOVINE#1")
                .sk("MILKING#2026-01-20#AM")
                .bovineId(1)
                .date("2026-01-20")
                .shift("AM")
                .liters(20.5)
                .status("completo")
                .observations("Normal")
                .recordedBy("John")
                .createdAt("2026-01-20T08:00:00Z")
                .build();

        // Act
        MilkingDTO result = mapper.toDTO(milkingRecord);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBovineId());
        assertEquals("2026-01-20", result.getDate());
        assertEquals("AM", result.getShift());
        assertEquals(20.5, result.getLiters());
        assertEquals("completo", result.getStatus());
        assertEquals("Normal", result.getObservations());
        assertEquals("John", result.getRecordedBy());
    }

    @Test
    void toDTO_nullFarmMilking_returnsNull() {
        // Act
        MilkingDTO result = mapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDTO_minimalFields_mapsCorrectly() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .bovineId(42)
                .date("2026-01-20")
                .shift("PM")
                .liters(18.0)
                .build();

        // Act
        MilkingDTO result = mapper.toDTO(milkingRecord);

        // Assert
        assertNotNull(result);
        assertEquals(42, result.getBovineId());
        assertEquals("2026-01-20", result.getDate());
        assertEquals("PM", result.getShift());
        assertEquals(18.0, result.getLiters());
    }

    // ==================== toEntity Tests ====================

    @Test
    void toEntity_validMilkingDTO_returnsFarmMilking() {
        // Arrange
        MilkingDTO dto = MilkingDTO.builder()
                .bovineId(5)
                .date("2026-01-20")
                .shift("AM")
                .liters(22.0)
                .status("parcial")
                .observations("Test observation")
                .recordedBy("Jane")
                .build();

        // Act
        MilkingRecord result = mapper.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getBovineId());
        assertEquals("2026-01-20", result.getDate());
        assertEquals("AM", result.getShift());
        assertEquals(22.0, result.getLiters());
        assertEquals("parcial", result.getStatus());
        assertEquals("Test observation", result.getObservations());
        assertEquals("Jane", result.getRecordedBy());
        // PK, SK y createdAt son ignorados en el mapping
        assertNull(result.getPk());
        assertNull(result.getSk());
        assertNull(result.getCreatedAt());
    }

    @Test
    void toEntity_nullDTO_returnsNull() {
        // Act
        MilkingRecord result = mapper.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void roundTrip_entityToDtoToEntity_preservesCoreData() {
        // Arrange
        MilkingRecord original = MilkingRecord.builder()
                .pk("BOVINE#10")
                .sk("MILKING#2026-01-20#PM")
                .bovineId(10)
                .date("2026-01-20")
                .shift("PM")
                .liters(19.5)
                .status("completo")
                .observations("Round trip test")
                .recordedBy("Tester")
                .createdAt("2026-01-20T14:00:00Z")
                .build();

        // Act
        MilkingDTO dto = mapper.toDTO(original);
        MilkingRecord result = mapper.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(original.getBovineId(), result.getBovineId());
        assertEquals(original.getDate(), result.getDate());
        assertEquals(original.getShift(), result.getShift());
        assertEquals(original.getLiters(), result.getLiters());
        assertEquals(original.getStatus(), result.getStatus());
        assertEquals(original.getObservations(), result.getObservations());
        assertEquals(original.getRecordedBy(), result.getRecordedBy());
        // PK, SK y createdAt no se preservan en round trip
        assertNull(result.getPk());
        assertNull(result.getSk());
        assertNull(result.getCreatedAt());
    }
}
