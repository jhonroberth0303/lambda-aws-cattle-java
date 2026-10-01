package com.cattle.tasks.planner;

import com.cattle.enums.BovineEventType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Reglas de fechas de la gestación compartidas por la agenda (planner) y los perfiles de
 * preñez (HU-20260930, D4), para que tarea y perfil calculen el mismo parto.
 */
public final class GestationCalendar {

    static final String CALVING_ESTIMATE_FIELD = "estimatedCalvingDate";
    static final String GESTATION_ESTIMATE_FIELD = "gestationDays";

    private static final Set<BovineEventType> SERVICE_EVENTS =
            EnumSet.of(BovineEventType.MONTA, BovineEventType.INSEMINACION);
    private static final Set<BovineEventType> GESTATION_END_EVENTS =
            EnumSet.of(BovineEventType.PARTO, BovineEventType.ABORTO);

    /** Orden cronológico del historial: fecha, luego orden de registro, luego id. */
    public static final Comparator<PlannerEvent> CHRONOLOGICAL = Comparator.comparing(PlannerEvent::date)
            .thenComparing(e -> e.createdAt() == null ? "" : e.createdAt())
            .thenComparing(e -> e.eventId() == null ? "" : e.eventId());

    private GestationCalendar() {
    }

    /** Parto esperado desde el servicio: su fecha estimada si la trae, si no servicio + gestación. */
    public static LocalDate expectedCalving(PlannerEvent service, int gestationDays) {
        String estimate = service.payloadString(CALVING_ESTIMATE_FIELD);
        if (estimate != null) {
            try {
                return LocalDate.parse(estimate);
            } catch (DateTimeParseException ignored) {
                // Fecha inválida en el payload: se usa la gestación configurada.
            }
        }
        return service.date().plusDays(gestationDays);
    }

    /** Servicio estimado desde el diagnóstico (P1): diagnóstico − días de gestación estimados. */
    public static LocalDate estimatedServiceDate(PlannerEvent diagnosis) {
        Integer estimatedDays = diagnosis.payloadPositiveInt(GESTATION_ESTIMATE_FIELD);
        return estimatedDays == null ? null : diagnosis.date().minusDays(estimatedDays);
    }

    /** Parto desde la estimación del diagnóstico (P1); {@code null} si no la trae. */
    public static LocalDate calvingFromGestationEstimate(PlannerEvent diagnosis, int gestationDays) {
        LocalDate service = estimatedServiceDate(diagnosis);
        return service == null ? null : service.plusDays(gestationDays);
    }

    /**
     * Último servicio de la gestación en curso antes de {@code event}: el más reciente
     * posterior al último parto o aborto.
     */
    public static Optional<PlannerEvent> lastServiceBefore(List<PlannerEvent> events, PlannerEvent event) {
        PlannerEvent lastService = null;
        for (PlannerEvent e : events.stream().sorted(CHRONOLOGICAL).toList()) {
            if (CHRONOLOGICAL.compare(e, event) >= 0) {
                break;
            }
            if (SERVICE_EVENTS.contains(e.type())) {
                lastService = e;
            } else if (GESTATION_END_EVENTS.contains(e.type())) {
                lastService = null;
            }
        }
        return Optional.ofNullable(lastService);
    }
}
