package com.cattle;

import com.amazonaws.services.lambda.runtime.Context;
import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.services.BovineSummaryService;
import com.cattle.tasks.service.ReproductiveTaskMaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
class SummaryRefreshSchedulerHandlerTest {

    @Mock
    private BovineSummaryService bovineSummaryService;

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private Context awsContext;

    @Mock
    private ReproductiveTaskMaintenanceService maintenanceService;

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final LocalDate TODAY = LocalDate.now(ZONE);

    private SummaryRefreshSchedulerHandler handler;

    @BeforeEach
    void setUp() {
        openMocks(this);
        handler = new SummaryRefreshSchedulerHandler(bovineSummaryService, maintenanceService, lambdaContext, ZONE);
    }

    @Test
    void handleRequest_success_returnsCountAndMetadata() {
        when(awsContext.getAwsRequestId()).thenReturn("req-123");
        when(bovineSummaryService.refreshAllSummaries()).thenReturn(17);

        Map<String, Object> result = handler.handleRequest(Map.of("source", "aws.scheduler"), awsContext);

        assertEquals("Scheduled summary refresh completed successfully", result.get("message"));
        assertEquals(17, result.get("count"));
        assertEquals("req-123", result.get("requestId"));
        assertEquals("eventbridge-scheduler", result.get("trigger"));
        verify(lambdaContext).logInfo(eq(LogType.SERVICE), contains("Scheduled summary refresh started"));
        verify(lambdaContext).logInfo(eq(LogType.SERVICE), contains("updated=17"));
    }

    @Test
    void handleRequest_nullContext_usesUnknownRequestId() {
        when(bovineSummaryService.refreshAllSummaries()).thenReturn(0);

        Map<String, Object> result = handler.handleRequest(Map.of(), null);

        assertEquals("unknown", result.get("requestId"));
        assertEquals(0, result.get("count"));
        verify(lambdaContext).logInfo(eq(LogType.SERVICE), contains("event={}"));
    }

    @Test
    void handleRequest_nullEvent_summarizedAsEmptyObject() {
        when(bovineSummaryService.refreshAllSummaries()).thenReturn(1);

        handler.handleRequest(null, awsContext);

        verify(lambdaContext).logInfo(eq(LogType.SERVICE), contains("event={}"));
    }

    @Test
    void handleRequest_runtimeException_isRethrownAndLogged() {
        when(awsContext.getAwsRequestId()).thenReturn("req-456");
        IllegalStateException failure = new IllegalStateException("boom");
        when(bovineSummaryService.refreshAllSummaries()).thenThrow(failure);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> handler.handleRequest(Map.of(), awsContext));

        assertSame(failure, thrown);
        verify(lambdaContext).logException(eq(LogType.SERVICE), contains("Scheduled summary refresh failed"), eq(failure));
    }

    // ==================== Tareas reproductivas (HU-20260929, fase B) ====================

    @Test
    void handleRequest_runsReproductiveTasksAfterSummaries() {
        when(bovineSummaryService.refreshAllSummaries()).thenReturn(3);
        ReproductiveTaskMaintenanceService.DailyResult daily =
                new ReproductiveTaskMaintenanceService.DailyResult(5, 2, 1, 0, 0);
        when(maintenanceService.runDaily(TODAY)).thenReturn(daily);

        Map<String, Object> result = handler.handleRequest(Map.of(), awsContext);

        assertEquals(3, result.get("count"));
        assertSame(daily, result.get("reproductiveTasks"));
        org.mockito.InOrder order = org.mockito.Mockito.inOrder(bovineSummaryService, maintenanceService);
        order.verify(bovineSummaryService).refreshAllSummaries();
        order.verify(maintenanceService).runDaily(TODAY);
    }

    @Test
    void handleRequest_tasksFailure_doesNotBreakSummaryRefresh() {
        when(bovineSummaryService.refreshAllSummaries()).thenReturn(3);
        when(maintenanceService.runDaily(TODAY)).thenThrow(new IllegalStateException("tasks table down"));

        Map<String, Object> result = handler.handleRequest(Map.of(), awsContext);

        assertEquals(3, result.get("count"));
        assertEquals("FAILED: tasks table down", result.get("reproductiveTasks"));
        verify(lambdaContext).logException(eq(LogType.SERVICE), contains("Reproductive tasks maintenance failed"),
                org.mockito.ArgumentMatchers.any(IllegalStateException.class));
    }

    @Test
    void handleRequest_summaryFailure_stillRunsTasksThenRethrows() {
        IllegalStateException failure = new IllegalStateException("boom");
        when(bovineSummaryService.refreshAllSummaries()).thenThrow(failure);

        assertThrows(IllegalStateException.class, () -> handler.handleRequest(Map.of(), awsContext));

        verify(maintenanceService).runDaily(TODAY);
    }

    @Test
    void handleRequest_reproductiveTasksJob_skipsSummaries() {
        ReproductiveTaskMaintenanceService.DailyResult daily =
                new ReproductiveTaskMaintenanceService.DailyResult(5, 7, 0, 0, 0);
        when(maintenanceService.runDaily(TODAY)).thenReturn(daily);

        Map<String, Object> result = handler.handleRequest(Map.of("job", "reproductive-tasks"), awsContext);

        assertEquals("Reproductive tasks maintenance completed", result.get("message"));
        assertSame(daily, result.get("reproductiveTasks"));
        verify(bovineSummaryService, org.mockito.Mockito.never()).refreshAllSummaries();
    }
}
