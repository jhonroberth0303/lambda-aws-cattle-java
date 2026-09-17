package com.cattle.services;

import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.enums.QueryIntent;
import com.cattle.services.chatbot.ChatbotContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para ContextBuilderService.
 * <p>
 * Desde el rediseño de desacople del chatbot (docs/deuda-tecnica/backend/codigo-fase1-domainqueryservice.md),
 * ContextBuilderService ya no conoce BovineQueryService/MilkingQueryService/PastureQueryService:
 * solo enruta por QueryIntent hacia un ChatbotContextProvider. Las aserciones de contenido
 * formateado (traducción de género, secciones de producción/potreros, etc.) viven ahora en
 * BovineQueryServiceTest, MilkingQueryServiceTest, PastureQueryServiceTest y GeneralContextProviderTest.
 */
@Tag("unit")
@Tag("service")
class ContextBuilderServiceTest {

    @Mock
    private ChatbotContextProvider bovineProvider;

    @Mock
    private ChatbotContextProvider generalProvider;

    private ContextBuilderService contextBuilderService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(bovineProvider.supportedIntents()).thenReturn(Set.of(QueryIntent.COUNT_BOVINES));
        when(generalProvider.supportedIntents()).thenReturn(Set.of(QueryIntent.GENERAL_QUERY));
        contextBuilderService = new ContextBuilderService(List.of(bovineProvider, generalProvider));
    }

    @Test
    void buildContext_routesToProviderThatSupportsIntent() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.COUNT_BOVINES).build();
        when(bovineProvider.buildContext(intent, "farm-001")).thenReturn("TOTAL DE BOVINOS: 50");

        String result = contextBuilderService.buildContext(intent, "farm-001");

        assertTrue(result.contains("TOTAL DE BOVINOS: 50"));
        verify(bovineProvider).buildContext(intent, "farm-001");
        verify(generalProvider, never()).buildContext(any(), any());
    }

    @Test
    void buildContext_unregisteredIntent_fallsBackToGeneralProvider() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.PASTURE_STATUS).build(); // nadie lo registró en este test
        when(generalProvider.buildContext(intent, "farm-001")).thenReturn("RESUMEN GENERAL");

        String result = contextBuilderService.buildContext(intent, "farm-001");

        assertTrue(result.contains("RESUMEN GENERAL"));
        verify(generalProvider).buildContext(intent, "farm-001");
    }

    @Test
    void buildContext_nullIntent_fallsBackToGeneralProvider() {
        IntentContext intent = IntentContext.builder().intent(null).build();
        when(generalProvider.buildContext(intent, "farm-001")).thenReturn("RESUMEN GENERAL");

        String result = contextBuilderService.buildContext(intent, "farm-001");

        assertTrue(result.contains("RESUMEN GENERAL"));
    }

    @Test
    void buildContext_providerThrows_returnsFriendlyError() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.COUNT_BOVINES).build();
        when(bovineProvider.buildContext(intent, "farm-001")).thenThrow(new RuntimeException("boom"));

        String result = contextBuilderService.buildContext(intent, "farm-001");

        assertEquals("Error al construir el contexto. Por favor intenta nuevamente.", result);
    }

    @Test
    void buildContext_truncatesLongContext() {
        IntentContext intent = IntentContext.builder().intent(QueryIntent.COUNT_BOVINES).build();
        when(bovineProvider.buildContext(intent, "farm-001")).thenReturn("x".repeat(3000));

        String result = contextBuilderService.buildContext(intent, "farm-001");

        assertTrue(result.contains("[Contexto truncado...]"));
        assertTrue(result.length() <= 2000);
    }

    @Test
    void buildPrompt_validInputs_returnsEnrichedPrompt() {
        String result = contextBuilderService.buildPrompt("¿Cuántas vacas tengo?", "Total de bovinos: 50");

        assertTrue(result.contains("asistente virtual"));
        assertTrue(result.contains("Total de bovinos: 50"));
        assertTrue(result.contains("¿Cuántas vacas tengo?"));
    }

    @Test
    void buildPrompt_emptyContext_returnsPromptWithEmptyContext() {
        String result = contextBuilderService.buildPrompt("¿Cuántas vacas hay?", "");

        assertNotNull(result);
        assertTrue(result.contains("¿Cuántas vacas hay?"));
    }
}
