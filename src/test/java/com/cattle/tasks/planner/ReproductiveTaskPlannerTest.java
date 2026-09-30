package com.cattle.tasks.planner;

import com.cattle.enums.BovineEventType;
import com.cattle.tasks.ReproductiveTaskRule;
import com.cattle.tasks.ReproductiveTaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Escenarios de HU-20260929-tareas-ciclo-reproductivo sobre el planner puro.
 */
@Tag("unit")
@Tag("fast")
@DisplayName("ReproductiveTaskPlanner Tests")
class ReproductiveTaskPlannerTest {

    private static final ReproductiveTaskSettings SETTINGS = ReproductiveTaskSettings.defaults();
    private static final BovineReproductiveContext LACTATING_COW =
            new BovineReproductiveContext("7", "F001", true, true, true);
    private static final BovineReproductiveContext DRY_COW =
            new BovineReproductiveContext("7", "F001", true, true, false);

    private final ReproductiveTaskPlanner planner = new ReproductiveTaskPlanner();
    private final List<PlannerEvent> events = new ArrayList<>();

    private PlannerEvent event(String id, BovineEventType type, String date, Map<String, Object> payload) {
        PlannerEvent e = new PlannerEvent(id, type, LocalDate.parse(date), date + "T12:00:00Z#" + id, payload);
        events.add(e);
        return e;
    }

    private PlannerEvent event(String id, BovineEventType type, String date) {
        return event(id, type, date, Map.of());
    }

    private ReproductivePlan plan(BovineReproductiveContext context, String today) {
        return planner.plan(context, events, SETTINGS, LocalDate.parse(today));
    }

    private static PlannedTask task(ReproductivePlan plan, ReproductiveTaskRule rule, String origin) {
        return plan.tasks().stream()
                .filter(t -> t.rule() == rule && t.originEventId().equals(origin))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No existe la tarea " + rule + "#" + origin));
    }

    // --- Flujo principal ---------------------------------------------------

    @Test
    @DisplayName("CA1: servicio genera DIAGNOSTICAR_PRENEZ a +40 días y abre el semáforo en servicio 1")
    void service_createsDiagnosisAndOpensFollowUp() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");

        ReproductivePlan plan = plan(LACTATING_COW, "2026-10-01");

