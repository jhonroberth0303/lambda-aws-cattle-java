package com.cattle.notifications;

import java.util.Optional;

/**
 * Ventana horaria en la que corre un productor de notificaciones.
 * <p>
 * EventBridge Scheduler dispara un schedule por ventana e invoca la Lambda con
 * {@code {"window":"MORNING"}}. El handler solo ejecuta los productores de esa ventana.
 */
public enum ScheduleWindow {

    /** Primera hora del día (~06:00). Chequeos de sanidad, vencimientos. */
    EARLY,

    /** Media mañana (~09:00). Recordatorio del ordeño AM. */
    MORNING,

    /** Mediodía (~13:00). Estado de potreros, descansos listos. */
    MIDDAY,

    /** Tarde (~15:00). Recordatorio del ordeño PM. */
    AFTERNOON;

    /**
     * Resuelve el nombre recibido en el evento del scheduler, tolerando espacios y mayúsculas.
     *
     * @param raw valor crudo del campo {@code window} del evento; puede ser {@code null}
     * @return la ventana correspondiente, o vacío si no aplica (se interpreta como "todas")
     */
    public static Optional<ScheduleWindow> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(ScheduleWindow.valueOf(raw.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
