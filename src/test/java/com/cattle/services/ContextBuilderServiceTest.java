package com.cattle.services;

import com.cattle.dtos.chatbot.BovineContextDTO;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.dtos.chatbot.MilkingContextDTO;
import com.cattle.dtos.chatbot.PastureContextDTO;
import com.cattle.enums.QueryIntent;
import com.cattle.exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para ContextBuilderService
 * Fase 14 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Services 26% → 38%
 * Tests: 10
 */
@Tag("unit")
@Tag("service")
class ContextBuilderServiceTest {

    @Mock
    private BovineQueryService bovineQueryService;

    @Mock
    private MilkingQueryService milkingQueryService;

    @Mock
    private PastureQueryService pastureQueryService;

    private ContextBuilderService contextBuilderService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        contextBuilderService = new ContextBuilderService();
        try {
            java.lang.reflect.Field bovineField = ContextBuilderService.class.getDeclaredField("bovineQueryService");
            bovineField.setAccessible(true);
            bovineField.set(contextBuilderService, bovineQueryService);
            
            java.lang.reflect.Field milkingField = ContextBuilderService.class.getDeclaredField("milkingQueryService");
            milkingField.setAccessible(true);
            milkingField.set(contextBuilderService, milkingQueryService);
            
            java.lang.reflect.Field pastureField = ContextBuilderService.class.getDeclaredField("pastureQueryService");
            pastureField.setAccessible(true);
            pastureField.set(contextBuilderService, pastureQueryService);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }
    }

    // ==================== buildContext - COUNT_BOVINES ====================

    @Test
    void buildContext_countBovines_returnsContext() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.COUNT_BOVINES)
                .build();
        
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(50L);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO"));
        verify(bovineQueryService, times(1)).countAllBovines(farmId);
    }

    // ==================== buildContext - AGGREGATE_MILKING ====================

    @Test
    void buildContext_aggregateMilking_returnsContext() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.AGGREGATE_MILKING)
                .build();
        
        when(milkingQueryService.getMonthlyAverageProduction(farmId)).thenReturn(25.5);
        when(milkingQueryService.getWeeklyAverageProduction(farmId)).thenReturn(26.0);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO"));
        verify(milkingQueryService, times(1)).getMonthlyAverageProduction(farmId);
    }

    // ==================== buildContext - PASTURE_STATUS ====================

    @Test
    void buildContext_pastureStatus_returnsContext() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.PASTURE_STATUS)
                .build();
        
        List<PastureContextDTO> pastures = new ArrayList<>();
        pastures.add(createPastureContext("P-01", "DISPONIBLE"));
        when(pastureQueryService.getAvailablePastures(farmId)).thenReturn(pastures);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO"));
        verify(pastureQueryService, times(1)).getAvailablePastures(farmId);
    }

    // ==================== buildContext - LIST_ALL_BOVINES ====================

    @Test
    void buildContext_listAllBovines_returnsContext() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.LIST_ALL_BOVINES)
                .build();
        
        List<BovineContextDTO> bovines = new ArrayList<>();
        bovines.add(createBovineContext("1", "Vaca 1"));
        bovines.add(createBovineContext("2", "Vaca 2"));
        when(bovineQueryService.getAllBovinesDetails(farmId)).thenReturn(bovines);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO"));
        verify(bovineQueryService, times(1)).getAllBovinesDetails(farmId);
    }

    // ==================== buildContext - GENERAL_QUERY ====================

    @Test
    void buildContext_generalQuery_returnsGeneralContext() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.GENERAL_QUERY)
                .build();
        
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(50L);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO"));
    }

    // ==================== buildContext - Edge Cases ====================

    @Test
    void buildContext_nullIntent_handlesGracefully() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(null)
                .build();
        
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(50L);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void buildContext_emptyFarmId_callsServices() throws RepositoryException {
        // Arrange
        String farmId = "";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.COUNT_BOVINES)
                .build();
        
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(0L);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        verify(bovineQueryService, times(1)).countAllBovines(farmId);
    }

    @Test
    void buildContext_repositoryException_handlesGracefully() throws RepositoryException {
        // Arrange
        String farmId = "farm-error";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.COUNT_BOVINES)
                .build();
        
        when(bovineQueryService.countAllBovines(farmId))
                .thenThrow(new RepositoryException("Repository error"));

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertEquals("Error al construir el contexto. Por favor intenta nuevamente.", result);
    }

    @Test
    void buildContext_anyIntent_returnsNonEmptyString() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.COUNT_BOVINES)
                .build();
        
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(10L);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.length() > 0);
    }

    // ==================== Helper Methods ====================

    private BovineContextDTO createBovineContext(String id, String name) {
        return BovineContextDTO.builder()
                .bovineId(id)
                .name(name)
                .gender("female")
                .breed("Holstein")
                .bornDate(LocalDate.now().minusMonths(36))
                .ageInMonths(36)
                .build();
    }

    private PastureContextDTO createPastureContext(String id, String status) {
        PastureContextDTO dto = new PastureContextDTO();
        dto.setPastureId(id);
        dto.setName("Potrero " + id);
        dto.setStatus(status);
        dto.setAreaHa(10.0);
        return dto;
    }

    // ==================== buildContext - COUNT_BY_GENDER ====================

    @Test
    void buildContext_countByGender_returnsContext() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.COUNT_BY_GENDER)
                .gender("female")
                .build();
        
        Map<String, Long> byGender = new HashMap<>();
        byGender.put("female", 40L);
        byGender.put("male", 10L);
        when(bovineQueryService.countByGender(farmId)).thenReturn(byGender);
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(50L);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO"));
        verify(bovineQueryService, times(1)).countByGender(farmId);
    }

    // ==================== buildContext - GET_BOVINE_DETAILS ====================

    @Test
    void buildContext_getBovineDetails_returnsContext() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.GET_BOVINE_DETAILS)
                .build();
        
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(50L);

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO"));
    }

    // ==================== buildPrompt Tests ====================

    @Test
    void buildPrompt_validInputs_returnsEnrichedPrompt() {
        // Arrange
        String userMessage = "¿Cuántas vacas tengo?";
        String context = "Total de bovinos: 50";

        // Act
        String result = contextBuilderService.buildPrompt(userMessage, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("asistente virtual"));
        assertTrue(result.contains(context));
        assertTrue(result.contains(userMessage));
    }

    @Test
    void buildPrompt_emptyContext_returnsPromptWithEmptyContext() {
        // Arrange
        String userMessage = "¿Cuántas vacas hay?";
        String context = "";

        // Act
        String result = contextBuilderService.buildPrompt(userMessage, context);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(userMessage));
    }

    // ==================== buildContext - Empty Lists ====================

    @Test
    void buildContext_listAllBovinesEmpty_returnsEmptyMessage() throws RepositoryException {
        // Arrange
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.LIST_ALL_BOVINES)
                .build();
        
        when(bovineQueryService.getAllBovinesDetails(farmId)).thenReturn(new ArrayList<>());

        // Act
        String result = contextBuilderService.buildContext(intent, farmId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("CONTEXTO") || result.contains("No se encontraron"));
    }

    @Test
    void buildContext_listAllBovines_omitsOptionalFragmentsWhenDataMissing() throws RepositoryException {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.LIST_ALL_BOVINES)
                .build();
        List<BovineContextDTO> bovines = List.of(
                BovineContextDTO.builder()
                        .bovineId("1")
                        .gender(null)
                        .ageInMonths(0)
                        .build()
        );
        when(bovineQueryService.getAllBovinesDetails(farmId)).thenReturn(bovines);

        String result = contextBuilderService.buildContext(intent, farmId);

        assertTrue(result.contains("Desconocido"));
        assertFalse(result.contains("Nombre:"));
        assertFalse(result.contains("Raza:"));
        assertFalse(result.contains("Edad:"));
    }

    @Test
    void buildContext_aggregateMilking_includesShiftAndTopProducer() throws RepositoryException {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.AGGREGATE_MILKING)
                .build();

        when(milkingQueryService.getMonthlyAverageProduction(farmId)).thenReturn(25.5);
        when(milkingQueryService.getWeeklyAverageProduction(farmId)).thenReturn(26.0);
        when(milkingQueryService.getProductionByShift(farmId)).thenReturn(Map.of("AM", 30.0, "PM", 21.0));
        when(milkingQueryService.getTopProducerBovine(farmId)).thenReturn(
                MilkingContextDTO.builder().bovineName("Bovina 10").litersMilked(44.2).build());

        String result = contextBuilderService.buildContext(intent, farmId);

        assertTrue(result.contains("PRODUCCIÓN DE LECHE"));
        assertTrue(result.contains("Bovina 10"));
        assertTrue(result.contains("AM"));
    }

    @Test
    void buildContext_aggregateMilking_withoutTopProducer_skipsSection() throws RepositoryException {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.AGGREGATE_MILKING)
                .build();

        when(milkingQueryService.getMonthlyAverageProduction(farmId)).thenReturn(25.5);
        when(milkingQueryService.getWeeklyAverageProduction(farmId)).thenReturn(26.0);
        when(milkingQueryService.getProductionByShift(farmId)).thenReturn(Map.of());
        when(milkingQueryService.getTopProducerBovine(farmId)).thenReturn(null);

        String result = contextBuilderService.buildContext(intent, farmId);

        assertTrue(result.contains("PRODUCCIÓN DE LECHE"));
        assertFalse(result.contains("Top productor"));
    }

    @Test
    void buildContext_pastureStatus_includesDistributionSummary() throws RepositoryException {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.PASTURE_STATUS)
                .build();

        when(pastureQueryService.getAvailablePastures(farmId)).thenReturn(List.of(createPastureContext("P-01", "DISPONIBLE")));
        when(pastureQueryService.getPasturesInUse(farmId)).thenReturn(List.of(createPastureContext("P-02", "EN_USO")));
        when(pastureQueryService.getTotalHectaresInUse(farmId)).thenReturn(12.5);
        when(pastureQueryService.getTotalAvailableHectares(farmId)).thenReturn(20.0);
        when(pastureQueryService.getPastureCountByStatus(farmId)).thenReturn(Map.of("DISPONIBLE", 1, "EN_USO", 1));

        String result = contextBuilderService.buildContext(intent, farmId);

        assertTrue(result.contains("ESTADO DE POTREROS"));
        assertTrue(result.contains("EN_USO"));
    }

    @Test
    void buildContext_generalQuery_includesCrossAreaSummary() throws RepositoryException {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.GENERAL_QUERY)
                .build();

        when(bovineQueryService.countAllBovines(farmId)).thenReturn(50L);
        when(milkingQueryService.getMonthlyAverageProduction(farmId)).thenReturn(18.5);
        when(pastureQueryService.getAvailablePastures(farmId)).thenReturn(List.of(createPastureContext("P-01", "DISPONIBLE")));

        String result = contextBuilderService.buildContext(intent, farmId);

        assertTrue(result.contains("RESUMEN GENERAL"));
        assertTrue(result.contains("Producción promedio mensual"));
    }

    @Test
    void buildContext_repositoryException_returnsFriendlyError() throws RepositoryException {
        String farmId = "farm-error";
        IntentContext intent = IntentContext.builder()
                .intent(QueryIntent.COUNT_BOVINES)
                .build();

        when(bovineQueryService.countAllBovines(farmId)).thenThrow(new RepositoryException("Repository error"));

        String result = contextBuilderService.buildContext(intent, farmId);

        assertEquals("Error al construir el contexto. Por favor intenta nuevamente.", result);
    }

    @Test
    void buildContext_countBovines_translatesGenderLabels() throws RepositoryException {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder().intent(QueryIntent.COUNT_BOVINES).build();
        when(bovineQueryService.countAllBovines(farmId)).thenReturn(10L);
        when(bovineQueryService.countByGender(farmId)).thenReturn(Map.of("female", 8L, "male", 2L));

        String result = contextBuilderService.buildContext(intent, farmId);

        assertTrue(result.contains("Hembras"));
        assertTrue(result.contains("Machos"));
    }

    @Test
    void buildContext_listAllBovines_truncatesLongContext() throws RepositoryException {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder().intent(QueryIntent.LIST_ALL_BOVINES).build();
        List<BovineContextDTO> bovines = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            bovines.add(BovineContextDTO.builder()
                    .bovineId(String.valueOf(i))
                    .name("Bovino-nombre-muy-largo-" + i)
                    .gender(i % 2 == 0 ? "female" : "male")
                    .breed("Holstein-super-larga-" + i)
                    .ageInMonths(24)
                    .build());
        }
        when(bovineQueryService.getAllBovinesDetails(farmId)).thenReturn(bovines);

        String result = contextBuilderService.buildContext(intent, farmId);

        assertTrue(result.contains("[Contexto truncado...]"));
        assertTrue(result.length() <= 2000);
    }

    @Test
    void privateHelpers_translateCategoryStatusAndTruncate_areCovered() throws Exception {
        Method translateCategory = ContextBuilderService.class.getDeclaredMethod("translateCategory", String.class);
        Method translateGender = ContextBuilderService.class.getDeclaredMethod("translateGender", String.class);
        Method translateStatus = ContextBuilderService.class.getDeclaredMethod("translateStatus", String.class);
        Method truncateContext = ContextBuilderService.class.getDeclaredMethod("truncateContext", String.class);
        translateCategory.setAccessible(true);
        translateGender.setAccessible(true);
        translateStatus.setAccessible(true);
        truncateContext.setAccessible(true);

        assertEquals("Vacas", translateCategory.invoke(contextBuilderService, "cow"));
        assertEquals("Toros", translateCategory.invoke(contextBuilderService, "bull"));
        assertEquals("Terneros", translateCategory.invoke(contextBuilderService, "calf"));
        assertEquals("Novillas", translateCategory.invoke(contextBuilderService, "heifer"));
        assertEquals("Novillos", translateCategory.invoke(contextBuilderService, "steer"));
        assertEquals("otro", translateCategory.invoke(contextBuilderService, "otro"));
        assertEquals("Desconocida", translateCategory.invoke(contextBuilderService, new Object[]{null}));
        assertEquals("Machos", translateGender.invoke(contextBuilderService, "male"));
        assertEquals("Hembras", translateGender.invoke(contextBuilderService, "female"));
        assertEquals("otro", translateGender.invoke(contextBuilderService, "otro"));
        assertEquals("Desconocido", translateGender.invoke(contextBuilderService, new Object[]{null}));
        assertEquals("Preñadas", translateStatus.invoke(contextBuilderService, "PREGNANT"));
        assertEquals("Lactando", translateStatus.invoke(contextBuilderService, "LACTATING"));
        assertEquals("Secas", translateStatus.invoke(contextBuilderService, "DRY"));
        assertEquals("Abiertas", translateStatus.invoke(contextBuilderService, "OPEN"));
        assertEquals("otro", translateStatus.invoke(contextBuilderService, "otro"));
        assertEquals("Desconocido", translateStatus.invoke(contextBuilderService, new Object[]{null}));
        assertEquals("texto corto", truncateContext.invoke(contextBuilderService, "texto corto"));
    }

}
