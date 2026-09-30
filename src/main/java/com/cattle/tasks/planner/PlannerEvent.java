package com.cattle.tasks.planner;

import com.cattle.enums.BovineEventType;

import java.time.LocalDate;
import java.util.Map;

/**
 * Evento del timeline ya normalizado para el planner: fecha operativa calculada
 * ({@code EventDates}) y payload deserializado.
 *
 * @param createdAt desempate de eventos del mismo día (orden de registro)
 */
public record PlannerEvent(String eventId, BovineEventType type, LocalDate date, String createdAt,
                           Map<String, Object> payload) {

    public PlannerEvent {
        payload = payload == null ? Map.of() : payload;
    }

    public String payloadString(String key) {
        Object value = payload.get(key);
        return value == null || value.toString().isBlank() ? null : value.toString().trim();
    }

    /** Entero positivo del payload (acepta número o texto numérico); {@code null} si falta o no es válido. */
    public Integer payloadPositiveInt(String key) {
        String raw = payloadString(key);
        if (raw == null) {
            return null;
        }
        try {
            long value = Math.round(Double.parseDouble(raw));
            return value > 0 && value <= Integer.MAX_VALUE ? (int) value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
