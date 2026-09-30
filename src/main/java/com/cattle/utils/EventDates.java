package com.cattle.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Deriva el día calendario operativo de un {@code eventAt} de evento.
 * <p>
 * {@code BovineEventProcessor.resolveEventAt} guarda una fecha elegida por el usuario
 * ({@code yyyy-MM-dd}) como medianoche UTC; sin fecha usa {@code Instant.now()}. Convertir
 * la medianoche UTC a America/Bogota (UTC-5) da el día anterior, así que:
 * <ul>
 *   <li>instante exactamente a medianoche UTC → fecha explícita: se lee en UTC;</li>
 *   <li>cualquier otro instante → hora real de registro: se lee en la zona operativa.</li>
 * </ul>
 * HU-20260929-tareas-ciclo-reproductivo, RT1.
 */
public final class EventDates {

    private EventDates() {
    }

    public static LocalDate toOperationalDate(Instant eventAt, ZoneId operationalZone) {
        if (eventAt == null) {
            return null;
        }
        if (eventAt.atOffset(ZoneOffset.UTC).toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return eventAt.atOffset(ZoneOffset.UTC).toLocalDate();
        }
        return eventAt.atZone(operationalZone).toLocalDate();
    }
}
