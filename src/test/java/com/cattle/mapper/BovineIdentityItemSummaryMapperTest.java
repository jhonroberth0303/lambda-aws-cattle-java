package com.cattle.mapper;

import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.entities.bovines.BovineSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BovineSummaryMapper
 * HU-20260428-deuda-tecnica-summary
 * 
 * Cobertura objetivo: >= 80%
 */
@Tag("unit")
@Tag("fast")
@Tag("mappers")
@DisplayName("BovineSummaryMapper Tests")
class BovineIdentityItemSummaryMapperTest {

    private final BovineSummaryMapper mapper = Mappers.getMapper(BovineSummaryMapper.class);

    // ==================== Helper Methods ====================

    private BovineSummary createCompleteSummary() {
        return BovineSummary.builder()
                .pk("BOVINE#167")
                .sk("SUMMARY")
                .gsi1pk("SUMMARY")
                .gsi1sk("BOVINE#167")
                .bovineId(167)
                .name("Mona")
                .gender("FEMALE")
                .breed("Jersey")
                .bornDate("2022-08-31")
                .farmId("FARM#001")
                .category("COW")
                .status("OPEN")
                .enabled(true)
                .currentLactationId("LACT#01")
                .currentPregnancyId("PREG#2025-07-06")
                .isPregnant(true)
                .pregnancyStatus("ACTIVE")
                .expectedDueDate("2026-04-10")
                .calvingDate(null)
                .isLactating(true)
                .lactationStatus("LACTATING")
                .lactationNumber("3")
                .lactationStartDate("2025-01-25")
                // Nuevos campos calculados
                .reproductiveState("PREGNANT")
                .productiveState("PREGNANT_LACTATING")
                .alerts(Arrays.asList("DRY_OFF_SOON"))
                .daysUntilDue(66)
                .daysInLactation(374)
                .daysSinceCalving(null)
                .updatedAt("2026-02-02T14:34:31.104930846Z")
                .build();
    }

    private BovineSummary createPartialSummary() {
        return BovineSummary.builder()
                .pk("BOVINE#168")
                .sk("SUMMARY")
                .gsi1pk("SUMMARY")
                .gsi1sk("BOVINE#168")
                .bovineId(168)
                .name("Luna")
                .gender("FEMALE")
                .breed("Holstein")
                .bornDate("2023-03-15")
                .farmId("FARM#001")
                .category("HEIFER")
                .status("ACTIVE")
                .enabled(true)
                // Sin datos de preñez ni lactancia
                .isPregnant(false)
                .pregnancyStatus(null)
                .expectedDueDate(null)
                .isLactating(false)
                .lactationStatus(null)
                .lactationNumber(null)
                .lactationStartDate(null)
                // Campos calculados para vaca sin estado reproductivo
                .reproductiveState("OPEN")
                .productiveState("OPEN")
                .alerts(List.of())
                .daysUntilDue(null)
                .daysInLactation(null)
                .daysSinceCalving(null)
                .updatedAt("2026-02-02T10:00:00Z")
                .build();
    }

    private BovineSummary createMinimalSummary() {
        return BovineSummary.builder()
                .pk("BOVINE#169")
                .sk("SUMMARY")
                .bovineId(169)
                .name("Estrella")
                .gender("FEMALE")
                .build();
    }

    // ==================== toDTO Tests ====================

    @Nested
    @DisplayName("toDTO() Tests")
    class ToDTOTests {

        @Test
        @DisplayName("Debe mapear entidad completa a DTO correctamente")
        void toDTO_completeEntity_mapsAllFields() {
            // Arrange
            BovineSummary entity = createCompleteSummary();

            // Act
            BovineSummaryDTO dto = mapper.toDTO(entity);

            // Assert
            assertNotNull(dto);
            assertEquals(167, dto.getBovineId());
            assertEquals("Mona", dto.getName());
            assertEquals("FEMALE", dto.getGender());
            assertEquals("Jersey", dto.getBreed());
            assertEquals("2022-08-31", dto.getBornDate());
            assertEquals("FARM#001", dto.getFarmId());
            assertEquals("COW", dto.getCategory());
            assertEquals("OPEN", dto.getStatus());
            assertTrue(dto.getEnabled());
            assertTrue(dto.getIsPregnant());
            assertEquals("ACTIVE", dto.getPregnancyStatus());
            assertEquals("2026-04-10", dto.getExpectedDueDate());
            assertTrue(dto.getIsLactating());
            assertEquals("LACTATING", dto.getLactationStatus());
            assertEquals("3", dto.getLactationNumber());
            assertEquals("2025-01-25", dto.getLactationStartDate());
            // Nuevos campos calculados
            assertEquals("PREGNANT", dto.getReproductiveState());
            assertEquals("PREGNANT_LACTATING", dto.getProductiveState());
            assertNotNull(dto.getAlerts());
            assertEquals(1, dto.getAlerts().size());
            assertTrue(dto.getAlerts().contains("DRY_OFF_SOON"));
            assertEquals(66, dto.getDaysUntilDue());
            assertEquals(374, dto.getDaysInLactation());
            assertNull(dto.getDaysSinceCalving());
            assertEquals("2026-02-02T14:34:31.104930846Z", dto.getUpdatedAt());
        }

