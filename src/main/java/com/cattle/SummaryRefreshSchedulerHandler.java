package com.cattle;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cattle.config.AppProperties;
import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.services.BovineSummaryService;
import com.cattle.tasks.service.ReproductiveTaskMaintenanceService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Job diario (03:00 America/Bogota): refresca los summaries y mantiene la agenda de tareas
 * reproductivas (HU-20260929, D2). Los dos pasos están aislados: el fallo de uno no impide el
 * otro. Con {@code {"job":"reproductive-tasks"}} solo corre el paso de tareas (carga inicial o
 * reproceso manual).
 */
public class SummaryRefreshSchedulerHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    static final String JOB_KEY = "job";
    static final String JOB_REPRODUCTIVE_TASKS = "reproductive-tasks";

    private final BovineSummaryService bovineSummaryService;
    private final ReproductiveTaskMaintenanceService reproductiveTaskMaintenanceService;
    private final LambdaContext lambdaContext;
    private final ZoneId zoneId;

    public SummaryRefreshSchedulerHandler() {
        this(loadDependencies());
    }

    SummaryRefreshSchedulerHandler(BovineSummaryService bovineSummaryService,
                                   ReproductiveTaskMaintenanceService reproductiveTaskMaintenanceService,
                                   LambdaContext lambdaContext, ZoneId zoneId) {
        this(new HandlerDependencies(bovineSummaryService, reproductiveTaskMaintenanceService, lambdaContext, zoneId));
    }

    private SummaryRefreshSchedulerHandler(HandlerDependencies dependencies) {
        this.bovineSummaryService = dependencies.bovineSummaryService();
        this.reproductiveTaskMaintenanceService = dependencies.reproductiveTaskMaintenanceService();
        this.lambdaContext = dependencies.lambdaContext();
        this.zoneId = dependencies.zoneId();
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        String requestId = context != null ? context.getAwsRequestId() : "unknown";
        lambdaContext.logInfo(LogType.SERVICE,
                "Scheduled summary refresh started. requestId=" + requestId + ", event=" + summarizeEvent(event));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("requestId", requestId);
        response.put("trigger", "eventbridge-scheduler");

        if (isReproductiveTasksOnly(event)) {
            response.put("message", "Reproductive tasks maintenance completed");
            response.put("reproductiveTasks", runReproductiveTasks());
            return response;
        }

        RuntimeException summaryFailure = null;
        try {
            int updatedCount = bovineSummaryService.refreshAllSummaries();
            lambdaContext.logInfo(LogType.SERVICE,
                    "Scheduled summary refresh completed successfully. requestId=" + requestId + ", updated=" + updatedCount);
            response.put("message", "Scheduled summary refresh completed successfully");
            response.put("count", updatedCount);
        } catch (RuntimeException exception) {
            summaryFailure = exception;
        } catch (Exception exception) {
            summaryFailure = new RuntimeException("Scheduled summary refresh failed", exception);
        }

        response.put("reproductiveTasks", runReproductiveTasks());

        if (summaryFailure != null) {
            lambdaContext.logException(LogType.SERVICE,
                    "Scheduled summary refresh failed. requestId=" + requestId + ", reason=" + summaryFailure.getMessage(),
                    summaryFailure);
            throw summaryFailure;
        }
        return response;
    }

    /** Paso de tareas aislado: su fallo se registra y se informa, sin romper el job. */
    private Object runReproductiveTasks() {
        try {
            return reproductiveTaskMaintenanceService.runDaily(LocalDate.now(zoneId));
        } catch (Exception exception) {
            lambdaContext.logException(LogType.SERVICE,
                    "Reproductive tasks maintenance failed. reason=" + exception.getMessage(), exception);
            return "FAILED: " + exception.getMessage();
        }
    }

    private static boolean isReproductiveTasksOnly(Map<String, Object> event) {
        return event != null && JOB_REPRODUCTIVE_TASKS.equals(String.valueOf(event.get(JOB_KEY)));
    }

    private static HandlerDependencies loadDependencies() {
        ConfigurableApplicationContext applicationContext = ApplicationContextHolder.APPLICATION_CONTEXT;
        return new HandlerDependencies(
                applicationContext.getBean(BovineSummaryService.class),
                applicationContext.getBean(ReproductiveTaskMaintenanceService.class),
                applicationContext.getBean(LambdaContext.class),
                ZoneId.of(applicationContext.getBean(AppProperties.class).getTimezone())
        );
    }

    private static String summarizeEvent(Map<String, Object> event) {
        if (event == null || event.isEmpty()) {
            return "{}";
        }
        return event.keySet().toString();
    }

    private static final class ApplicationContextHolder {
        private static final ConfigurableApplicationContext APPLICATION_CONTEXT = new SpringApplicationBuilder(Application.class)
                .properties(
                        "server.port=0",
                        "spring.main.banner-mode=off"
                )
                .run();
    }

    private record HandlerDependencies(BovineSummaryService bovineSummaryService,
                                       ReproductiveTaskMaintenanceService reproductiveTaskMaintenanceService,
                                       LambdaContext lambdaContext,
                                       ZoneId zoneId) {
    }
}
