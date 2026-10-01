package com.cattle.tasks.planner;

import com.cattle.enums.BovineEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@Tag("fast")
@DisplayName("GestationCalendar Tests")
class GestationCalendarTest {

    private static PlannerEvent ev(String id, BovineEventType type, String date, Map<String, Object> payload) {
        return new PlannerEvent(id, type, LocalDate.parse(date), date + "T12:00:00Z", payload);
    }

    @Test
    @DisplayName("Parto desde servicio: fecha estimada del servicio o servicio + gestación")
    void expectedCalving() {
        assertThat(GestationCalendar.expectedCalving(ev("S", BovineEventType.MONTA, "2026-10-01", Map.of()), 279))
                .isEqualTo(LocalDate.parse("2027-07-07"));
        assertThat(GestationCalendar.expectedCalving(
                ev("S", BovineEventType.MONTA, "2026-10-01", Map.of("estimatedCalvingDate", "2027-07-15")), 279))
                .isEqualTo(LocalDate.parse("2027-07-15"));
    }

    @Test
    @DisplayName("Estimación del diagnóstico: servicio y parto estimados")
    void gestationEstimate() {
        PlannerEvent d = ev("D", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("gestationDays", 90));
        assertThat(GestationCalendar.estimatedServiceDate(d)).isEqualTo(LocalDate.parse("2026-08-12"));
        assertThat(GestationCalendar.calvingFromGestationEstimate(d, 279)).isEqualTo(LocalDate.parse("2027-05-18"));
        assertThat(GestationCalendar.calvingFromGestationEstimate(
                ev("D", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of()), 279)).isNull();
    }

    @Test
    @DisplayName("Último servicio: el más reciente tras el último parto y antes del evento")
    void lastServiceBefore() {
        PlannerEvent s1 = ev("S1", BovineEventType.MONTA, "2025-06-01", Map.of());
        PlannerEvent p1 = ev("P1", BovineEventType.PARTO, "2026-03-06", Map.of());
        PlannerEvent s2 = ev("S2", BovineEventType.INSEMINACION, "2026-06-01", Map.of());
        PlannerEvent s3 = ev("S3", BovineEventType.INSEMINACION, "2026-06-22", Map.of());
        PlannerEvent d = ev("D", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-08-01", Map.of());
        PlannerEvent later = ev("S4", BovineEventType.MONTA, "2026-09-01", Map.of());

        assertThat(GestationCalendar.lastServiceBefore(List.of(later, d, s3, s2, p1, s1), d)).contains(s3);
        assertThat(GestationCalendar.lastServiceBefore(List.of(s1, p1, d), d)).isEmpty();
    }
}
