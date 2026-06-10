package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureStatisticsDTO;
import com.cattle.events.entities.PastureEventItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("service")
class PastureStatisticsServiceTest {

    @Mock
    private LambdaContext lambdaContext;

    private PastureStatisticsService service;

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 3, 31); // 90 days

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new PastureStatisticsService(new ObjectMapper(), lambdaContext);
    }

    @Test
    void aggregate_noEvents_returnsNullMetrics() {
        PastureStatisticsDTO result = service.aggregate("P1", "Potrero 1", "KIKUYO", List.of(), FROM, TO);

        assertNotNull(result);
        assertEquals("P1", result.getPastureId());
        assertEquals("Potrero 1", result.getPastureName());
        assertEquals("KIKUYO", result.getSpecies());
        assertNull(result.getCyclesCompleted());
        assertNull(result.getAvgDaysInUse());
        assertNull(result.getAvgRestDays());
        assertNull(result.getUtilizationPercent());
        assertNull(result.getAvgResidualCm());
        assertNull(result.getLastResidualCm());
        assertNull(result.getLaborCount());
        assertNull(result.getLaborSummary());
    }

    @Test
    void aggregate_oneCycle_returnsCorrectMetrics() {
        // OPEN Jan 10 → CLOSE Jan 20 = 10 days in use
        PastureEventItem open = makeEvent("OPEN", "2026-01-10T08:00:00Z", null);
        PastureEventItem close = makeEvent("CLOSE", "2026-01-20T08:00:00Z",
                "{\"residualCm\":8,\"animals\":10}");

        PastureStatisticsDTO result = service.aggregate("P1", "Potrero 1", "KIKUYO",
                List.of(open, close), FROM, TO);

        assertEquals(1, result.getCyclesCompleted());
        assertEquals(10.0, result.getAvgDaysInUse(), 0.01);
        assertNull(result.getAvgRestDays()); // sin descanso previo completo
        assertEquals(8, result.getLastResidualCm());
        assertEquals(8, result.getAvgResidualCm());
        assertNotNull(result.getUtilizationPercent());
        assertTrue(result.getUtilizationPercent() > 0);
    }

    @Test
    void aggregate_twoCycles_computesAverages() {
        // Ciclo 1: Jan 1–11 = 10 días uso
        // Descanso: Jan 11–21 = 10 días
        // Ciclo 2: Jan 21–31 = 10 días uso
        PastureEventItem open1 = makeEvent("OPEN", "2026-01-01T00:00:00Z", null);
        PastureEventItem close1 = makeEvent("CLOSE", "2026-01-11T00:00:00Z",
                "{\"residualCm\":6}");
        PastureEventItem open2 = makeEvent("OPEN", "2026-01-21T00:00:00Z", null);
        PastureEventItem close2 = makeEvent("CLOSE", "2026-01-31T00:00:00Z",
                "{\"residualCm\":10}");

        PastureStatisticsDTO result = service.aggregate("P1", "Potrero 1", "KIKUYO",
                List.of(open1, close1, open2, close2), FROM, TO);

        assertEquals(2, result.getCyclesCompleted());
        assertEquals(10.0, result.getAvgDaysInUse(), 0.01);
        assertEquals(10.0, result.getAvgRestDays(), 0.01);
        assertEquals(8, result.getAvgResidualCm()); // (6+10)/2
        assertEquals(10, result.getLastResidualCm());
    }

    @Test
    void aggregate_onlyLaborEvents_noRotationMetrics() {
        PastureEventItem labor1 = makeEvent("FERTILIZED", "2026-02-01T10:00:00Z", null);
        PastureEventItem labor2 = makeEvent("HEIGHT_MEASURED", "2026-02-15T10:00:00Z",
                "{\"heightCm\":25}");
        PastureEventItem labor3 = makeEvent("FERTILIZED", "2026-03-01T10:00:00Z", null);

        PastureStatisticsDTO result = service.aggregate("P1", "Potrero 1", "KIKUYO",
                List.of(labor1, labor2, labor3), FROM, TO);

        assertNull(result.getCyclesCompleted());
        assertNull(result.getAvgDaysInUse());
        assertEquals(3, result.getLaborCount());
        assertNotNull(result.getLaborSummary());
        assertEquals(2, result.getLaborSummary().size()); // FERTILIZED + HEIGHT_MEASURED

        var fertilizedEntry = result.getLaborSummary().stream()
                .filter(l -> "FERTILIZED".equals(l.getEventType()))
                .findFirst().orElseThrow();
        assertEquals(2, fertilizedEntry.getCount());
    }

    @Test
    void aggregate_nullResidualCm_doesNotFail() {
        PastureEventItem open = makeEvent("OPEN", "2026-01-10T08:00:00Z", null);
        PastureEventItem close = makeEvent("CLOSE", "2026-01-20T08:00:00Z",
                "{\"residualCm\":null,\"animals\":5}");

        PastureStatisticsDTO result = service.aggregate("P1", "Potrero 1", "KIKUYO",
                List.of(open, close), FROM, TO);

        assertEquals(1, result.getCyclesCompleted());
        assertNull(result.getAvgResidualCm());
        assertNull(result.getLastResidualCm());
    }

    @Test
    void aggregate_malformedPayloadJson_doesNotFail() {
        PastureEventItem open = makeEvent("OPEN", "2026-01-05T00:00:00Z", null);
        PastureEventItem close = makeEvent("CLOSE", "2026-01-10T00:00:00Z", "not-valid-json");

        PastureStatisticsDTO result = service.aggregate("P1", "Potrero 1", "KIKUYO",
                List.of(open, close), FROM, TO);

        assertEquals(1, result.getCyclesCompleted());
        assertNull(result.getAvgResidualCm());
    }

    @Test
    void aggregate_utilizationPercent_doesNotExceed100() {
        // 90 días en uso / 90 días de período = 100%
        PastureEventItem open = makeEvent("OPEN", "2026-01-01T00:00:00Z", null);
        PastureEventItem close = makeEvent("CLOSE", "2026-03-31T00:00:00Z",
                "{\"residualCm\":5}");

        PastureStatisticsDTO result = service.aggregate("P1", "Potrero 1", "KIKUYO",
                List.of(open, close), FROM, TO);

        assertNotNull(result.getUtilizationPercent());
        assertTrue(result.getUtilizationPercent() <= 100.0);
    }

    @Test
    void aggregate_setsFromAndToInResult() {
        PastureStatisticsDTO result = service.aggregate("P1", "N", "S", List.of(), FROM, TO);

        assertEquals("2026-01-01", result.getFrom());
        assertEquals("2026-03-31", result.getTo());
    }

    // ==================== helpers ====================

    private PastureEventItem makeEvent(String type, String isoAt, String payloadJson) {
        PastureEventItem item = new PastureEventItem();
        item.setEventType(type);
        item.setEventAt(Instant.parse(isoAt));
        item.setFarmId("farm-001");
        item.setPastureId("P1");
        item.setPayloadJson(payloadJson);
        return item;
    }
}
