package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Plan;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.PlanRepository;
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
 * Tests unitarios para PlanService
 * Fase 12 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Services 22% → 35%
 * Tests: 8
 */
@Tag("unit")
@Tag("service")
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private LambdaContext lambdaContext;

    private PlanService planService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        planService = new PlanService(planRepository, lambdaContext);
    }

    // ==================== getPlans Tests ====================

    @Test
    void getPlans_validFarmId_returnsPlans() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        List<Plan> plans = new ArrayList<>();
        plans.add(createPlan("RYEGRASS"));
        plans.add(createPlan("KIKUYO"));
        
        when(planRepository.findPlans(farmId)).thenReturn(Optional.of(plans));

        // Act
        Optional<List<Plan>> result = planService.getPlans(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(planRepository, times(1)).findPlans(farmId);
        verify(lambdaContext, times(1)).logInfo(eq(LogType.SERVICE), contains("farm#"));
    }

    @Test
    void getPlans_noPlans_returnsEmpty() throws RepositoryException {
        // Arrange
        String farmId = "farm-empty";
        when(planRepository.findPlans(farmId)).thenReturn(Optional.empty());

        // Act
        Optional<List<Plan>> result = planService.getPlans(farmId);

        // Assert
        assertFalse(result.isPresent());
        verify(planRepository, times(1)).findPlans(farmId);
    }

    @Test
    void getPlans_emptyList_returnsEmptyOptional() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        when(planRepository.findPlans(farmId)).thenReturn(Optional.of(new ArrayList<>()));

        // Act
        Optional<List<Plan>> result = planService.getPlans(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void getPlans_nullFarmId_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planService.getPlans(null)
        );
        assertTrue(exception.getMessage().contains("farmId es requerido"));
    }

    @Test
    void getPlans_emptyFarmId_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planService.getPlans("")
        );
        assertTrue(exception.getMessage().contains("farmId es requerido"));
    }

    @Test
    void getPlans_whitespaceOnly_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planService.getPlans("   ")
        );
        assertTrue(exception.getMessage().contains("farmId es requerido"));
    }

    @Test
    void getPlans_repositoryException_throwsServiceException() throws RepositoryException {
        // Arrange
        String farmId = "farm-error";
        when(planRepository.findPlans(farmId))
                .thenThrow(new RepositoryException("Repository error"));

        // Act & Assert
        assertThrows(com.cattle.exceptions.ServiceException.class, 
                () -> planService.getPlans(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.SERVICE), contains("Repository error"));
    }

    @Test
    void getPlans_unexpectedException_throwsProcessingException() throws RepositoryException {
        // Arrange
        String farmId = "farm-error";
        when(planRepository.findPlans(farmId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(com.cattle.exceptions.ProcessingException.class, 
                () -> planService.getPlans(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.SERVICE), contains("Unexpected error"));
    }

    // ==================== Helper Methods ====================

    private Plan createPlan(String species) {
        return Plan.builder()
                .pk("farm#F001#species#" + species)
                .farmId("F001")
                .species(species)
                .planType("GRAZING")
                .growthRateCmPerDay(0.5)
                .rules(Plan.Rules.builder()
                        .entryHeightCm(30)
                        .exitResidualCm(7)
                        .restDaysMin(35)
                        .build())
                .version(1)
                .build();
    }
}
