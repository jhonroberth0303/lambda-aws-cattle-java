package com.cattle;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.services.BovineSummaryService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class SummaryRefreshSchedulerHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final HandlerDependencies DEFAULT_DEPENDENCIES = loadDependencies();

    private final BovineSummaryService bovineSummaryService;
    private final LambdaContext lambdaContext;

    public SummaryRefreshSchedulerHandler() {
        this(DEFAULT_DEPENDENCIES);
    }

    SummaryRefreshSchedulerHandler(BovineSummaryService bovineSummaryService, LambdaContext lambdaContext) {
        this(new HandlerDependencies(bovineSummaryService, lambdaContext));
    }

    private SummaryRefreshSchedulerHandler(HandlerDependencies dependencies) {
        this.bovineSummaryService = dependencies.bovineSummaryService();
        this.lambdaContext = dependencies.lambdaContext();
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        String requestId = context != null ? context.getAwsRequestId() : "unknown";
        lambdaContext.logInfo(LogType.SERVICE,
                "Scheduled summary refresh started. requestId=" + requestId + ", event=" + summarizeEvent(event));

        try {
            int updatedCount = bovineSummaryService.refreshAllSummaries();
            lambdaContext.logInfo(LogType.SERVICE,
                    "Scheduled summary refresh completed successfully. requestId=" + requestId + ", updated=" + updatedCount);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Scheduled summary refresh completed successfully");
            response.put("count", updatedCount);
            response.put("requestId", requestId);
            response.put("trigger", "eventbridge-scheduler");
            return response;
        } catch (RuntimeException exception) {
            lambdaContext.logException(LogType.SERVICE,
                    "Scheduled summary refresh failed. requestId=" + requestId + ", reason=" + exception.getMessage(),
                    exception);
            throw exception;
        } catch (Exception exception) {
            RuntimeException wrappedException = new RuntimeException("Scheduled summary refresh failed", exception);
            lambdaContext.logException(LogType.SERVICE,
                    "Scheduled summary refresh failed. requestId=" + requestId + ", reason=" + exception.getMessage(),
                    wrappedException);
            throw wrappedException;
        }
    }

    private static HandlerDependencies loadDependencies() {
        ConfigurableApplicationContext applicationContext = ApplicationContextHolder.APPLICATION_CONTEXT;
        return new HandlerDependencies(
                applicationContext.getBean(BovineSummaryService.class),
                applicationContext.getBean(LambdaContext.class)
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

    private record HandlerDependencies(BovineSummaryService bovineSummaryService, LambdaContext lambdaContext) {
    }
}