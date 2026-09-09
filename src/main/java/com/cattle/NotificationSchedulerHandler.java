package com.cattle;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.ScheduleWindow;
import com.cattle.notifications.producer.NotificationProducer;
import com.cattle.notifications.service.NotificationDispatcher;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lambda programada de la plataforma de notificaciones.
 * <p>
 * EventBridge Scheduler la invoca con {@code {"window":"MORNING"}} (un schedule por ventana).
 * El handler corre los {@link NotificationProducer} de esa ventana con aislamiento de fallos y
 * entrega los drafts resultantes al {@link NotificationDispatcher}. Sin campo {@code window}
 * corre todos los productores (útil para invocación manual).
 */
public class NotificationSchedulerHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String DEFAULT_TIMEZONE = "America/Bogota";
    private static final String EVENT_WINDOW_KEY = "window";
    private static final String ALL_WINDOWS = "ALL";

    private final List<NotificationProducer> producers;
    private final NotificationDispatcher dispatcher;
    private final LambdaContext lambdaContext;
    private final ZoneId zoneId;

    public NotificationSchedulerHandler() {
        this(Dependencies.fromSpringContext());
    }

    NotificationSchedulerHandler(List<NotificationProducer> producers,
                                 NotificationDispatcher dispatcher,
                                 LambdaContext lambdaContext,
                                 ZoneId zoneId) {
        this.producers = producers;
        this.dispatcher = dispatcher;
        this.lambdaContext = lambdaContext;
        this.zoneId = zoneId;
    }

    private NotificationSchedulerHandler(Dependencies dependencies) {
        this(dependencies.producers(), dependencies.dispatcher(), dependencies.lambdaContext(), dependencies.zoneId());
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        String requestId = context != null ? context.getAwsRequestId() : "unknown";
        Optional<ScheduleWindow> window = ScheduleWindow.parse(stringValue(event));
        String windowLabel = window.map(Enum::name).orElse(ALL_WINDOWS);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        lambdaContext.logInfo(LogType.SERVICE, "Notification scheduler started. requestId=" + requestId
                + ", window=" + windowLabel + ", now=" + now);

        List<NotificationDraft> drafts = collectDrafts(window, now);
        NotificationDispatcher.DispatchResult result = dispatcher.dispatch(drafts);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Notification scheduler completed");
        response.put("window", windowLabel);
        response.put("drafts", drafts.size());
        response.put("created", result.created());
        response.put("deduplicated", result.deduplicated());
        response.put("failed", result.failed());
        response.put("requestId", requestId);
        response.put("trigger", "eventbridge-scheduler");

        lambdaContext.logInfo(LogType.SERVICE, "Notification scheduler completed. " + response);
        return response;
    }

    private List<NotificationDraft> collectDrafts(Optional<ScheduleWindow> window, ZonedDateTime now) {
        List<NotificationDraft> drafts = new ArrayList<>();
        for (NotificationProducer producer : producers) {
            if (window.isPresent() && producer.window() != window.get()) {
                continue;
            }
            try {
                List<NotificationDraft> produced = producer.evaluate(now);
                if (produced != null) {
                    drafts.addAll(produced);
                }
            } catch (Exception ex) {
                lambdaContext.logException(LogType.SERVICE,
                        "Notification producer failed and was skipped. producer=" + describe(producer), ex);
            }
        }
        return drafts;
    }

    private static String describe(NotificationProducer producer) {
        try {
            return String.valueOf(producer.type());
        } catch (RuntimeException ex) {
            return producer.getClass().getSimpleName();
        }
    }

    private static String stringValue(Map<String, Object> event) {
        if (event == null) {
            return null;
        }
        Object value = event.get(EVENT_WINDOW_KEY);
        return value != null ? value.toString() : null;
    }

    private static ZoneId resolveZone() {
        String configured = System.getenv("APP_TIMEZONE");
        try {
            return ZoneId.of(configured == null || configured.isBlank() ? DEFAULT_TIMEZONE : configured);
        } catch (RuntimeException ex) {
            return ZoneId.of(DEFAULT_TIMEZONE);
        }
    }

    private record Dependencies(List<NotificationProducer> producers,
                                NotificationDispatcher dispatcher,
                                LambdaContext lambdaContext,
                                ZoneId zoneId) {

        static Dependencies fromSpringContext() {
            ConfigurableApplicationContext context = SpringContextHolder.CONTEXT;
            return new Dependencies(
                    new ArrayList<>(context.getBeansOfType(NotificationProducer.class).values()),
                    context.getBean(NotificationDispatcher.class),
                    context.getBean(LambdaContext.class),
                    resolveZone());
        }
    }

    private static final class SpringContextHolder {
        private static final ConfigurableApplicationContext CONTEXT = new SpringApplicationBuilder(Application.class)
                .properties("server.port=0", "spring.main.banner-mode=off")
                .run();
    }
}
