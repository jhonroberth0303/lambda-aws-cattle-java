package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureStatisticsDTO;
import com.cattle.processor.PastureStatisticsProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class PastureStatisticsControllerTest {

    @Mock
    private PastureStatisticsProcessor processor;

    @Mock
    private LambdaContext lambdaContext;

    private PastureStatisticsController controller;

    @BeforeEach
    void setUp() {
        openMocks(this);
        controller = new PastureStatisticsController(processor, lambdaContext);
    }

    @Test
    void getFarmStatistics_withResults_returnsOkWithList() {
        String farmId = "farm-001";
        List<PastureStatisticsDTO> stats = List.of(
                PastureStatisticsDTO.builder().pastureId("P1").cyclesCompleted(3).utilizationPercent(45.0).build(),
                PastureStatisticsDTO.builder().pastureId("P2").cyclesCompleted(1).utilizationPercent(10.0).build()
        );
        when(processor.computeForFarm(eq(farmId), any(LocalDate.class), any(LocalDate.class))).thenReturn(stats);

        ResponseEntity<?> response = controller.getFarmStatistics(farmId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, ((List<?>) response.getBody()).size());
        verify(processor, times(1)).computeForFarm(eq(farmId), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getFarmStatistics_emptyFarm_returnsOkWithEmptyList() {
        when(processor.computeForFarm(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        ResponseEntity<?> response = controller.getFarmStatistics("farm-empty", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    @Test
    void getFarmStatistics_withExplicitDates_passesToProcessor() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);
        when(processor.computeForFarm(anyString(), eq(from), eq(to))).thenReturn(List.of());

        controller.getFarmStatistics("farm-001", from, to);

        verify(processor).computeForFarm("farm-001", from, to);
    }

    @Test
    void getFarmStatistics_reversedDates_returnsBadRequest() {
        LocalDate from = LocalDate.of(2026, 6, 9);
        LocalDate to = LocalDate.of(2026, 1, 1);

        ResponseEntity<?> response = controller.getFarmStatistics("farm-001", from, to);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(processor, never()).computeForFarm(anyString(), any(), any());
    }

    @Test
    void getPastureStatistics_withResults_returnsOk() {
        String farmId = "farm-001";
        String pastureId = "P-01";
        PastureStatisticsDTO dto = PastureStatisticsDTO.builder()
                .pastureId(pastureId)
                .pastureName("Potrero Norte")
                .cyclesCompleted(2)
                .utilizationPercent(33.3)
                .build();
        when(processor.computeForPasture(eq(farmId), eq(pastureId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dto);

        ResponseEntity<?> response = controller.getPastureStatistics(farmId, pastureId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PastureStatisticsDTO body = (PastureStatisticsDTO) response.getBody();
        assertEquals(pastureId, body.getPastureId());
        assertEquals(2, body.getCyclesCompleted());
        verify(processor).computeForPasture(eq(farmId), eq(pastureId), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getPastureStatistics_noData_returnsOkWithNullMetrics() {
        PastureStatisticsDTO emptyDto = PastureStatisticsDTO.builder()
                .pastureId("P-99")
                .cyclesCompleted(null)
                .build();
        when(processor.computeForPasture(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(emptyDto);

        ResponseEntity<?> response = controller.getPastureStatistics("farm-001", "P-99", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(((PastureStatisticsDTO) response.getBody()).getCyclesCompleted());
    }

    @Test
    void getPastureStatistics_reversedDates_returnsBadRequest() {
        LocalDate from = LocalDate.of(2026, 6, 9);
        LocalDate to = LocalDate.of(2026, 1, 1);

        ResponseEntity<?> response = controller.getPastureStatistics("farm-001", "P-01", from, to);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(processor, never()).computeForPasture(anyString(), anyString(), any(), any());
    }
}
