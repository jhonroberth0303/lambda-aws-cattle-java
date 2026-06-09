package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Pasture;
import com.cattle.events.EntityPatch;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.PastureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para PastureService
 * Fase 1.3 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: 0% → 95%
 * Tests: 15
 */
@Tag("unit")
@Tag("fast")
@Tag("services")
class PastureServiceTest {

    @Mock
    private PastureRepository pastureRepository;

    @Mock
    private LambdaContext lambdaContext;

    private PastureService pastureService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        pastureService = new PastureService(pastureRepository, lambdaContext);
    }

    // ==================== getPastures Tests ====================

    @Test
    void findPastures_validFarmId_returnsList() {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        
        Pasture pasture = Pasture.builder()
                .pk("PASTURE#pasture-1")
                .farmId(farmId)
                .name("Potrero 1")
                .species("Kikuyu")
                .status("DISPONIBLE")
                .areaHa(10.0)
                .build();
        
        pastures.add(pasture);
        when(pastureRepository.findPastures2(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Optional<List<Pasture>> result = pastureService.getPastures(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Potrero 1", result.get().get(0).getName());
        verify(pastureRepository, times(1)).findPastures2(farmId);
    }

    @Test
    void findPastures_emptyFarm_returnsEmptyList() {
        // Arrange
        String farmId = "farm-empty";
        when(pastureRepository.findPastures2(farmId)).thenReturn(Optional.empty());

        // Act
        Optional<List<Pasture>> result = pastureService.getPastures(farmId);

        // Assert
        assertTrue(result.isEmpty());
        verify(pastureRepository, times(1)).findPastures2(farmId);
    }

    @Test
    void findPastures_nullFarmId_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pastureService.getPastures(null));
        assertTrue(exception.getMessage().contains("farmId"));
        verify(pastureRepository, never()).findPastures2(any());
    }

    @Test
    void findPastures_emptyFarmId_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pastureService.getPastures("  "));
        assertTrue(exception.getMessage().contains("farmId"));
        verify(pastureRepository, never()).findPastures2(any());
    }

    @Test
    void findPastures_repositoryException_throwsServiceException() {
        // Arrange
        String farmId = "farm-001";
        when(pastureRepository.findPastures2(farmId)).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> pastureService.getPastures(farmId));
        verify(pastureRepository, times(1)).findPastures2(farmId);
    }

    @Test
    void findPastures_unexpectedException_throwsProcessingException() {
        String farmId = "farm-001";
        when(pastureRepository.findPastures2(farmId)).thenThrow(new RuntimeException("boom"));

        assertThrows(ProcessingException.class, () -> pastureService.getPastures(farmId));
        verify(pastureRepository).findPastures2(farmId);
    }

    // ==================== applyPatch Tests ====================

    @Test
    void applyEvent_openEvent_changesStatus() {
        // Arrange
        String pk = "PASTURE#pasture-1";
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_USO");
        
        doNothing().when(pastureRepository).applyPatch(eq(pk), any(), eq(patch));

        // Act
        pastureService.applyPatch(pk, null, patch);

        // Assert
        verify(pastureRepository, times(1)).applyPatch(eq(pk), any(), eq(patch));
    }

    @Test
    void applyEvent_closeEvent_updatesOccupancy() {
        // Arrange
        String pk = "PASTURE#pasture-1";
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_DESCANSO");
        patch.set("lastUseAtIso", "2026-01-20T10:00:00Z");
        
        doNothing().when(pastureRepository).applyPatch(eq(pk), any(), eq(patch));

        // Act
        pastureService.applyPatch(pk, null, patch);

        // Assert
        verify(pastureRepository, times(1)).applyPatch(eq(pk), any(), eq(patch));
    }

    @Test
    void applyEvent_maintenanceSet_setsSubstatus() {
        // Arrange
        String pk = "PASTURE#pasture-1";
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "MANTENIMIENTO");
        patch.set("substatus", "FERTILIZACION");
        patch.set("holdUntil", "2026-02-28");
        
        doNothing().when(pastureRepository).applyPatch(eq(pk), any(), eq(patch));

        // Act
        pastureService.applyPatch(pk, null, patch);

        // Assert
        verify(pastureRepository, times(1)).applyPatch(eq(pk), any(), eq(patch));
    }

    @Test
    void applyEvent_maintenanceClear_clearsSubstatus() {
        // Arrange
        String pk = "PASTURE#pasture-1";
        EntityPatch patch = EntityPatch.of();
        patch.set("substatus", "NINGUNO");
        patch.remove("holdUntil");
        
        doNothing().when(pastureRepository).applyPatch(eq(pk), any(), eq(patch));

        // Act
        pastureService.applyPatch(pk, null, patch);

        // Assert
        verify(pastureRepository, times(1)).applyPatch(eq(pk), any(), eq(patch));
    }

    @Test
    void applyPatch_nullPk_throwsException() {
        // Arrange
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_USO");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pastureService.applyPatch(null, null, patch));
        assertTrue(exception.getMessage().contains("pk"));
        verify(pastureRepository, never()).applyPatch(any(), any(), any());
    }

    @Test
    void applyPatch_emptyPatch_throwsException() {
        // Arrange
        String pk = "PASTURE#pasture-1";
        EntityPatch emptyPatch = EntityPatch.of();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pastureService.applyPatch(pk, null, emptyPatch));
        assertTrue(exception.getMessage().contains("patch"));
        verify(pastureRepository, never()).applyPatch(any(), any(), any());
    }

    @Test
    void applyPatch_blankPk_throwsException() {
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_USO");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pastureService.applyPatch("   ", null, patch));
        assertTrue(exception.getMessage().contains("pk"));
    }

    @Test
    void applyPatch_nullPatch_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pastureService.applyPatch("PASTURE#1", null, null));
        assertTrue(exception.getMessage().contains("patch"));
    }

    @Test
    void applyPatch_repositoryException_throwsServiceException() {
        // Arrange
        String pk = "PASTURE#pasture-1";
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_USO");
        
        doThrow(new RepositoryException("DB Error")).when(pastureRepository).applyPatch(eq(pk), any(), eq(patch));

        // Act & Assert
        assertThrows(ServiceException.class, () -> pastureService.applyPatch(pk, null, patch));
        verify(pastureRepository, times(1)).applyPatch(eq(pk), any(), eq(patch));
    }

    @Test
    void applyPatch_unexpectedException_throwsProcessingException() {
        String pk = "PASTURE#pasture-1";
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_USO");
        doThrow(new RuntimeException("boom")).when(pastureRepository).applyPatch(eq(pk), any(), eq(patch));

        assertThrows(ProcessingException.class, () -> pastureService.applyPatch(pk, null, patch));
        verify(pastureRepository).applyPatch(eq(pk), any(), eq(patch));
    }

    @Test
    void getAvailablePastures_filtersCorrectly() {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        
        Pasture available = Pasture.builder()
                .pk("PASTURE#pasture-1")
                .farmId(farmId)
                .status("DISPONIBLE")
                .build();
        
        Pasture maintenance = Pasture.builder()
                .pk("PASTURE#pasture-2")
                .farmId(farmId)
                .status("MANTENIMIENTO")
                .build();
        
        pastures.add(available);
        pastures.add(maintenance);
        
        when(pastureRepository.findPastures2(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Optional<List<Pasture>> result = pastureService.getPastures(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void calculateTotalHectares_sumsCorrectly() {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = new ArrayList<>();
        
        pastures.add(Pasture.builder().farmId(farmId).areaHa(10.0).build());
        pastures.add(Pasture.builder().farmId(farmId).areaHa(15.5).build());
        pastures.add(Pasture.builder().farmId(farmId).areaHa(8.3).build());
        
        when(pastureRepository.findPastures2(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Optional<List<Pasture>> result = pastureService.getPastures(farmId);

        // Assert
        assertTrue(result.isPresent());
        double totalArea = result.get().stream()
                .mapToDouble(Pasture::getAreaHa)
                .sum();
        assertEquals(33.8, totalArea, 0.01);
    }

    @Test
    void autoUpdateByHoldUntil_expired_changesStatus() {
        // Arrange
        String pk = "PASTURE#pasture-1";
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "DISPONIBLE");
        patch.set("substatus", "NINGUNO");
        patch.remove("holdUntil");
        
        doNothing().when(pastureRepository).applyPatch(eq(pk), any(), eq(patch));

        // Act
        pastureService.applyPatch(pk, null, patch);

        // Assert
        verify(pastureRepository, times(1)).applyPatch(eq(pk), any(), eq(patch));
    }
}
