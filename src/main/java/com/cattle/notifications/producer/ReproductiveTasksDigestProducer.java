package com.cattle.notifications.producer;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.ScheduleWindow;
import com.cattle.tasks.dto.ReproductiveTaskDTO;
import com.cattle.tasks.service.ReproductiveTaskQueryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resumen diario de la agenda reproductiva (HU-20260929, CA5, D3).
 * <p>
 * Corre en la ventana {@link ScheduleWindow#MORNING} (09:00, schedule ya existente). Cuenta
 * las tareas abiertas que vencen hasta hoy: las que siguen dentro de su ventana son "para
 * hoy" y las que ya la pasaron son "vencidas". Si no hay ninguna, no notifica.
 */
@Component
public class ReproductiveTasksDigestProducer implements NotificationProducer {

    static final String DEEPLINK = "/agenda";
    static final String DEDUPE_PREFIX = NotificationType.REPRODUCTIVE_TASKS_DIGEST.name() + "#";

    private final ReproductiveTaskQueryService queryService;
    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;
    /** Misma finca de contexto que el batch de tareas (H9). */
    private final String farmId;

    public ReproductiveTasksDigestProducer(ReproductiveTaskQueryService queryService,
                                           ObjectMapper objectMapper,
                                           LambdaContext lambdaContext,
                                           @Value("${reproductive-tasks.farm-id:F001}") String farmId) {
        this.queryService = queryService;
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
        this.farmId = farmId;
    }

    @Override
    public NotificationType type() {
        return NotificationType.REPRODUCTIVE_TASKS_DIGEST;
    }

    @Override
    public ScheduleWindow window() {
        return ScheduleWindow.MORNING;
    }

    @Override
    public List<NotificationDraft> evaluate(ZonedDateTime now) {
        LocalDate today = now.toLocalDate();
        List<ReproductiveTaskDTO> dueUpToToday = queryService.farmAgenda(farmId, today, today);
        long overdue = dueUpToToday.stream().filter(ReproductiveTaskDTO::isOverdue).count();
        long forToday = dueUpToToday.size() - overdue;
        if (forToday == 0 && overdue == 0) {
            return List.of();
        }

        return List.of(NotificationDraft.builder()
                .farmId(farmId)
                .type(NotificationType.REPRODUCTIVE_TASKS_DIGEST)
                .title("Agenda reproductiva de hoy")
                .body(body(forToday, overdue))
                .deeplink(DEEPLINK)
                .dataJson(dataJson(today, forToday, overdue))
                .dedupeKey(DEDUPE_PREFIX + farmId + "#" + today)
                .build());
    }

    static String body(long forToday, long overdue) {
        String todayPart = forToday == 1 ? "1 tarea para hoy" : forToday + " tareas para hoy";
        String overduePart = overdue == 1 ? "1 vencida" : overdue + " vencidas";
        return todayPart + ", " + overduePart + ".";
    }

    private String dataJson(LocalDate date, long forToday, long overdue) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("date", date.toString());
        data.put("forToday", forToday);
        data.put("overdue", overdue);
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            lambdaContext.logException(LogType.SERVICE, "Could not serialize reproductive tasks digest data", ex);
            return "{}";
        }
    }
}
