package com.cattle.services;

import com.cattle.dtos.chatbot.BovineContextDTO;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.BovineRepository;
import com.cattle.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BovineQueryService
 * Fase 8 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Services 10% → 30%
 * Tests: 12
 */
@Tag("unit")
@Tag("service")
@Tag("query")
class BovineIdentityItemQueryServiceTest {

    @Mock
    private BovineRepository bovineRepository;

    private BovineQueryService bovineQueryService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        bovineQueryService = new BovineQueryService();
        try {
            java.lang.reflect.Field field = BovineQueryService.class.getDeclaredField("bovineRepository");
            field.setAccessible(true);
            field.set(bovineQueryService, bovineRepository);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    // ==================== countAllBovines Tests ====================

    @Test
    void countAllBovines_existingFarm_returnsCount() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        when(bovineRepository.countByFarmId(farmId)).thenReturn(25L);

        // Act
        Long result = bovineQueryService.countAllBovines(farmId);

        // Assert
        assertEquals(25L, result);
        verify(bovineRepository, times(1)).countByFarmId(farmId);
    }

    @Test
    void countAllBovines_emptyFarm_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(bovineRepository.countByFarmId(farmId)).thenReturn(0L);

        // Act
        Long result = bovineQueryService.countAllBovines(farmId);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== countByGender Tests ====================

    @Test
    void countByGender_multipleGenders_returnsMap() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = new ArrayList<>();
        bovineIdentityItems.add(createBovineWithGender("female"));
        bovineIdentityItems.add(createBovineWithGender("female"));
        bovineIdentityItems.add(createBovineWithGender("female"));
        bovineIdentityItems.add(createBovineWithGender("male"));

        when(bovineRepository.findAllByFarmId(farmId)).thenReturn(bovineIdentityItems);

        // Act
        Map<String, Long> result = bovineQueryService.countByGender(farmId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(3L, result.get("female"));
        assertEquals(1L, result.get("male"));
    }

    // ==================== countPregnantBovines Tests ====================

    @Test
    void countPregnantBovines_withPregnantAnimals_returnsCount() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> pregnant = List.of(
                TestDataBuilder.createBovine(farmId, "cow"),
                TestDataBuilder.createBovine(farmId, "cow"),
                TestDataBuilder.createBovine(farmId, "cow")
        );
        when(bovineRepository.findByFarmIdAndStatus(farmId, "PREGNANT")).thenReturn(pregnant);

        // Act
        Long result = bovineQueryService.countPregnantBovines(farmId);

        // Assert
        assertEquals(3L, result);
    }

    @Test
    void countPregnantBovines_noPregnant_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        when(bovineRepository.findByFarmIdAndStatus(farmId, "PREGNANT")).thenReturn(new ArrayList<>());

        // Act
        Long result = bovineQueryService.countPregnantBovines(farmId);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== getAgeDistribution Tests ====================

    @Test
    void getAgeDistribution_variousAges_returnsDistribution() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = new ArrayList<>();
        bovineIdentityItems.add(createBovineWithBornDate("2025-10-01")); // ~3 meses
        bovineIdentityItems.add(createBovineWithBornDate("2025-06-01")); // ~7 meses
        bovineIdentityItems.add(createBovineWithBornDate("2024-06-01")); // ~19 meses
        bovineIdentityItems.add(createBovineWithBornDate("2022-01-01")); // ~36+ meses

        when(bovineRepository.findAllByFarmId(farmId)).thenReturn(bovineIdentityItems);

        // Act
        Map<String, Integer> result = bovineQueryService.getAgeDistribution(farmId);

        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.values().stream().mapToInt(Integer::intValue).sum() == 4);
    }

    @Test
    void getAgeDistribution_withNullDates_ignores() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = new ArrayList<>();
        bovineIdentityItems.add(createBovineWithBornDate("2025-01-01"));
        bovineIdentityItems.add(createBovineWithBornDate(null));

        when(bovineRepository.findAllByFarmId(farmId)).thenReturn(bovineIdentityItems);

        // Act
        Map<String, Integer> result = bovineQueryService.getAgeDistribution(farmId);

        // Assert
        assertEquals(1, result.values().stream().mapToInt(Integer::intValue).sum());
    }

    // ==================== getCalvesForWeaning Tests ====================

    @Test
    void getCalvesForWeaning_calvesInRange_returnsFiltered() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> calves = new ArrayList<>();

        LocalDate today = LocalDate.now();
        String fiveMonthsAgo = today.minus(5, ChronoUnit.MONTHS).toString();
        String sevenMonthsAgo = today.minus(7, ChronoUnit.MONTHS).toString();
        String twoMonthsAgo = today.minus(2, ChronoUnit.MONTHS).toString();

        calves.add(createBovineWithBornDate(fiveMonthsAgo)); // ~5 meses
        calves.add(createBovineWithBornDate(sevenMonthsAgo)); // ~7 meses
        calves.add(createBovineWithBornDate(twoMonthsAgo)); // ~2 meses - fuera de rango

        when(bovineRepository.findByFarmIdAndCategory(farmId, "calf")).thenReturn(calves);

        // Act
        List<BovineContextDTO> result = bovineQueryService.getCalvesForWeaning(farmId);

        // Assert
        assertEquals(2, result.size());
    }

    // ==================== getLactatingBovines Tests ====================

    @Test
    void getLactatingBovines_withLactating_returnsContextDTOs() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> lactating = List.of(
                TestDataBuilder.createBovine(farmId, "cow"),
                TestDataBuilder.createBovine(farmId, "cow")
        );
        when(bovineRepository.findByFarmIdAndStatus(farmId, "LACTATING")).thenReturn(lactating);

        // Act
        List<BovineContextDTO> result = bovineQueryService.getLactatingBovines(farmId);

        // Assert
        assertEquals(2, result.size());
        assertNotNull(result.get(0).getBovineId());
    }

    // ==================== getAllBovinesDetails Tests ====================


    // ==================== Helper Methods ====================

    private BovineIdentityItem createBovineWithGender(String gender) {
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setGender(gender);
        return bovineIdentityItem;
    }

    private BovineIdentityItem createBovineWithBornDate(String bornDate) {
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBornDate(bornDate);
        return bovineIdentityItem;
    }

}
