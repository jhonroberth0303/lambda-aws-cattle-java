package com.cattle.mapper;

import com.cattle.dtos.PastureDTO;
import com.cattle.entities.Pasture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PasturesMapper
 * Fase 6 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Mappers → 80%
 * Tests: 8
 */
@Tag("unit")
@Tag("mapper")
class PasturesMapperTest {

    private PasturesMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PasturesMapperImpl();
    }

    // ==================== toDTO Tests ====================

    @Test
    void toDTO_validPasture_returnsPastureDTO() {
        // Arrange
        Pasture pasture = Pasture.builder()
                .pk("PASTURE#pasture-1")
                .farmId("farm-001")
                .id("pasture-1")
                .name("Potrero 1")
                .species("Kikuyu")
                .status("DISPONIBLE")
                .substatus("NINGUNO")
                .areaHa(10.5)
                .currentHeightCm(25)
                .lastUseAt("2026-01-10")
                .notes("Test notes")
                .build();

        // Act
        PastureDTO result = mapper.toDTO(pasture);

        // Assert
        assertNotNull(result);
        assertEquals("PASTURE#pasture-1", result.getPk());
        assertEquals("farm-001", result.getFarmId());
        assertEquals("pasture-1", result.getId());
        assertEquals("Potrero 1", result.getName());
        assertEquals("Kikuyu", result.getSpecies());
        assertEquals("DISPONIBLE", result.getStatus());
        assertEquals("NINGUNO", result.getSubstatus());
        assertEquals(10.5, result.getAreaHa());
        assertEquals(25, result.getCurrentHeightCm());
        assertEquals("2026-01-10", result.getLastUseAt());
        assertEquals("Test notes", result.getNotes());
    }

    @Test
    void toDTO_nullPasture_returnsNull() {
        // Act
        PastureDTO result = mapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    // ==================== toEntity Tests ====================

    @Test
    void toEntity_validPastureDTO_returnsPasture() {
        // Arrange
        PastureDTO dto = PastureDTO.builder()
                .pk("PASTURE#pasture-2")
                .farmId("farm-002")
                .id("pasture-2")
                .name("Potrero 2")
                .species("Brachiaria")
                .status("EN_DESCANSO")
                .areaHa(15.0)
                .build();

        // Act
        Pasture result = mapper.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals("PASTURE#pasture-2", result.getPk());
        assertEquals("farm-002", result.getFarmId());
        assertEquals("pasture-2", result.getId());
        assertEquals("Potrero 2", result.getName());
        assertEquals("Brachiaria", result.getSpecies());
        assertEquals("EN_DESCANSO", result.getStatus());
        assertEquals(15.0, result.getAreaHa());
    }

    @Test
    void toEntity_nullDTO_returnsNull() {
        // Act
        Pasture result = mapper.toEntity(null);

        // Assert
        assertNull(result);
    }

    // ==================== toDTOList Tests ====================

    @Test
    void toDTOList_validList_returnsListOfDTOs() {
        // Arrange
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(Pasture.builder()
                .pk("PASTURE#1")
                .farmId("farm-001")
                .name("Potrero 1")
                .build());
        pastures.add(Pasture.builder()
                .pk("PASTURE#2")
                .farmId("farm-001")
                .name("Potrero 2")
                .build());

        // Act
        List<PastureDTO> result = mapper.toDTOList(pastures);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Potrero 1", result.get(0).getName());
        assertEquals("Potrero 2", result.get(1).getName());
    }

    @Test
    void toDTOList_emptyList_returnsEmptyList() {
        // Arrange
        List<Pasture> pastures = new ArrayList<>();

        // Act
        List<PastureDTO> result = mapper.toDTOList(pastures);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDTOList_nullList_returnsNull() {
        // Act
        List<PastureDTO> result = mapper.toDTOList(null);

        // Assert
        assertNull(result);
    }

    // ==================== toEntityList Tests ====================

    @Test
    void toEntityList_validList_returnsListOfEntities() {
        // Arrange
        List<PastureDTO> dtos = new ArrayList<>();
        dtos.add(PastureDTO.builder()
                .pk("PASTURE#3")
                .farmId("farm-002")
                .name("Potrero 3")
                .build());
        dtos.add(PastureDTO.builder()
                .pk("PASTURE#4")
                .farmId("farm-002")
                .name("Potrero 4")
                .build());

        // Act
        List<Pasture> result = mapper.toEntityList(dtos);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Potrero 3", result.get(0).getName());
        assertEquals("Potrero 4", result.get(1).getName());
    }

    @Test
    void toEntityList_emptyList_returnsEmptyList() {
        // Arrange
        List<PastureDTO> dtos = new ArrayList<>();

        // Act
        List<Pasture> result = mapper.toEntityList(dtos);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
