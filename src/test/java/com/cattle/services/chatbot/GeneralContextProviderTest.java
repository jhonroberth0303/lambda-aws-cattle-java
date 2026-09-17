package com.cattle.services.chatbot;

import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.dtos.chatbot.PastureContextDTO;
import com.cattle.enums.QueryIntent;
import com.cattle.services.BovineQueryService;
import com.cattle.services.MilkingQueryService;
import com.cattle.services.PastureQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para GeneralContextProvider.
 * Cubre QueryIntent.GENERAL_QUERY, la única intención que combina los 3 dominios.
 */
@Tag("unit")
@Tag("service")
class GeneralContextProviderTest {

    @Mock
    private BovineQueryService bovineQueryService;

    @Mock
    private MilkingQueryService milkingQueryService;

    @Mock
    private PastureQueryService pastureQueryService;

    private GeneralContextProvider provider;

    @BeforeEach
    void setUp() {
        openMocks(this);
        provider = new GeneralContextProvider(bovineQueryService, milkingQueryService, pastureQueryService);
    }

    @Test
    void supportedIntents_returnsGeneralQuery() {
        Set<QueryIntent> result = provider.supportedIntents();

        assertEquals(Set.of(QueryIntent.GENERAL_QUERY), result);
    }

    @Test
    void buildContext_includesCrossAreaSummary() {
        String farmId = "farm-001";
        IntentContext intent = IntentContext.builder().intent(QueryIntent.GENERAL_QUERY).build();

        when(bovineQueryService.countAllBovines(farmId)).thenReturn(50L);
        when(milkingQueryService.getMonthlyAverageProduction(farmId)).thenReturn(18.5);
        when(pastureQueryService.getAvailablePastures(farmId)).thenReturn(
                List.of(PastureContextDTO.builder().pastureId("P-01").status("DISPONIBLE").build()));

        String result = provider.buildContext(intent, farmId);

        assertTrue(result.contains("RESUMEN GENERAL"));
        assertTrue(result.contains("Total de bovinos: 50"));
        assertTrue(result.contains("Producción promedio mensual"));
        assertTrue(result.contains("Potreros disponibles: 1"));
    }
}
