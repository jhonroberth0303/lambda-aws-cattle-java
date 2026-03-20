package com.cattle.dtos;

import com.cattle.enums.profiles.BovineOrigin;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BovineDTO
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase DTOs
 * 
 * Cobertura objetivo: DTOs 0% → 50%
 */
@Tag("unit")
@Tag("dtos")
class BovineIdentityItemDTOTest {

    // ==================== Builder Tests ====================

    @Test
    void builder_allFields_createsValidDTO() {
        // Arrange & Act
        BovineDTO dto = BovineDTO.builder()
                .bovineId(123)
                .name("Test Cow")
                .gender("female")
                .breed("Holstein")
                .bornDate("2020-05-15")
                .color("Black and White")
                .origin(BovineOrigin.BORN)
                .fatherId("father-001")
                .fatherNameSnapshot("Father Name")
                .motherId("mother-001")
                .motherNameSnapshot("Mother Name")
                .build();

        // Assert
        assertEquals(123, dto.getBovineId());
        assertEquals("Test Cow", dto.getName());
        assertEquals("female", dto.getGender());
        assertEquals("Holstein", dto.getBreed());
        assertEquals("2020-05-15", dto.getBornDate());
        assertEquals("Black and White", dto.getColor());
        assertEquals("BORN", dto.getOrigin().name());
    }

    @Test
    void builder_minimalFields_createsValidDTO() {
        // Arrange & Act
        BovineDTO dto = BovineDTO.builder()
                .bovineId(1)
                .name("Minimal Cow")
                .bornDate("2022-01-01")
                .build();

        // Assert
        assertEquals(1, dto.getBovineId());
        assertEquals("Minimal Cow", dto.getName());
        assertNull(dto.getGender());
        assertNull(dto.getBreed());
    }

    // ==================== toString Tests ====================

    @Test
    void toString_allFields_containsAllValues() {
        // Arrange
        BovineDTO dto = BovineDTO.builder()
                .bovineId(123)
                .name("Test Cow")
                .gender("female")
                .bornDate("2020-05-15")
                .breed("Holstein")
                .color("Black")
                .origin(BovineOrigin.BORN)
                .build();

        // Act
        String result = dto.toString();
        System.out.println("toString result: " + result);

        // Assert
        assertTrue(result.contains("bovineId=123"));
        assertTrue(result.contains("name=Test Cow"));
        assertTrue(result.contains("gender=female"));
        // assertTrue(result.contains("category:cow")); // category ya no existe
        // assertTrue(result.contains("farmId:farm-001")); // farmId no se setea en este test
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        BovineDTO dto = new BovineDTO();

        // Act
        dto.setBovineId(999);
        dto.setName("Updated Name");
        dto.setGender("male");
        dto.setBreed("Angus");
        dto.setBornDate("2021-03-20");
        dto.setColor("Brown");
        dto.setOrigin(BovineOrigin.BOUGHT);
        dto.setFatherId("new-father");
        dto.setFatherNameSnapshot("New Father");
        dto.setMotherId("new-mother");
        dto.setMotherNameSnapshot("New Mother");
        dto.setCreatedAt(Instant.parse("2025-02-01T00:00:00Z"));

        // Assert
        assertEquals(999, dto.getBovineId());
        assertEquals("Updated Name", dto.getName());
        assertEquals("male", dto.getGender());
        assertEquals("Angus", dto.getBreed());
        assertEquals("2021-03-20", dto.getBornDate());
        assertEquals("Brown", dto.getColor());
        assertEquals("BOUGHT", dto.getOrigin().name());
        assertEquals("new-father", dto.getFatherId());
        assertEquals("New Father", dto.getFatherNameSnapshot());
        assertEquals("new-mother", dto.getMotherId());
        assertEquals("New Mother", dto.getMotherNameSnapshot());
    }

    @Test
    void noArgsConstructor_createsEmptyDTO() {
        // Act
        BovineDTO dto = new BovineDTO();

        // Assert
        assertNull(dto.getBovineId());
        assertNull(dto.getName());
        assertNull(dto.getGender());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        // Arrange
        BreedCompositionDTO breedComp = new BreedCompositionDTO("Holstein", 100);
        List<BreedCompositionDTO> breedList = List.of(breedComp);
        Instant created = Instant.parse("2025-02-01T00:00:00Z");

        // Act
        Instant updated = Instant.parse("2026-01-01T00:00:00Z");
        BovineDTO dto = new BovineDTO(
            1, "farm-001", "Name", "FEMALE", "2020-01-01", "PURE",
            "Holstein", breedList, "Brown",
            BovineOrigin.BORN, "father", "fatherName",
            "mother", "motherName",
            created, updated
        );

        // Assert
        assertEquals(1, dto.getBovineId());
        assertEquals("Name", dto.getName());
        assertEquals("FEMALE", dto.getGender());
        assertEquals("2020-01-01", dto.getBornDate());
        assertEquals("PURE", dto.getBreedType());
        assertEquals("Holstein", dto.getBreed());
        assertEquals(breedList, dto.getBreedComposition());
        assertEquals("Brown", dto.getColor());
        assertEquals(BovineOrigin.BORN, dto.getOrigin());
        assertEquals("father", dto.getFatherId());
        assertEquals("fatherName", dto.getFatherNameSnapshot());
        assertEquals("mother", dto.getMotherId());
        assertEquals("motherName", dto.getMotherNameSnapshot());
        assertEquals("farm-001", dto.getFarmId());
        assertEquals(created, dto.getCreatedAt());
        assertEquals(updated, dto.getUpdatedAt());
    }
}
