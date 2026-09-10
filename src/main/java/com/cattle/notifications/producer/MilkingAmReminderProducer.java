package com.cattle.notifications.producer;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.ScheduleWindow;
import com.cattle.notifications.producer.milking.MorningMilkingStatus;
import com.cattle.notifications.producer.milking.MorningMilkingStatusService;
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
 * Productor del recordatorio del ordeño de la mañana.
 * <p>
 * Corre en la ventana {@link ScheduleWindow#MORNING} (schedule a las 09:00). Si hay al menos
 * una vaca lactante sin ordeño AM registrado hoy, emite una notificación para la finca.
 */
@Component
public class MilkingAmReminderProducer implements NotificationProducer {

    static final String DEEPLINK = "/lactancia?focus=registro";
    static final String DEDUPE_PREFIX = NotificationType.MILKING_AM_REMINDER.name() + "#";

    private final MorningMilkingStatusService milkingStatusService;
    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;

    /** Sitio con el que se consultan los datos de ordeño (el módulo de ordeño usa "001"). */
    private final String siteId;
    /** Finca destinataria de la notificación; debe coincidir con el {@code farmId} que consulta la campana. */
    private final String farmId;

    public MilkingAmReminderProducer(MorningMilkingStatusService milkingStatusService,
                                     ObjectMapper objectMapper,
                                     LambdaContext lambdaContext,
                                     @Value("${notification.milking.site-id:001}") String siteId,
                                     @Value("${notification.milking.farm-id:F001}") String farmId) {
        this.milkingStatusService = milkingStatusService;
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
        this.siteId = siteId;
        this.farmId = farmId;
    }

    @Override
    public NotificationType type() {
        return NotificationType.MILKING_AM_REMINDER;
    }

    @Override
    public ScheduleWindow window() {
        return ScheduleWindow.MORNING;
    }

    @Override
    public List<NotificationDraft> evaluate(ZonedDateTime now) {
        LocalDate today = now.toLocalDate();
        MorningMilkingStatus status = milkingStatusService.statusFor(siteId, today);

        if (!status.hasPending()) {
            return List.of();
        }

        return List.of(NotificationDraft.builder()
                .farmId(farmId)
                .type(NotificationType.MILKING_AM_REMINDER)
                .title("Ordeño de la mañana pendiente")
                .body("Faltan " + status.pendingCows() + " vacas por registrar el ordeño de la mañana.")
                .deeplink(DEEPLINK)
                .dataJson(dataJson(today, status))
                .dedupeKey(DEDUPE_PREFIX + farmId + "#" + today)
                .build());
    }

    private String dataJson(LocalDate date, MorningMilkingStatus status) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("date", date.toString());
        data.put("pendingCows", status.pendingCows());
        data.put("lactatingCows", status.lactatingCows());
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            lambdaContext.logException(LogType.SERVICE, "Could not serialize milking reminder data", ex);
            return "{}";
        }
    }
}
