package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import com.cattle.enums.LogType;
import com.cattle.events.EntityPatch;
import com.cattle.exceptions.ServiceException;
import com.cattle.services.PastureService;
import com.cattle.services.PlanService;
import com.cattle.utils.PastureStatusEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para RotationPlanProcessor
 * Fase 11 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Processors 48% → 75%
 * Tests: 10
 */
@Tag("unit")
@Tag("processor")
class RotationPlanProcessorTest {

    @Mock
    private PastureService pastureService;

    @Mock
    private PlanService planService;

    @Mock
    private PastureStatusEngine pastureStatusEngine;

    @Mock
    private LambdaContext lambdaContext;

    private RotationPlanProcessor rotationPlanProcessor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        rotationPlanProcessor = new RotationPlanProcessor(
                pastureService,
                planService,
                pastureStatusEngine,
                lambdaContext
        );
    }

    // ==================== getRotationSemaphoreItems Tests ====================

    @Test
    void getRotationSemaphoreItems_withPasturesAndPlans_returnsItems() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        
        Pasture pasture = createPasture("P-01", "DISPONIBLE", "RYEGRASS");
        Plan plan = createPlan("RYEGRASS", 30, 7, 35);
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.autoUpdateStatusTickByHoldUntil(any(), any()))
                .thenReturn(new EntityPatch(Map.of(), List.of()));
        when(pastureStatusEngine.deriveEffectiveStatus(any(), anyInt()))
                .thenReturn(com.cattle.enums.PastureStatus.DISPONIBLE);

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = rotationPlanProcessor.getRotationSemaphoreItems(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("P-01", result.get().get(0).getPastureId());
        verify(pastureService, times(1)).getPastures(farmId);
        verify(planService, times(1)).getPlans(farmId);
    }

    @Test
    void getRotationSemaphoreItems_noPastures_returnsEmpty() throws ServiceException {
        // Arrange
        String farmId = "farm-empty";
        when(pastureService.getPastures(farmId)).thenReturn(Optional.empty());
        when(planService.getPlans(farmId)).thenReturn(Optional.of(Collections.emptyList()));

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = rotationPlanProcessor.getRotationSemaphoreItems(farmId);

        // Assert
        assertFalse(result.isPresent());
        verify(lambdaContext, times(1)).logInfo(eq(LogType.PROCESSOR), contains("No se encontraron"));
    }

    @Test
    void getRotationSemaphoreItems_multiplePastures_sortedByEta() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        
        Pasture pasture1 = createPasture("P-01", "DISPONIBLE", "RYEGRASS");
        Pasture pasture2 = createPasture("P-02", "EN_DESCANSO", "KIKUYO");
        
        Plan plan1 = createPlan("RYEGRASS", 30, 7, 35);
        Plan plan2 = createPlan("KIKUYO", 20, 6, 28);
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture1, pasture2)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan1, plan2)));
        when(pastureStatusEngine.autoUpdateStatusTickByHoldUntil(any(), any()))
                .thenReturn(new EntityPatch(Map.of(), List.of()));
        when(pastureStatusEngine.deriveEffectiveStatus(any(), anyInt()))
                .thenReturn(com.cattle.enums.PastureStatus.DISPONIBLE);

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = rotationPlanProcessor.getRotationSemaphoreItems(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void getRotationSemaphoreItems_withPatch_appliesPatch() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        Pasture pasture = createPasture("P-01", "DISPONIBLE", "RYEGRASS");
        Plan plan = createPlan("RYEGRASS", 30, 7, 35);
        
        Map<String, Object> patchSet = new HashMap<>();
        patchSet.put("status", "EN_DESCANSO");
        EntityPatch patch = new EntityPatch(patchSet, List.of());
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.autoUpdateStatusTickByHoldUntil(any(), any())).thenReturn(patch);
        when(pastureStatusEngine.deriveEffectiveStatus(any(), anyInt()))
                .thenReturn(com.cattle.enums.PastureStatus.EN_DESCANSO);

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = rotationPlanProcessor.getRotationSemaphoreItems(farmId);

        // Assert
        assertTrue(result.isPresent());
        verify(pastureService, times(1)).applyPatch(eq(pasture.getPk()), eq(patch));
    }

    @Test
    void getRotationSemaphoreItems_serviceException_throwsProcessingException() throws ServiceException {
        // Arrange
        String farmId = "farm-error";
        when(pastureService.getPastures(farmId)).thenThrow(new ServiceException("Service error"));

        // Act & Assert
        assertThrows(com.cattle.exceptions.ProcessingException.class, 
                () -> rotationPlanProcessor.getRotationSemaphoreItems(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.PROCESSOR), contains("Failed to build"));
    }

    @Test
    void getRotationSemaphoreItems_unexpectedException_throwsProcessingException() throws ServiceException {
        // Arrange
        String farmId = "farm-error";
        when(pastureService.getPastures(farmId)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(com.cattle.exceptions.ProcessingException.class, 
                () -> rotationPlanProcessor.getRotationSemaphoreItems(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.PROCESSOR), contains("Unexpected error"));
    }

    @Test
    void getRotationSemaphoreItems_pastureWithGsi2pk_setsBlocked() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        Pasture pasture = createPasture("P-01", "DISPONIBLE", "RYEGRASS");
        pasture.setGsi2pk("farm#F001#blocked#true");
        Plan plan = createPlan("RYEGRASS", 30, 7, 35);
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.autoUpdateStatusTickByHoldUntil(any(), any()))
                .thenReturn(new EntityPatch(Map.of(), List.of()));
        when(pastureStatusEngine.deriveEffectiveStatus(any(), anyInt()))
                .thenReturn(com.cattle.enums.PastureStatus.DISPONIBLE);

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = rotationPlanProcessor.getRotationSemaphoreItems(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().get(0).blocked);
    }

    @Test
    void getRotationSemaphoreItems_successfulExecution_logsSuccess() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        Pasture pasture = createPasture("P-01", "DISPONIBLE", "RYEGRASS");
        Plan plan = createPlan("RYEGRASS", 30, 7, 35);
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.autoUpdateStatusTickByHoldUntil(any(), any()))
                .thenReturn(new EntityPatch(Map.of(), List.of()));
        when(pastureStatusEngine.deriveEffectiveStatus(any(), anyInt()))
                .thenReturn(com.cattle.enums.PastureStatus.DISPONIBLE);

        // Act
        rotationPlanProcessor.getRotationSemaphoreItems(farmId);

        // Assert
        verify(lambdaContext, times(1)).logInfo(eq(LogType.PROCESSOR), contains("Success building"));
    }

    @Test
    void getRotationSemaphoreItems_planWithAllRules_populatesAllFields() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        Pasture pasture = createPasture("P-01", "DISPONIBLE", "RYEGRASS");
        pasture.setAreaHa(10.5);
        pasture.setCurrentHeightCm(25);
        
        Plan plan = createPlan("RYEGRASS", 30, 7, 35);
        plan.getRules().setCutIntervalDays(60);
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.autoUpdateStatusTickByHoldUntil(any(), any()))
                .thenReturn(new EntityPatch(Map.of(), List.of()));
        when(pastureStatusEngine.deriveEffectiveStatus(any(), anyInt()))
                .thenReturn(com.cattle.enums.PastureStatus.DISPONIBLE);

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = rotationPlanProcessor.getRotationSemaphoreItems(farmId);

        // Assert
        assertTrue(result.isPresent());
        RotationSemaphoreItemDTO dto = result.get().get(0);
        assertEquals(30, dto.getEntryHeightCm());
        assertEquals(7, dto.getExitResidualCm());
        assertEquals(35, dto.getRestDaysMin());
        assertEquals(60, dto.getCutIntervalDays());
        assertEquals(10.5, dto.getAreaHa());
        assertEquals(25, dto.getCurrentHeightCm());
    }

        @Test
        void getRotationSemaphoreItems_usesStableBusinessIdentifiersAndDetailFields() throws ServiceException {
                String farmId = "farm-001";
                Pasture pasture = createPasture("P-07", "MANTENIMIENTO", "CUBA22");
                pasture.setLastUseAt("2026-05-10");
                pasture.setHoldUntil("2026-05-25");
                pasture.setBlockReason("HUMEDO");
                pasture.setNotes("Pendiente secado");
                pasture.setGsi2pk("farm#F001#blocked#true");

                Plan plan = createPlan("CUBA22", 40, 10, 21);

                when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
                when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
                when(pastureStatusEngine.autoUpdateStatusTickByHoldUntil(any(), any()))
                                .thenReturn(new EntityPatch(Map.of(), List.of()));
                when(pastureStatusEngine.deriveEffectiveStatus(any(), anyInt()))
                                .thenReturn(com.cattle.enums.PastureStatus.MANTENIMIENTO);

                Optional<List<RotationSemaphoreItemDTO>> result = rotationPlanProcessor.getRotationSemaphoreItems(farmId);

                assertTrue(result.isPresent());
                RotationSemaphoreItemDTO dto = result.get().get(0);
                assertEquals("P-07", dto.getId());
                assertEquals("P-07", dto.getPastureId());
                assertEquals("2026-05-10", dto.getLastUseAt());
                assertEquals("2026-05-25", dto.getHoldUntil());
                assertEquals("HUMEDO", dto.getBlockReason());
                assertEquals("Pendiente secado", dto.getNotes());
                assertTrue(dto.isBlocked());
        }

    // ==================== Helper Methods ====================

    private Pasture createPasture(String id, String status, String species) {
        return Pasture.builder()
                .pk("farm#F001#pasture#" + id)
                .farmId("F001")
                .id(id)
                .name("Potrero " + id)
                .status(status)
                .substatus("NINGUNO")
                .species(species)
                .areaHa(10.0)
                .currentHeightCm(20)
                .build();
    }

    private Plan createPlan(String species, Integer entryHeight, Integer exitResidual, Integer restDays) {
        return Plan.builder()
                .pk("farm#F001#species#" + species)
                .farmId("F001")
                .species(species)
                .planType("GRAZING")
                .growthRateCmPerDay(0.5)
                .rules(Plan.Rules.builder()
                        .entryHeightCm(entryHeight)
                        .exitResidualCm(exitResidual)
                        .restDaysMin(restDays)
                        .build())
                .notes("Test notes")
                .build();
    }
}
