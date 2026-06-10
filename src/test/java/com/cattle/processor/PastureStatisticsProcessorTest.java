package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureStatisticsDTO;
import com.cattle.entities.Pasture;
import com.cattle.repository.PastureStatisticsRepository;
import com.cattle.services.PastureService;
import com.cattle.services.PastureStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("processor")
class PastureStatisticsProcessorTest {

    @Mock private PastureService pastureService;
    @Mock private PastureStatisticsRepository repository;
    @Mock private PastureStatisticsService statisticsService;
    @Mock private LambdaContext lambdaContext;

    private PastureStatisticsProcessor processor;

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO   = LocalDate.of(2026, 3, 31);

    @BeforeEach
    void setUp() {
        openMocks(this);
        processor = new PastureStatisticsProcessor(pastureService, repository, statisticsService, lambdaContext);
    }

    // ==================== computeForFarm ====================

    @Test
    void computeForFarm_sortsByUtilizationPercentDescending() {
        List<Pasture> pastures = List.of(makePasture("P1", "Norte"), makePasture("P2", "Sur"));
        when(pastureService.getPastures("farm-001")).thenReturn(Optional.of(pastures));
        when(repository.findByPastureInRange(eq("farm-001"), anyString(), any(), any())).thenReturn(List.of());
        when(statisticsService.aggregate(eq("P1"), any(), any(), any(), any(), any()))
                .thenReturn(dto("P1", 20.0));
        when(statisticsService.aggregate(eq("P2"), any(), any(), any(), any(), any()))
                .thenReturn(dto("P2", 60.0));

        List<PastureStatisticsDTO> result = processor.computeForFarm("farm-001", FROM, TO);

        assertEquals(2, result.size());
        assertEquals("P2", result.get(0).getPastureId()); // mayor utilización primero
        assertEquals("P1", result.get(1).getPastureId());
    }

    @Test
    void computeForFarm_nullUtilizationSortsLast() {
        List<Pasture> pastures = List.of(makePasture("P1", "Norte"), makePasture("P2", "Sur"));
        when(pastureService.getPastures("farm-001")).thenReturn(Optional.of(pastures));
        when(repository.findByPastureInRange(any(), any(), any(), any())).thenReturn(List.of());
        when(statisticsService.aggregate(eq("P1"), any(), any(), any(), any(), any()))
                .thenReturn(dto("P1", null));
        when(statisticsService.aggregate(eq("P2"), any(), any(), any(), any(), any()))
                .thenReturn(dto("P2", 35.0));

        List<PastureStatisticsDTO> result = processor.computeForFarm("farm-001", FROM, TO);

        assertEquals("P2", result.get(0).getPastureId());
        assertEquals("P1", result.get(1).getPastureId());
    }

    @Test
    void computeForFarm_emptyPastures_returnsEmptyList() {
        when(pastureService.getPastures("farm-vacía")).thenReturn(Optional.empty());

        List<PastureStatisticsDTO> result = processor.computeForFarm("farm-vacía", FROM, TO);

        assertTrue(result.isEmpty());
        verifyNoInteractions(repository, statisticsService);
    }

    @Test
    void computeForFarm_delegatesEachPastureToRepository() {
        List<Pasture> pastures = List.of(makePasture("P1", "Norte"), makePasture("P2", "Sur"));
        when(pastureService.getPastures("farm-001")).thenReturn(Optional.of(pastures));
        when(repository.findByPastureInRange(any(), any(), any(), any())).thenReturn(List.of());
        when(statisticsService.aggregate(any(), any(), any(), any(), any(), any()))
                .thenReturn(dto("X", null));

        processor.computeForFarm("farm-001", FROM, TO);

        verify(repository).findByPastureInRange("farm-001", "P1", FROM, TO);
        verify(repository).findByPastureInRange("farm-001", "P2", FROM, TO);
    }

    // ==================== computeForPasture ====================

    @Test
    void computeForPasture_foundPasture_usesNameAndSpecies() {
        Pasture pasture = makePasture("P1", "Norte");
        pasture.setSpecies("KIKUYO");
        when(pastureService.getPastures("farm-001")).thenReturn(Optional.of(List.of(pasture)));
        when(repository.findByPastureInRange("farm-001", "P1", FROM, TO)).thenReturn(List.of());
        when(statisticsService.aggregate(eq("P1"), eq("Norte"), eq("KIKUYO"), any(), eq(FROM), eq(TO)))
                .thenReturn(dto("P1", 25.0));

        PastureStatisticsDTO result = processor.computeForPasture("farm-001", "P1", FROM, TO);

        assertNotNull(result);
        verify(statisticsService).aggregate(eq("P1"), eq("Norte"), eq("KIKUYO"), any(), eq(FROM), eq(TO));
    }

    @Test
    void computeForPasture_pastureNotInFarm_fallsBackToPastureId() {
        when(pastureService.getPastures("farm-001")).thenReturn(Optional.of(List.of()));
        when(repository.findByPastureInRange("farm-001", "P-extraño", FROM, TO)).thenReturn(List.of());
        when(statisticsService.aggregate(eq("P-extraño"), eq("P-extraño"), isNull(), any(), eq(FROM), eq(TO)))
                .thenReturn(dto("P-extraño", null));

        PastureStatisticsDTO result = processor.computeForPasture("farm-001", "P-extraño", FROM, TO);

        assertNotNull(result);
        verify(statisticsService).aggregate(eq("P-extraño"), eq("P-extraño"), isNull(), any(), eq(FROM), eq(TO));
    }

    // ==================== helpers ====================

    private Pasture makePasture(String id, String name) {
        Pasture p = new Pasture();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private PastureStatisticsDTO dto(String pastureId, Double utilizationPercent) {
        return PastureStatisticsDTO.builder()
                .pastureId(pastureId)
                .utilizationPercent(utilizationPercent)
                .build();
    }
}