        PlannedTask diagnosis = task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1");
        assertThat(diagnosis.dueDate()).isEqualTo(LocalDate.parse("2026-11-10"));
        assertThat(diagnosis.status()).isEqualTo(ReproductiveTaskStatus.PENDIENTE);
        assertThat(plan.tasks()).hasSize(1);
        assertThat(plan.followUp().open()).isTrue();
        assertThat(plan.followUp().serviceNumber()).isEqualTo(1);
        assertThat(plan.followUp().lastServiceDate()).isEqualTo(LocalDate.parse("2026-10-01"));
    }

    @Test
    @DisplayName("CA2: diagnóstico positivo en vaca lactante programa parto, preparto y secado; cierra el semáforo")
    void positiveDiagnosis_schedulesGestation() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-11-10");

        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-07-07"));
        assertThat(task(plan, ReproductiveTaskRule.R2_PREPARTUM, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-06-16"));
        assertThat(task(plan, ReproductiveTaskRule.R2_DRY_OFF, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-05-08"));
        assertThat(plan.followUp().open()).isFalse();
        assertThat(plan.followUp().closedByEventId()).isEqualTo("D1");
    }

    @Test
    @DisplayName("P4: sin lactancia al diagnosticar no se programa SECAR")
    void positiveDiagnosis_dryCow_hasNoDryOff() {
        event("S1", BovineEventType.MONTA, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));

        ReproductivePlan plan = plan(DRY_COW, "2026-11-10");

        assertThat(plan.tasks()).noneMatch(t -> t.rule() == ReproductiveTaskRule.R2_DRY_OFF);
        assertThat(plan.tasks()).anyMatch(t -> t.rule() == ReproductiveTaskRule.R2_CALVING);
    }

    @Test
    @DisplayName("La fecha estimada de parto del servicio tiene prioridad sobre servicio + gestación")
    void positiveDiagnosis_usesEstimatedCalvingDateFromService() {
        event("S1", BovineEventType.MONTA, "2026-10-01", Map.of("estimatedCalvingDate", "2027-07-15"));
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));

        ReproductivePlan plan = plan(DRY_COW, "2026-11-10");

        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-07-15"));
    }

    @Test
    @DisplayName("CA3: parto cumple PARTO_ESPERADO, cancela SECAR pendiente y programa posparto y servir")
    void calving_closesGestationAndSchedulesPostpartum() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));
        event("P1", BovineEventType.PARTO, "2027-07-05");

        ReproductivePlan plan = plan(LACTATING_COW, "2027-07-05");

        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(task(plan, ReproductiveTaskRule.R2_DRY_OFF, "D1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        assertThat(task(plan, ReproductiveTaskRule.R2_PREPARTUM, "D1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        assertThat(task(plan, ReproductiveTaskRule.R5_POSTPARTUM_CHECK, "P1").dueDate()).isEqualTo(LocalDate.parse("2027-07-26"));
        assertThat(task(plan, ReproductiveTaskRule.R5_SERVE, "P1").dueDate()).isEqualTo(LocalDate.parse("2027-09-03"));
    }

    @Test
    @DisplayName("CA6: los eventos nuevos cumplen sus tareas (PREPARTO, SECADO, CONTROL_POSPARTO)")
    void newEvents_completeTheirTasks() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));
        event("SEC", BovineEventType.SECADO, "2027-05-08");
        event("PRE", BovineEventType.PREPARTO, "2027-06-16");
        event("P1", BovineEventType.PARTO, "2027-07-05");
        event("CP", BovineEventType.CONTROL_POSPARTO, "2027-07-26", Map.of("uterineStatus", "NORMAL"));

        ReproductivePlan plan = plan(LACTATING_COW, "2027-07-26");

        assertThat(task(plan, ReproductiveTaskRule.R2_DRY_OFF, "D1").closedByEventId()).isEqualTo("SEC");
        assertThat(task(plan, ReproductiveTaskRule.R2_PREPARTUM, "D1").status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(task(plan, ReproductiveTaskRule.R5_POSTPARTUM_CHECK, "P1").status()).isEqualTo(ReproductiveTaskStatus.HECHA);
    }

    // --- Validaciones ------------------------------------------------------

    @Test
    @DisplayName("CA7: diagnóstico vacío cumple el diagnóstico, no crea gestación y el semáforo sigue abierto")
    void emptyDiagnosis_keepsFollowUpOpen() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "VACIA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-11-10");

        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(plan.tasks()).hasSize(1);
        assertThat(plan.followUp().open()).isTrue();
        assertThat(plan.followUp().lastServiceDate()).isEqualTo(LocalDate.parse("2026-10-01"));
    }

    @Test
    @DisplayName("CA8: diagnóstico dudoso programa rediagnóstico a +15 días")
    void doubtfulDiagnosis_schedulesRecheck() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "DUDOSA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-11-10");

        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(task(plan, ReproductiveTaskRule.R4_RECHECK, "D1").dueDate()).isEqualTo(LocalDate.parse("2026-11-25"));
    }

    @Test
    @DisplayName("CA9: celo tras servicio cancela el diagnóstico y crea SERVIR_EN_CELO; servir al día siguiente la cumple")
    void heatAfterService_repeatsCycle() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("C1", BovineEventType.CELO, "2026-10-21", Map.of("intensity", "FUERTE"));

        ReproductivePlan onHeatDay = plan(LACTATING_COW, "2026-10-21");
        assertThat(task(onHeatDay, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").status())
                .isEqualTo(ReproductiveTaskStatus.CANCELADA);
        PlannedTask serve = task(onHeatDay, ReproductiveTaskRule.R7_SERVE_IN_HEAT, "C1");
        assertThat(serve.dueDate()).isEqualTo(LocalDate.parse("2026-10-21"));
        assertThat(serve.status()).isEqualTo(ReproductiveTaskStatus.PENDIENTE);

        event("S2", BovineEventType.INSEMINACION, "2026-10-22");
        ReproductivePlan afterService = plan(LACTATING_COW, "2026-10-22");
        PlannedTask served = task(afterService, ReproductiveTaskRule.R7_SERVE_IN_HEAT, "C1");
        assertThat(served.status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(served.lateCompletion()).isFalse();
        assertThat(afterService.followUp().serviceNumber()).isEqualTo(2);
        assertThat(afterService.followUp().lastServiceDate()).isEqualTo(LocalDate.parse("2026-10-22"));
    }

    @Test
    @DisplayName("CA10: VENTA cancela todas las pendientes y cierra el semáforo")
    void exit_cancelsEverything() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("V1", BovineEventType.VENTA, "2026-10-10");

        ReproductivePlan plan = plan(LACTATING_COW, "2026-10-10");

        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        assertThat(plan.followUp().open()).isFalse();
    }

    @Test
    @DisplayName("CA11: un macho no tiene tareas")
    void male_hasNoTasks() {
        event("S1", BovineEventType.MONTA, "2026-10-01");

        ReproductivePlan plan = plan(new BovineReproductiveContext("9", "F001", false, true, false), "2026-10-01");

        assertThat(plan.tasks()).isEmpty();
        assertThat(plan.followUp()).isNull();
    }

    @Test
    @DisplayName("G4: bovino inactivo sin evento de salida cancela las abiertas y cierra el semáforo")
    void inactive_cancelsOpenTasks() {
        event("S1", BovineEventType.MONTA, "2026-10-01");

        ReproductivePlan plan = plan(new BovineReproductiveContext("7", "F001", true, false, false), "2026-10-05");

        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        assertThat(plan.followUp().open()).isFalse();
    }

    @Test
    @DisplayName("CA12: los días de gestación se toman de la configuración de la finca")
    void settings_changeGestation() {
        ReproductiveTaskSettings custom = new ReproductiveTaskSettings(285, 40, 15, 60, 21, 21, 60, 42, 63, 3);
        event("S1", BovineEventType.MONTA, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));

        ReproductivePlan plan = planner.plan(DRY_COW, events, custom, LocalDate.parse("2026-11-10"));

        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-07-13"));
    }

    // --- Casos extremos ----------------------------------------------------

    @Test
    @DisplayName("CA13: la tarea vence al pasar su ventana y el cumplimiento tardío queda HECHA marcada tarde")
    void overdue_thenLateCompletion() {
        event("P1", BovineEventType.PARTO, "2027-07-05");

        assertThat(task(plan(LACTATING_COW, "2027-07-26"), ReproductiveTaskRule.R5_POSTPARTUM_CHECK, "P1").status())
                .isEqualTo(ReproductiveTaskStatus.PENDIENTE);
        assertThat(task(plan(LACTATING_COW, "2027-07-27"), ReproductiveTaskRule.R5_POSTPARTUM_CHECK, "P1").status())
                .isEqualTo(ReproductiveTaskStatus.VENCIDA);

        event("CP", BovineEventType.CONTROL_POSPARTO, "2027-07-30");
        PlannedTask late = task(plan(LACTATING_COW, "2027-07-30"), ReproductiveTaskRule.R5_POSTPARTUM_CHECK, "P1");
        assertThat(late.status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(late.lateCompletion()).isTrue();
    }

    @Test
    @DisplayName("CA14: servicio registrado con fecha de hace 50 días nace con el diagnóstico VENCIDA")
    void retroactiveService_isOverdue() {
        event("S1", BovineEventType.MONTA, "2026-08-12");

        PlannedTask diagnosis = task(plan(LACTATING_COW, "2026-10-01"), ReproductiveTaskRule.R1_DIAGNOSIS, "S1");

        assertThat(diagnosis.status()).isEqualTo(ReproductiveTaskStatus.VENCIDA);
    }

    @Test
    @DisplayName("CA16: nuevo servicio cancela el diagnóstico anterior y crea el del nuevo servicio")
    void newService_replacesPreviousDiagnosis() {
        event("S1", BovineEventType.MONTA, "2026-10-01");
        event("S2", BovineEventType.MONTA, "2026-10-22");

        ReproductivePlan plan = plan(LACTATING_COW, "2026-10-22");

        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S2").status()).isEqualTo(ReproductiveTaskStatus.PENDIENTE);
        assertThat(plan.followUp().serviceNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("CA17: diagnóstico positivo sin servicio no genera gestación y lo señala")
    void positiveDiagnosisWithoutService_isFlagged() {
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-11-10");

        assertThat(plan.tasks()).isEmpty();
        assertThat(plan.diagnosisWithoutService()).isTrue();
    }

    @Test
    @DisplayName("P1: sin servicio, los días de gestación estimados programan la gestación")
    void positiveDiagnosisWithGestationEstimate_schedulesGestation() {
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA", "gestationDays", 90));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-11-10");

        // 2026-11-10 − 90 + 279 = 2027-05-18
        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-05-18"));
        assertThat(task(plan, ReproductiveTaskRule.R2_PREPARTUM, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-04-27"));
        assertThat(task(plan, ReproductiveTaskRule.R2_DRY_OFF, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-03-19"));
        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").currentCycle()).isTrue();
        assertThat(plan.diagnosisWithoutService()).isFalse();
    }

    @Test
    @DisplayName("P1: si hay servicio registrado, gana sobre la estimación del diagnóstico")
    void serviceDate_winsOverGestationEstimate() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA", "gestationDays", "90"));

        assertThat(task(plan(DRY_COW, "2026-11-10"), ReproductiveTaskRule.R2_CALVING, "D1").dueDate())
                .isEqualTo(LocalDate.parse("2027-07-07"));
    }

    @Test
    @DisplayName("P1: una estimación inválida se ignora y queda señalado como sin servicio")
    void invalidGestationEstimate_isIgnored() {
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA", "gestationDays", "abc"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-11-10");

        assertThat(plan.tasks()).isEmpty();
        assertThat(plan.diagnosisWithoutService()).isTrue();
    }

    @Test
    @DisplayName("CA18: solo las tareas del último ciclo se marcan como del ciclo actual")
    void onlyLastCycleIsCurrent() {
        event("S1", BovineEventType.MONTA, "2025-06-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2025-07-11", Map.of("result", "PRENIADA"));
        event("P1", BovineEventType.PARTO, "2026-03-06");

        ReproductivePlan plan = plan(LACTATING_COW, "2026-03-10");

        assertThat(task(plan, ReproductiveTaskRule.R1_DIAGNOSIS, "S1").currentCycle()).isFalse();
        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").currentCycle()).isFalse();
        assertThat(task(plan, ReproductiveTaskRule.R5_SERVE, "P1").currentCycle()).isTrue();
        assertThat(task(plan, ReproductiveTaskRule.R5_POSTPARTUM_CHECK, "P1").currentCycle()).isTrue();
    }

    @Test
    @DisplayName("CA21: tres servicios desde el parto sin preñez dejan el contador en 3")
    void thirdService_countsForRepeatBreeder() {
        event("P1", BovineEventType.PARTO, "2026-01-01");
        event("S1", BovineEventType.MONTA, "2026-03-10");
        event("S2", BovineEventType.MONTA, "2026-03-31");
        event("S3", BovineEventType.MONTA, "2026-04-21");

        ReproductivePlan plan = plan(LACTATING_COW, "2026-04-21");

        assertThat(plan.followUp().serviceNumber()).isEqualTo(3);
        assertThat(ServiceFollowUpEvaluator.evaluate(plan.followUp().lastServiceDate(),
                plan.followUp().serviceNumber(), LocalDate.parse("2026-04-21"), SETTINGS).repeatBreeder()).isTrue();
    }

    @Test
    @DisplayName("El contador de servicios se reinicia con el parto")
    void calving_resetsServiceCounter() {
        event("S1", BovineEventType.MONTA, "2025-06-01");
        event("S2", BovineEventType.MONTA, "2025-06-22");
        event("P1", BovineEventType.PARTO, "2026-03-27");
        event("S3", BovineEventType.MONTA, "2026-06-01");

        assertThat(plan(LACTATING_COW, "2026-06-01").followUp().serviceNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("CA22: celo dentro del periodo voluntario de espera no genera SERVIR_EN_CELO")
    void heatWithinWaitingPeriod_isOnlyRecorded() {
        event("P1", BovineEventType.PARTO, "2026-01-01");
        event("C1", BovineEventType.CELO, "2026-01-31", Map.of("intensity", "DEBIL"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-01-31");

        assertThat(plan.tasks()).noneMatch(t -> t.rule() == ReproductiveTaskRule.R7_SERVE_IN_HEAT);
    }

    @Test
    @DisplayName("Celo fuera del periodo de espera genera SERVIR_EN_CELO")
    void heatAfterWaitingPeriod_createsServeInHeat() {
        event("P1", BovineEventType.PARTO, "2026-01-01");
        event("C1", BovineEventType.CELO, "2026-03-05", Map.of("intensity", "FUERTE"));

        assertThat(task(plan(LACTATING_COW, "2026-03-05"), ReproductiveTaskRule.R7_SERVE_IN_HEAT, "C1").status())
                .isEqualTo(ReproductiveTaskStatus.PENDIENTE);
    }

    @Test
    @DisplayName("CA23 / C6: SERVIR_EN_CELO sin servicio pasado su día de gracia queda VENCIDA")
    void missedHeat_isOverdue() {
        event("S1", BovineEventType.MONTA, "2026-10-01");
        event("C1", BovineEventType.CELO, "2026-10-21", Map.of("intensity", "FUERTE"));

        assertThat(task(plan(LACTATING_COW, "2026-10-22"), ReproductiveTaskRule.R7_SERVE_IN_HEAT, "C1").status())
                .isEqualTo(ReproductiveTaskStatus.PENDIENTE);
        assertThat(task(plan(LACTATING_COW, "2026-10-23"), ReproductiveTaskRule.R7_SERVE_IN_HEAT, "C1").status())
                .isEqualTo(ReproductiveTaskStatus.VENCIDA);
    }

    @Test
    @DisplayName("Aborto cancela la gestación, programa servir y reinicia el ciclo")
    void abortion_cancelsGestation() {
        event("S1", BovineEventType.MONTA, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));
        event("A1", BovineEventType.ABORTO, "2027-01-15");

        ReproductivePlan plan = plan(DRY_COW, "2027-01-15");

        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        assertThat(task(plan, ReproductiveTaskRule.R6_SERVE, "A1").dueDate()).isEqualTo(LocalDate.parse("2027-03-16"));
    }

    @Test
    @DisplayName("P6: el aborto programa control post-aborto a +21 días, que se cumple con CONTROL_POSPARTO")
    void abortion_schedulesPostAbortionCheck() {
        event("S1", BovineEventType.MONTA, "2026-10-01");
        event("A1", BovineEventType.ABORTO, "2027-01-15");

        PlannedTask check = task(plan(DRY_COW, "2027-01-15"), ReproductiveTaskRule.R6_POSTABORTION_CHECK, "A1");
        assertThat(check.dueDate()).isEqualTo(LocalDate.parse("2027-02-05"));
        assertThat(check.status()).isEqualTo(ReproductiveTaskStatus.PENDIENTE);

        event("CP", BovineEventType.CONTROL_POSPARTO, "2027-02-04", Map.of("uterineStatus", "NORMAL"));
        PlannedTask done = task(plan(DRY_COW, "2027-02-04"), ReproductiveTaskRule.R6_POSTABORTION_CHECK, "A1");
        assertThat(done.status()).isEqualTo(ReproductiveTaskStatus.HECHA);
        assertThat(done.closedByEventId()).isEqualTo("CP");
    }

    // --- Correcciones de revisión -----------------------------------------

    @Test
    @DisplayName("H1: reconfirmar la preñez no duplica las tareas de gestación")
    void pregnancyReconfirmation_doesNotDuplicateGestation() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));
        event("D2", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-12-20", Map.of("result", "PRENIADA", "method", "ECOGRAFIA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-12-20");

        assertThat(plan.tasks()).filteredOn(t -> t.rule() == ReproductiveTaskRule.R2_CALVING).hasSize(1);
        assertThat(plan.tasks()).filteredOn(t -> t.rule() == ReproductiveTaskRule.R2_PREPARTUM).hasSize(1);
        assertThat(plan.tasks()).filteredOn(t -> t.rule() == ReproductiveTaskRule.R2_DRY_OFF).hasSize(1);
        assertThat(plan.tasks()).noneMatch(t -> t.originEventId().equals("D2"));
        assertThat(plan.followUp().open()).isFalse();
    }

    @Test
    @DisplayName("H2: pérdida embrionaria (PRENIADA → VACIA) reabre el semáforo desde el último servicio")
    void pregnancyLoss_reopensFollowUp() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));
        event("D2", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-12-20", Map.of("result", "VACIA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-12-20");

        assertThat(plan.followUp().open()).isTrue();
        assertThat(plan.followUp().closedByEventId()).isNull();
        assertThat(plan.followUp().lastServiceDate()).isEqualTo(LocalDate.parse("2026-10-01"));
        assertThat(plan.followUp().serviceNumber()).isEqualTo(1);
        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
    }

    @Test
    @DisplayName("H1/H2: una nueva preñez tras la pérdida vuelve a programar la gestación y cierra el semáforo")
    void pregnancyAfterLoss_schedulesGestationAgain() {
        event("S1", BovineEventType.INSEMINACION, "2026-10-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-11-10", Map.of("result", "PRENIADA"));
        event("D2", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-12-01", Map.of("result", "VACIA"));
        event("D3", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-12-20", Map.of("result", "PRENIADA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-12-20");

        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D3").status()).isEqualTo(ReproductiveTaskStatus.PENDIENTE);
        assertThat(plan.followUp().open()).isFalse();
        assertThat(plan.followUp().closedByEventId()).isEqualTo("D3");
    }

    @Test
    @DisplayName("H2: un VACIA sin preñez previa no altera un semáforo ya abierto ni reabre uno cerrado por parto")
    void emptyDiagnosis_doesNotReopenAfterCalving() {
        event("S1", BovineEventType.MONTA, "2025-06-01");
        event("P1", BovineEventType.PARTO, "2026-03-06");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-04-01", Map.of("result", "VACIA"));

        assertThat(plan(LACTATING_COW, "2026-04-01").followUp().open()).isFalse();
    }

    @Test
    @DisplayName("H8: un nuevo servicio cancela la gestación vieja sin parto registrado y la nueva preñez se programa")
    void newService_cancelsStaleGestation() {
        event("S1", BovineEventType.MONTA, "2025-06-01");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2025-07-11", Map.of("result", "PRENIADA"));
        // El parto real (~2026-03) nunca se registró.
        event("S2", BovineEventType.MONTA, "2026-06-01");
        event("D2", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-07-11", Map.of("result", "PRENIADA"));

        ReproductivePlan plan = plan(LACTATING_COW, "2026-07-11");

        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").closedByEventId()).isEqualTo("S2");
        assertThat(task(plan, ReproductiveTaskRule.R2_PREPARTUM, "D1").status()).isEqualTo(ReproductiveTaskStatus.CANCELADA);
        PlannedTask calving = task(plan, ReproductiveTaskRule.R2_CALVING, "D2");
        assertThat(calving.status()).isEqualTo(ReproductiveTaskStatus.PENDIENTE);
        assertThat(calving.dueDate()).isEqualTo(LocalDate.parse("2027-03-07"));
        assertThat(calving.currentCycle()).isTrue();
    }

    @Test
    @DisplayName("H4: el aborto no reinicia el contador de servicios; solo el parto lo hace")
    void abortion_keepsServiceCounter() {
        event("P1", BovineEventType.PARTO, "2026-01-01");
        event("S1", BovineEventType.MONTA, "2026-03-10");
        event("D1", BovineEventType.DIAGNOSTICO_PRENEZ, "2026-04-19", Map.of("result", "PRENIADA"));
        event("A1", BovineEventType.ABORTO, "2026-06-01");
        event("S2", BovineEventType.MONTA, "2026-08-05");

        assertThat(plan(LACTATING_COW, "2026-08-05").followUp().serviceNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("Eventos del mismo día se ordenan por registro, no por su posición en la lista")
    void sameDayEvents_areOrderedByCreatedAt() {
        events.add(new PlannerEvent("D1", BovineEventType.DIAGNOSTICO_PRENEZ, LocalDate.parse("2026-11-10"),
                "2026-11-10T15:00:00Z", Map.of("result", "PRENIADA")));
        events.add(new PlannerEvent("S1", BovineEventType.MONTA, LocalDate.parse("2026-11-10"),
                "2026-11-10T09:00:00Z", Map.of()));

        ReproductivePlan plan = plan(DRY_COW, "2026-11-10");

        assertThat(plan.diagnosisWithoutService()).isFalse();
        assertThat(task(plan, ReproductiveTaskRule.R2_CALVING, "D1").dueDate()).isEqualTo(LocalDate.parse("2027-08-16"));
    }
}
