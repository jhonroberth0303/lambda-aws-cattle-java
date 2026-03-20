package com.cattle.mapper;

import com.cattle.dtos.BovineDTO;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.enums.profiles.BovineOrigin;
import com.cattle.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BovinesMapper
 * Fase 6 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Mappers 1% → 80%
 * Tests: 8
 */
@Tag("unit")
@Tag("mapper")
class BovinesMapperTest {

    private BovinesMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BovinesMapperImpl();
    }

    // ==================== toDTO Tests ====================

    @Test
    void toDTO_validBovine_returnsBovineDTO() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(123);
        bovineIdentityItem.setName("Test Cow");
        bovineIdentityItem.setGender("female");
        bovineIdentityItem.setBreed("Holstein");

        // Act
        BovineDTO result = mapper.toDTO(bovineIdentityItem);

        // Assert
        assertNotNull(result);
        assertEquals(123, result.getBovineId());
        assertEquals("Test Cow", result.getName());
        assertEquals("female", result.getGender());
        assertEquals("Holstein", result.getBreed());
    }

    @Test
    void toDTO_nullBovine_returnsNull() {
        // Act
        BovineDTO result = mapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDTO_allFieldsPopulated_mapsAllFields() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = BovineIdentityItem.builder()
                .bovineId(456)
                .name("Full Cow")
                .gender("female")
                .breed("Jersey")
                .bornDate("2020-01-15")
                .color("brown")
                .origin("BOUGHT")
                .fatherId("100")
                .fatherNameSnapshot("Father Name")
                .motherId("200")
                .motherNameSnapshot("Mother Name")
                .farmId("farm-001")
                .createdAt("2023-01-01T00:00:00Z")
                .updatedAt("2023-12-01T00:00:00Z")
                .build();

        // Act
        BovineDTO result = mapper.toDTO(bovineIdentityItem);

        // Assert
        assertNotNull(result);
        assertEquals(456, result.getBovineId());
        assertEquals("Full Cow", result.getName());
        assertEquals("female", result.getGender());
        assertEquals("Jersey", result.getBreed());
        assertEquals("2020-01-15", result.getBornDate());
        assertEquals("brown", result.getColor());
        assertEquals(BovineOrigin.BOUGHT, result.getOrigin());
        assertEquals("100", result.getFatherId());
        assertEquals("Father Name", result.getFatherNameSnapshot());
        assertEquals("200", result.getMotherId());
        assertEquals("Mother Name", result.getMotherNameSnapshot());
        assertEquals("farm-001", result.getFarmId());
        assertEquals(Instant.parse("2023-01-01T00:00:00Z"), result.getCreatedAt(), "createdAt real: " + result.getCreatedAt());
        assertEquals(Instant.parse("2023-12-01T00:00:00Z"), result.getUpdatedAt(), "updatedAt real: " + result.getUpdatedAt());
    }

    // ==================== toEntity Tests ====================

    @Test
    void toEntity_validBovineDTO_returnsBovine() {
        // Arrange
        BovineDTO dto = BovineDTO.builder()
                .bovineId(789)
                .name("DTO Cow")
                .gender("male")
                .breed("Angus")
                .build();

        // Act
        BovineIdentityItem result = mapper.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(789, result.getBovineId());
        assertEquals("DTO Cow", result.getName());
        assertEquals("male", result.getGender());
        assertEquals("Angus", result.getBreed());
    }

    @Test
    void toEntity_nullDTO_returnsNull() {
        // Act
        BovineIdentityItem result = mapper.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_allFieldsPopulated_mapsAllFields() {
        // Arrange
        BovineDTO dto = BovineDTO.builder()
            .bovineId(999)
            .name("Complete DTO")
            .gender("female")
            .breed("Simmental")
            .bornDate("2021-06-10")
            .color("white")
            .origin(BovineOrigin.BOUGHT)
            .fatherId("300")
            .fatherNameSnapshot("Complete Father")
            .motherId("400")
            .motherNameSnapshot("Complete Mother")
            .farmId("farm-002")
            .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2024-12-01T00:00:00Z"))
            .build();

        // Act
        BovineIdentityItem result = mapper.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(999, result.getBovineId());
        assertEquals("Complete DTO", result.getName());
        assertEquals("female", result.getGender());
        assertEquals("Simmental", result.getBreed());
        assertEquals("2021-06-10", result.getBornDate());
        assertEquals("white", result.getColor());
        assertEquals("BOUGHT", result.getOrigin());
        assertEquals("300", result.getFatherId());
        assertEquals("Complete Father", result.getFatherNameSnapshot());
        assertEquals("400", result.getMotherId());
        assertEquals("Complete Mother", result.getMotherNameSnapshot());
        assertEquals("farm-002", result.getFarmId());
        assertEquals("2024-01-01T00:00:00Z", result.getCreatedAt());
        assertEquals("2024-12-01T00:00:00Z", result.getUpdatedAt());
    }

    @Test
    void roundTrip_entityToDtoToEntity_preservesData() {
        // Arrange
        BovineIdentityItem original = TestDataBuilder.createBovine("farm-001", "cow");
        original.setBovineId(555);
        original.setName("Round Trip Cow");

        // Act
        BovineDTO dto = mapper.toDTO(original);
        BovineIdentityItem result = mapper.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(original.getBovineId(), result.getBovineId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getGender(), result.getGender());
        assertEquals(original.getBreed(), result.getBreed());
    }

    @Test
    void roundTrip_dtoToEntityToDto_preservesData() {
        // Arrange
        BovineDTO original = BovineDTO.builder()
                .bovineId(666)
                .name("DTO Round Trip")
                .gender("male")
                .breed("Hereford")
                .build();

        // Act
        BovineIdentityItem entity = mapper.toEntity(original);
        BovineDTO result = mapper.toDTO(entity);

        // Assert
        assertNotNull(result);
        assertEquals(original.getBovineId(), result.getBovineId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getGender(), result.getGender());
        assertEquals(original.getBreed(), result.getBreed());
    }
}
