package com.cattle.services;

import com.cattle.dtos.chatbot.PastureContextDTO;
import com.cattle.entities.Pasture;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.PastureRepository;
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
 * Tests unitarios para PastureQueryService
 * Fase 8 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Services 10% → 30%
 * Tests: 10
 */
@Tag("unit")
@Tag("service")
@Tag("query")
class PastureQueryServiceTest {

    @Mock
    private PastureRepository pastureRepository;

    private PastureQueryService pastureQueryService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        pastureQueryService = new PastureQueryService();
        try {
            java.lang.reflect.Field field = PastureQueryService.class.getDeclaredField("pastureRepository");
            field.setAccessible(true);
            field.set(pastureQueryService, pastureRepository);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    // ==================== getAvailablePastures Tests ====================

    @Test
    void getAvailablePastures_withAvailableOnes_returnsFiltered() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("DISPONIBLE", 10.5));
        pastures.add(createPastureWithStatus("EN_USO", 8.0));
        pastures.add(createPastureWithStatus("AVAILABLE", 12.0));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        List<PastureContextDTO> result = pastureQueryService.getAvailablePastures(farmId);

        // Assert
        assertEquals(2, result.size());
        verify(pastureRepository, times(1)).findPastures(farmId);
    }

    @Test
    void getAvailablePastures_noPastures_returnsEmptyList() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.empty());

        // Act
        List<PastureContextDTO> result = pastureQueryService.getAvailablePastures(farmId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailablePastures_allInUse_returnsEmptyList() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("EN_USO", 10.0));
        pastures.add(createPastureWithStatus("OCUPADO", 15.0));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        List<PastureContextDTO> result = pastureQueryService.getAvailablePastures(farmId);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== getPasturesInUse Tests ====================

    @Test
    void getPasturesInUse_withInUseOnes_returnsFiltered() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("EN_USO", 10.5));
        pastures.add(createPastureWithStatus("DISPONIBLE", 8.0));
        pastures.add(createPastureWithStatus("IN_USE", 12.0));
        pastures.add(createPastureWithStatus("OCUPADO", 7.5));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        List<PastureContextDTO> result = pastureQueryService.getPasturesInUse(farmId);

        // Assert
        assertEquals(3, result.size());
    }

    @Test
    void getPasturesInUse_noPastures_returnsEmptyList() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.empty());

        // Act
        List<PastureContextDTO> result = pastureQueryService.getPasturesInUse(farmId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getPasturesInUse_allAvailable_returnsEmptyList() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("DISPONIBLE", 10.0));
        pastures.add(createPastureWithStatus("AVAILABLE", 15.0));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        List<PastureContextDTO> result = pastureQueryService.getPasturesInUse(farmId);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== getTotalHectaresInUse Tests ====================

    @Test
    void getTotalHectaresInUse_withMultiplePastures_returnsSum() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("EN_USO", 10.5));
        pastures.add(createPastureWithStatus("IN_USE", 15.0));
        pastures.add(createPastureWithStatus("DISPONIBLE", 20.0)); // No se cuenta

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Double result = pastureQueryService.getTotalHectaresInUse(farmId);

        // Assert
        assertEquals(25.5, result, 0.01);
    }

    @Test
    void getTotalHectaresInUse_noPasturesInUse_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("DISPONIBLE", 10.0));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Double result = pastureQueryService.getTotalHectaresInUse(farmId);

        // Assert
        assertEquals(0.0, result, 0.01);
    }

    @Test
    void getTotalHectaresInUse_withNullAreas_ignores() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("EN_USO", 10.5));
        pastures.add(createPastureWithStatus("EN_USO", null));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Double result = pastureQueryService.getTotalHectaresInUse(farmId);

        // Assert
        assertEquals(10.5, result, 0.01);
    }

    @Test
    void getTotalHectaresInUse_emptyFarm_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.empty());

        // Act
        Double result = pastureQueryService.getTotalHectaresInUse(farmId);

        // Assert
        assertEquals(0.0, result, 0.01);
    }

    // ==================== getTotalAvailableHectares Tests ====================

    @Test
    void getTotalAvailableHectares_withMultiplePastures_returnsSum() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("DISPONIBLE", 10.5));
        pastures.add(createPastureWithStatus("AVAILABLE", 15.0));
        pastures.add(createPastureWithStatus("EN_USO", 20.0)); // No se cuenta

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Double result = pastureQueryService.getTotalAvailableHectares(farmId);

        // Assert
        assertEquals(25.5, result, 0.01);
    }

    @Test
    void getTotalAvailableHectares_noPasturesAvailable_returnsZero() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("EN_USO", 10.0));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Double result = pastureQueryService.getTotalAvailableHectares(farmId);

        // Assert
        assertEquals(0.0, result, 0.01);
    }

    // ==================== getPastureCountByStatus Tests ====================

    @Test
    void getPastureCountByStatus_withMultipleStatuses_returnsMap() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        pastures.add(createPastureWithStatus("DISPONIBLE", 10.0));
        pastures.add(createPastureWithStatus("DISPONIBLE", 12.0));
        pastures.add(createPastureWithStatus("EN_USO", 8.0));
        pastures.add(createPastureWithStatus("DESCANSO", 15.0));

        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        java.util.Map<String, Integer> result = pastureQueryService.getPastureCountByStatus(farmId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getPastureCountByStatus_noPastures_returnsEmptyMap() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(pastureRepository.findPastures(farmId)).thenReturn(Optional.empty());

        // Act
        java.util.Map<String, Integer> result = pastureQueryService.getPastureCountByStatus(farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Helper Methods ====================

    private Pasture createPastureWithStatus(String status, Double areaHa) {
        return Pasture.builder()
                .pk("PASTURE#pasture-1")
                .farmId("farm-001")
                .id("pasture-1")
                .name("Potrero 1")
                .status(status)
                .areaHa(areaHa)
                .species("Kikuyu")
                .build();
    }
}