        @Test
        @DisplayName("Debe mapear entidad parcial con nulos correctamente")
        void toDTO_partialEntity_mapsWithNulls() {
            // Arrange
            BovineSummary entity = createPartialSummary();

            // Act
            BovineSummaryDTO dto = mapper.toDTO(entity);

            // Assert
            assertNotNull(dto);
            assertEquals(168, dto.getBovineId());
            assertEquals("Luna", dto.getName());
            assertEquals("HEIFER", dto.getCategory());
            assertFalse(dto.getIsPregnant());
            assertNull(dto.getPregnancyStatus());
            assertNull(dto.getExpectedDueDate());
            assertFalse(dto.getIsLactating());
            assertNull(dto.getLactationStatus());
            assertNull(dto.getLactationNumber());
            assertNull(dto.getLactationStartDate());
            // Campos calculados para vaca OPEN
            assertEquals("OPEN", dto.getReproductiveState());
            assertEquals("OPEN", dto.getProductiveState());
            assertNotNull(dto.getAlerts());
            assertTrue(dto.getAlerts().isEmpty());
            assertNull(dto.getDaysUntilDue());
            assertNull(dto.getDaysInLactation());
            assertNull(dto.getDaysSinceCalving());
        }

        @Test
        @DisplayName("Debe mapear entidad mínima correctamente")
        void toDTO_minimalEntity_mapsWithDefaults() {
            // Arrange
            BovineSummary entity = createMinimalSummary();

            // Act
            BovineSummaryDTO dto = mapper.toDTO(entity);

            // Assert
            assertNotNull(dto);
            assertEquals(169, dto.getBovineId());
            assertEquals("Estrella", dto.getName());
            assertEquals("FEMALE", dto.getGender());
            assertNull(dto.getBreed());
            assertNull(dto.getBornDate());
            assertNull(dto.getFarmId());
            assertNull(dto.getCategory());
            assertNull(dto.getStatus());
            assertNull(dto.getEnabled());
        }

        @Test
        @DisplayName("Debe retornar null cuando la entidad es null")
        void toDTO_nullEntity_returnsNull() {
            // Act
            BovineSummaryDTO dto = mapper.toDTO(null);

            // Assert
            assertNull(dto);
        }
    }

    // ==================== toEntity Tests ====================

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Debe mapear DTO completo a entidad (sin claves)")
        void toEntity_completeDTO_mapsWithoutKeys() {
            // Arrange
            BovineSummaryDTO dto = BovineSummaryDTO.builder()
                    .bovineId(167)
                    .name("Mona")
                    .gender("FEMALE")
                    .breed("Jersey")
                    .bornDate("2022-08-31")
                    .farmId("FARM#001")
                    .category("COW")
                    .status("OPEN")
                    .enabled(true)
                    .isPregnant(true)
                    .pregnancyStatus("ACTIVE")
                    .expectedDueDate("2026-04-10")
                    .isLactating(false)
                    .lactationStatus(null)
                    .lactationNumber(null)
                    .lactationStartDate(null)
                    .updatedAt("2026-02-02T14:34:31Z")
                    .build();

            // Act
            BovineSummary entity = mapper.toEntity(dto);

            // Assert
            assertNotNull(entity);
            assertEquals(167, entity.getBovineId());
            assertEquals("Mona", entity.getName());
            assertEquals("FEMALE", entity.getGender());
            assertEquals("Jersey", entity.getBreed());
            assertEquals("COW", entity.getCategory());
            assertTrue(entity.getIsPregnant());
            assertFalse(entity.getIsLactating());
            // Las claves deben ser null (ignoradas en el mapper)
            assertNull(entity.getPk());
            assertNull(entity.getSk());
            assertNull(entity.getGsi1pk());
            assertNull(entity.getGsi1sk());
        }

        @Test
        @DisplayName("Debe retornar null cuando el DTO es null")
        void toEntity_nullDTO_returnsNull() {
            // Act
            BovineSummary entity = mapper.toEntity(null);

            // Assert
            assertNull(entity);
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Debe manejar strings vacíos correctamente")
        void toDTO_emptyStrings_mapsAsEmptyStrings() {
            // Arrange
            BovineSummary entity = BovineSummary.builder()
                    .bovineId(170)
                    .name("")
                    .gender("")
                    .breed("")
                    .build();

            // Act
            BovineSummaryDTO dto = mapper.toDTO(entity);

            // Assert
            assertNotNull(dto);
            assertEquals("", dto.getName());
            assertEquals("", dto.getGender());
            assertEquals("", dto.getBreed());
        }

        @Test
        @DisplayName("Debe manejar bovineId cero correctamente")
        void toDTO_zeroId_mapsZero() {
            // Arrange
            BovineSummary entity = BovineSummary.builder()
                    .bovineId(0)
                    .name("Test")
                    .build();

            // Act
            BovineSummaryDTO dto = mapper.toDTO(entity);

            // Assert
            assertNotNull(dto);
            assertEquals(0, dto.getBovineId());
        }

        @Test
        @DisplayName("Debe manejar bovineId null correctamente")
        void toDTO_nullId_mapsNull() {
            // Arrange
            BovineSummary entity = BovineSummary.builder()
                    .bovineId(null)
                    .name("Test")
                    .build();

            // Act
            BovineSummaryDTO dto = mapper.toDTO(entity);

            // Assert
            assertNotNull(dto);
            assertNull(dto.getBovineId());
        }

        @Test
        @DisplayName("Debe preservar valores booleanos false")
        void toDTO_falseBooleans_preservesFalse() {
            // Arrange
            BovineSummary entity = BovineSummary.builder()
                    .bovineId(171)
                    .name("Test")
                    .enabled(false)
                    .isPregnant(false)
                    .isLactating(false)
                    .build();

            // Act
            BovineSummaryDTO dto = mapper.toDTO(entity);

            // Assert
            assertNotNull(dto);
            assertFalse(dto.getEnabled());
            assertFalse(dto.getIsPregnant());
            assertFalse(dto.getIsLactating());
        }
    }
}
