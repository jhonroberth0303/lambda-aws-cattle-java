package com.cattle.tasks.service;

import com.cattle.config.AppProperties;
import com.cattle.config.LambdaContext;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.LogType;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.tasks.planner.PlannerEvent;
import com.cattle.utils.EventDates;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Convierte ítems del timeline en {@link PlannerEvent}: fecha operativa ({@link EventDates}) y
 * payload deserializado. Compartido por la agenda y la proyección de perfiles (HU-20260930).
 */
@Component
public class PlannerEventMapper {

    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;
    private final ZoneId zoneId;

    public PlannerEventMapper(ObjectMapper objectMapper, LambdaContext lambdaContext, AppProperties appProperties) {
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
        this.zoneId = ZoneId.of(appProperties.getTimezone());
    }

    public List<PlannerEvent> toPlannerEvents(List<BovineEventItem> items) {
        return items.stream().map(this::toPlannerEvent).filter(Objects::nonNull).toList();
    }

    /** {@code null} si el tipo no es un evento bovino conocido o no tiene fecha. */
    public PlannerEvent toPlannerEvent(BovineEventItem item) {
        BovineEventType type;
        try {
            type = BovineEventType.valueOf(item.getEventType());
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
        LocalDate date = EventDates.toOperationalDate(item.getEventAt(), zoneId);
        if (date == null) {
            return null;
        }
        return new PlannerEvent(item.getEventId(), type, date, item.getCreatedAt(), parsePayload(item));
    }

    private Map<String, Object> parsePayload(BovineEventItem item) {
        if (item.getPayloadJson() == null || item.getPayloadJson().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(item.getPayloadJson(), new TypeReference<Map<String, Object>>() { });
        } catch (Exception ex) {
            lambdaContext.logException(LogType.SERVICE, "Payload de evento ilegible; se ignora. eventId="
                    + item.getEventId(), ex);
            return Map.of();
        }
    }
}
