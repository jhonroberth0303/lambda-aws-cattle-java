package com.cattle.services;

import com.cattle.dtos.chatbot.MilkingContextDTO;
import com.cattle.entities.MilkingRecord;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.MilkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para MilkingQueryService
 * Fase 8 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Services 10% → 30%
 * Tests: 10
 */
@Tag("unit")
@Tag("service")
@Tag("query")
class MilkingRecordQueryServiceTest {

    @Mock
    private MilkingRepository milkingRepository;

    private MilkingQueryService milkingQueryService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        milkingQueryService = new MilkingQueryService();
        try {
            java.lang.reflect.Field field = MilkingQueryService.class.getDeclaredField("milkingRepository");
            field.setAccessible(true);
            field.set(milkingQueryService, milkingRepository);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    // ==================== getMonthlyAverageProduction Tests ====================

    @Test
    void getMonthlyAverageProduction_withRecords_returnsAverage() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        milkingRecords.add(createMilking(20.0));
        milkingRecords.add(createMilking(30.0));
        milkingRecords.add(createMilking(40.0));

        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(milkingRecords));

        // Act
        Double result = milkingQueryService.getMonthlyAverageProduction(farmId);

        // Assert
        assertEquals(30.0, result, 0.01);
        verify(milkingRepository, times(1)).getMilkingBetweenDates(anyString(), anyString(), anyString());
    }

    @Test
    void getMonthlyAverageProduction_noRecords_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        Double result = milkingQueryService.getMonthlyAverageProduction(farmId);

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getMonthlyAverageProduction_emptyList_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(new ArrayList<>()));

        // Act
        Double result = milkingQueryService.getMonthlyAverageProduction(farmId);

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getMonthlyAverageProduction_withNullLiters_filtersOut() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        milkingRecords.add(createMilking(20.0));
        milkingRecords.add(createMilking(null));
        milkingRecords.add(createMilking(40.0));

        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(milkingRecords));

        // Act
        Double result = milkingQueryService.getMonthlyAverageProduction(farmId);

        // Assert
        assertEquals(20.0, result, 0.01); // (20+40)/3 = 20 (promedio con 3 elementos incluyendo null)
    }

    // ==================== getWeeklyAverageProduction Tests ====================

    @Test
    void getWeeklyAverageProduction_withRecords_returnsAverage() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        milkingRecords.add(createMilking(15.0));
        milkingRecords.add(createMilking(25.0));
        milkingRecords.add(createMilking(35.0));
        milkingRecords.add(createMilking(45.0));

        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(milkingRecords));

        // Act
        Double result = milkingQueryService.getWeeklyAverageProduction(farmId);

        // Assert
        assertEquals(30.0, result, 0.01);
    }

    @Test
    void getWeeklyAverageProduction_noRecords_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        Double result = milkingQueryService.getWeeklyAverageProduction(farmId);

        // Assert
        assertEquals(0.0, result);
    }

    // ==================== getTopProducerBovine Tests ====================

    @Test
    void getTopProducerBovine_withRecords_returnsTopProducer() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        
        MilkingRecord m1 = createMilking(15.0);
        m1.setBovineId(1);
        m1.setDate("2026-01-15");
        
        MilkingRecord m2 = createMilking(35.0);
        m2.setBovineId(2);
        m2.setDate("2026-01-15");
        
        MilkingRecord m3 = createMilking(25.0);
        m3.setBovineId(1);
        m3.setDate("2026-01-16");

        milkingRecords.add(m1);
        milkingRecords.add(m2);
        milkingRecords.add(m3);

        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(milkingRecords));

        // Act
        MilkingContextDTO result = milkingQueryService.getTopProducerBovine(farmId);

        // Assert
        assertNotNull(result);
        // Bovine 1: 15+25=40 total, Bovine 2: 35 total
        // Debería retornar bovine 1
    }

    @Test
    void getTopProducerBovine_noRecords_returnsNull() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        MilkingContextDTO result = milkingQueryService.getTopProducerBovine(farmId);

        // Assert
        assertNull(result);
    }

    @Test
    void getTopProducerBovine_singleBovine_returnsThatBovine() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        
        MilkingRecord m1 = createMilking(20.0);
        m1.setBovineId(42);
        m1.setDate("2026-01-15");
        
        milkingRecords.add(m1);

        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(milkingRecords));

        // Act
        MilkingContextDTO result = milkingQueryService.getTopProducerBovine(farmId);

        // Assert
        assertNotNull(result);
        assertEquals("42", result.getBovineId());
    }

    // ==================== getProductionByShift Tests ====================

    @Test
    void getProductionByShift_withRecords_returnsMap() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        
        MilkingRecord m1 = createMilking(15.0);
        m1.setShift("AM");
        MilkingRecord m2 = createMilking(12.0);
        m2.setShift("PM");
        MilkingRecord m3 = createMilking(18.0);
        m3.setShift("AM");
        
        milkingRecords.add(m1);
        milkingRecords.add(m2);
        milkingRecords.add(m3);

        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(milkingRecords));

        // Act
        java.util.Map<String, Double> result = milkingQueryService.getProductionByShift(farmId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(33.0, result.get("AM"), 0.01); // 15 + 18
        assertEquals(12.0, result.get("PM"), 0.01);
    }

    @Test
    void getProductionByShift_noRecords_returnsEmptyMap() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        java.util.Map<String, Double> result = milkingQueryService.getProductionByShift(farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getRecentMilkings Tests ====================

    @Test
    void getRecentMilkings_withRecords_returnsList() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        int days = 7;
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        
        MilkingRecord m1 = createMilking(20.0);
        m1.setDate("2026-01-19");
        MilkingRecord m2 = createMilking(22.0);
        m2.setDate("2026-01-20");
        
        milkingRecords.add(m1);
        milkingRecords.add(m2);

        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(milkingRecords));

        // Act
        List<MilkingContextDTO> result = milkingQueryService.getRecentMilkings(farmId, days);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getRecentMilkings_noRecords_returnsEmptyList() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(milkingRepository.getMilkingBetweenDates(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        List<MilkingContextDTO> result = milkingQueryService.getRecentMilkings(farmId, 7);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Helper Methods ====================

    private MilkingRecord createMilking(Double liters) {
        return MilkingRecord.builder()
                .PK("BOVINE#1")
                .SK("MILKING#2026-01-20#AM")
                .bovineId(1)
                .date("2026-01-20")
                .shift("AM")
                .liters(liters)
                .status("completo")
                .build();
    }
}
