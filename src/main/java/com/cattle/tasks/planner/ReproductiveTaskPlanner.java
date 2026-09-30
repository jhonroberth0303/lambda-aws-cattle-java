package com.cattle.tasks.planner;

import com.cattle.enums.BovineEventType;
import com.cattle.tasks.ReproductiveTaskRule;
import com.cattle.tasks.ReproductiveTaskStatus;
import com.cattle.tasks.ReproductiveTaskType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calcula, sin I/O, las tareas reproductivas y el seguimiento post-servicio de una hembra
 * reproduciendo su historial de eventos en orden cronológico (replay).
 * <p>
 * Implementa las reglas R1–R7, C1–C6 y G1–G6 de HU-20260929-tareas-ciclo-reproductivo. Un
 * mismo historial produce siempre el mismo plan (salvo el vencimiento, que depende de
 * {@code today}); por eso el registro en línea, la reconciliación diaria y la carga inicial
 * comparten este único cálculo.
 */
@Component
public class ReproductiveTaskPlanner {

    /** Tolerancia del diagnóstico de preñez: ventana 35–45 días con el default de 40. */
    static final int DIAGNOSIS_WINDOW_DAYS = 5;
    /** SERVIR_EN_CELO: se puede servir el día del celo o el siguiente (IA AM/PM). */
    static final int HEAT_SERVICE_WINDOW_DAYS = 1;

    private static final String RESULT_PREGNANT = "PRENIADA";
    private static final String RESULT_OPEN = "VACIA";
    private static final String RESULT_DOUBTFUL = "DUDOSA";
    private static final String CALVING_ESTIMATE_FIELD = "estimatedCalvingDate";
    private static final String GESTATION_ESTIMATE_FIELD = "gestationDays";

    private static final Set<BovineEventType> SERVICE_EVENTS =
            EnumSet.of(BovineEventType.MONTA, BovineEventType.INSEMINACION);
    private static final Set<BovineEventType> CYCLE_OPENING_EVENTS = EnumSet.of(
            BovineEventType.MONTA, BovineEventType.INSEMINACION, BovineEventType.PARTO, BovineEventType.ABORTO);
    private static final Set<ReproductiveTaskType> GESTATION_TASKS = EnumSet.of(
            ReproductiveTaskType.PARTO_ESPERADO, ReproductiveTaskType.PREPARTO, ReproductiveTaskType.SECAR);

    public ReproductivePlan plan(BovineReproductiveContext context, List<PlannerEvent> events,
                                 ReproductiveTaskSettings settings, LocalDate today) {
        if (context == null || !context.female() || events == null || events.isEmpty()) {
            return ReproductivePlan.empty();
        }
        Replay replay = new Replay(context, settings);
        List<PlannerEvent> sorted = events.stream()
                .filter(e -> e.type() != null && e.date() != null)
                .sorted(Comparator.comparing(PlannerEvent::date)
                        .thenComparing(e -> e.createdAt() == null ? "" : e.createdAt())
                        .thenComparing(e -> e.eventId() == null ? "" : e.eventId()))
                .toList();
        for (PlannerEvent event : sorted) {
            replay.apply(event);
        }
        String cycleStartEventId = sorted.stream()
                .filter(e -> CYCLE_OPENING_EVENTS.contains(e.type()))
                .reduce((first, second) -> second)
                .map(PlannerEvent::eventId)
                .orElse(null);
        return replay.finish(sorted, cycleStartEventId, today);
    }

    /** Estado mutable del recorrido; vive solo durante un {@link #plan}. */
    private static final class Replay {

        private final BovineReproductiveContext context;
        private final ReproductiveTaskSettings settings;
        private final Map<String, MutableTask> tasks = new LinkedHashMap<>();

        private PlannerEvent lastService;
        private int servicesSinceCalving;
        private LocalDate lastCalvingOrAbortion;
        private LocalDate followUpServiceDate;
        private String followUpServiceEventId;
        private int followUpServiceNumber;
        private boolean followUpOpen;
        private String followUpClosedBy;
        /** El seguimiento lo cerró un diagnóstico positivo (y no un parto, aborto o salida). */
        private boolean followUpClosedByPregnancy;
        private boolean diagnosisWithoutService;

        Replay(BovineReproductiveContext context, ReproductiveTaskSettings settings) {
            this.context = context;
            this.settings = settings;
        }

        void apply(PlannerEvent e) {
            switch (e.type()) {
                case MONTA, INSEMINACION -> onService(e);
                case DIAGNOSTICO_PRENEZ -> onDiagnosis(e);
                case CELO -> onHeat(e);
                case PREPARTO -> complete(e, ReproductiveTaskType.PREPARTO);
                case SECADO -> complete(e, ReproductiveTaskType.SECAR);
                case CONTROL_POSPARTO -> complete(e, ReproductiveTaskType.CONTROL_POSPARTO);
                case PARTO -> onCalving(e);
                case ABORTO -> onAbortion(e);
                case VENTA, MUERTE -> onExit(e);
                default -> {
                    // Eventos sin efecto en el ciclo reproductivo.
                }
            }
        }

        /**
         * R1 + C3: un servicio cumple SERVIR/SERVIR_EN_CELO, cancela el diagnóstico anterior y
         * reinicia el semáforo. C3 ampliada (revisión H8): también cancela la gestación que
         * quedara abierta de un ciclo anterior (p. ej. un parto no registrado), porque un
         * servicio implica que esa gestación ya terminó o nunca existió.
         */
        private void onService(PlannerEvent e) {
            complete(e, ReproductiveTaskType.SERVIR, ReproductiveTaskType.SERVIR_EN_CELO);
            cancel(e, ReproductiveTaskType.DIAGNOSTICAR_PRENEZ, ReproductiveTaskType.PARTO_ESPERADO,
                    ReproductiveTaskType.PREPARTO, ReproductiveTaskType.SECAR);
            LocalDate due = e.date().plusDays(settings.pregnancyCheckDays());
            add(ReproductiveTaskRule.R1_DIAGNOSIS, e, due, due.plusDays(DIAGNOSIS_WINDOW_DAYS));

            servicesSinceCalving++;
            lastService = e;
            followUpServiceDate = e.date();
            followUpServiceEventId = e.eventId();
            followUpServiceNumber = servicesSinceCalving;
            followUpOpen = true;
            followUpClosedBy = null;
            followUpClosedByPregnancy = false;
        }

        /** R2, R3 (C1), R4. */
        private void onDiagnosis(PlannerEvent e) {
            complete(e, ReproductiveTaskType.DIAGNOSTICAR_PRENEZ);
            String result = e.payloadString("result");
            if (RESULT_PREGNANT.equalsIgnoreCase(result)) {
                // El servicio registrado es el dato más exacto; sin él, la estimación del diagnóstico (P1).
                LocalDate calving = lastService != null ? expectedCalving(lastService) : calvingFromGestationEstimate(e);
                if (calving == null) {
                    diagnosisWithoutService = true; // CA17: sin servicio ni estimación no hay gestación que programar.
                } else if (!hasOpen(ReproductiveTaskType.PARTO_ESPERADO)) {
                    // Revisión H1: una reconfirmación de la misma gestación no la programa otra vez.
                    add(ReproductiveTaskRule.R2_CALVING, e, calving, calving);
                    LocalDate prepartum = calving.minusDays(settings.prepartumBeforeCalvingDays());
                    add(ReproductiveTaskRule.R2_PREPARTUM, e, prepartum, prepartum);
                    if (context.lactating()) {
                        LocalDate dryOff = calving.minusDays(settings.dryOffBeforeCalvingDays());
                        add(ReproductiveTaskRule.R2_DRY_OFF, e, dryOff, dryOff);
                    }
                }
                closeFollowUp(e.eventId());
                followUpClosedByPregnancy = true;
            } else if (RESULT_OPEN.equalsIgnoreCase(result)) {
                cancel(e, GESTATION_TASKS.toArray(ReproductiveTaskType[]::new));
                reopenFollowUpAfterPregnancyLoss();
            } else if (RESULT_DOUBTFUL.equalsIgnoreCase(result)) {
                LocalDate due = e.date().plusDays(settings.pregnancyRecheckDays());
                add(ReproductiveTaskRule.R4_RECHECK, e, due, due);
            }
        }

        /** R7 + C5: celo tras un servicio = repitió; se sirve en ese celo. No aplica en el periodo de espera (CA22). */
        private void onHeat(PlannerEvent e) {
            boolean inWaitingPeriod = lastCalvingOrAbortion != null
                    && e.date().isBefore(lastCalvingOrAbortion.plusDays(settings.voluntaryWaitingDays()));
            boolean pregnant = hasOpen(ReproductiveTaskType.PARTO_ESPERADO);
            if (inWaitingPeriod || pregnant) {
                return;
            }
            cancel(e, ReproductiveTaskType.DIAGNOSTICAR_PRENEZ);
            add(ReproductiveTaskRule.R7_SERVE_IN_HEAT, e, e.date(), e.date().plusDays(HEAT_SERVICE_WINDOW_DAYS));
        }

        /** R5 + C2. */
        private void onCalving(PlannerEvent e) {
            complete(e, ReproductiveTaskType.PARTO_ESPERADO);
            cancel(e, ReproductiveTaskType.PREPARTO, ReproductiveTaskType.SECAR,
                    ReproductiveTaskType.DIAGNOSTICAR_PRENEZ, ReproductiveTaskType.SERVIR,
                    ReproductiveTaskType.SERVIR_EN_CELO);
            LocalDate check = e.date().plusDays(settings.postpartumCheckDays());
            add(ReproductiveTaskRule.R5_POSTPARTUM_CHECK, e, check, check);
            LocalDate serve = e.date().plusDays(settings.voluntaryWaitingDays());
            add(ReproductiveTaskRule.R5_SERVE, e, serve, serve);
            endGestationCycle(e);
            servicesSinceCalving = 0; // H4: el contador de repetidora se reinicia solo con el parto.
        }

        /** R6 + C2. */
        private void onAbortion(PlannerEvent e) {
            cancel(e, ReproductiveTaskType.PARTO_ESPERADO, ReproductiveTaskType.PREPARTO, ReproductiveTaskType.SECAR,
                    ReproductiveTaskType.DIAGNOSTICAR_PRENEZ, ReproductiveTaskType.SERVIR,
                    ReproductiveTaskType.SERVIR_EN_CELO);
            // P6: el aborto también lleva control (mismo plazo y mismo evento que el posparto).
            LocalDate check = e.date().plusDays(settings.postpartumCheckDays());
            add(ReproductiveTaskRule.R6_POSTABORTION_CHECK, e, check, check);
            LocalDate serve = e.date().plusDays(settings.voluntaryWaitingDays());
            add(ReproductiveTaskRule.R6_SERVE, e, serve, serve);
            endGestationCycle(e);
        }

        /** C4. */
        private void onExit(PlannerEvent e) {
            cancel(e, ReproductiveTaskType.values());
            closeFollowUp(e.eventId());
        }

        private void endGestationCycle(PlannerEvent e) {
            closeFollowUp(e.eventId());
            lastService = null;
            lastCalvingOrAbortion = e.date();
        }

        /**
         * P1 (opción A): fecha probable de parto a partir de los días de gestación que estimó el
         * veterinario al diagnosticar = diagnóstico − días estimados + días de gestación.
         */
        private LocalDate calvingFromGestationEstimate(PlannerEvent diagnosis) {
            Integer estimatedDays = diagnosis.payloadPositiveInt(GESTATION_ESTIMATE_FIELD);
            if (estimatedDays == null) {
                return null;
            }
            return diagnosis.date().minusDays(estimatedDays).plusDays(settings.gestationDays());
        }

        private LocalDate expectedCalving(PlannerEvent service) {
            String estimate = service.payloadString(CALVING_ESTIMATE_FIELD);
            if (estimate != null) {
                try {
                    return LocalDate.parse(estimate);
                } catch (DateTimeParseException ignored) {
                    // Fecha inválida en el payload: se usa la gestación configurada.
                }
            }
            return service.date().plusDays(settings.gestationDays());
        }

        private void add(ReproductiveTaskRule rule, PlannerEvent origin, LocalDate due, LocalDate windowEnd) {
            MutableTask task = new MutableTask(rule, origin.eventId(), due, windowEnd);
            tasks.putIfAbsent(task.key(), task);
        }

        private void complete(PlannerEvent by, ReproductiveTaskType... types) {
            close(by, ReproductiveTaskStatus.HECHA, types);
        }

        private void cancel(PlannerEvent by, ReproductiveTaskType... types) {
            close(by, ReproductiveTaskStatus.CANCELADA, types);
        }

        private void close(PlannerEvent by, ReproductiveTaskStatus status, ReproductiveTaskType... types) {
            Set<ReproductiveTaskType> targets = types.length == 0
                    ? EnumSet.noneOf(ReproductiveTaskType.class) : EnumSet.of(types[0], types);
            for (MutableTask task : tasks.values()) {
                if (task.open && targets.contains(task.rule.taskType())) {
                    task.open = false;
                    task.status = status;
                    task.closedByEventId = by.eventId();
                    task.closedAt = by.date();
                }
            }
        }

        private boolean hasOpen(ReproductiveTaskType type) {
            return tasks.values().stream().anyMatch(t -> t.open && t.rule.taskType() == type);
        }

        private void closeFollowUp(String byEventId) {
            if (followUpOpen) {
                followUpOpen = false;
                followUpClosedBy = byEventId;
                followUpClosedByPregnancy = false;
            }
        }

        /**
         * Revisión H2: pérdida embrionaria (PRENIADA y luego VACIA en el mismo ciclo). La vaca
         * vuelve a estar sin preñez confirmada: el semáforo se reabre y sigue contando desde el
         * último servicio (C1, CA7).
         */
        private void reopenFollowUpAfterPregnancyLoss() {
            if (!followUpOpen && followUpClosedByPregnancy && lastService != null) {
                followUpOpen = true;
                followUpClosedBy = null;
                followUpClosedByPregnancy = false;
            }
        }

        ReproductivePlan finish(List<PlannerEvent> sorted, String cycleStartEventId, LocalDate today) {
            Map<String, Integer> order = new LinkedHashMap<>();
            for (int i = 0; i < sorted.size(); i++) {
                order.put(sorted.get(i).eventId(), i);
            }
            int cycleStart = cycleStartEventId == null ? 0 : order.getOrDefault(cycleStartEventId, 0);

            List<PlannedTask> planned = new ArrayList<>();
            for (MutableTask task : tasks.values()) {
                ReproductiveTaskStatus status = task.status;
                LocalDate closedAt = task.closedAt;
                if (task.open) {
                    if (!context.active()) {
                        status = ReproductiveTaskStatus.CANCELADA; // G4
                        closedAt = today;
                    } else if (today.isAfter(task.windowEnd)) {
                        status = ReproductiveTaskStatus.VENCIDA;
                    } else {
                        status = ReproductiveTaskStatus.PENDIENTE;
                    }
                }
                boolean late = status == ReproductiveTaskStatus.HECHA && closedAt != null
                        && closedAt.isAfter(task.windowEnd);
                boolean currentCycle = order.getOrDefault(task.originEventId, -1) >= cycleStart;
                planned.add(new PlannedTask(task.rule, task.originEventId, task.dueDate, task.windowEnd, status,
                        task.closedByEventId, closedAt, late, currentCycle));
            }

            PlannedFollowUp followUp = null;
            if (followUpServiceDate != null) {
                boolean open = followUpOpen && context.active();
                followUp = new PlannedFollowUp(followUpServiceDate, followUpServiceEventId, followUpServiceNumber,
                        open, followUpClosedBy);
            }
            return new ReproductivePlan(List.copyOf(planned), followUp, diagnosisWithoutService);
        }
    }

    private static final class MutableTask {
        private final ReproductiveTaskRule rule;
        private final String originEventId;
        private final LocalDate dueDate;
        private final LocalDate windowEnd;
        private boolean open = true;
        private ReproductiveTaskStatus status = ReproductiveTaskStatus.PENDIENTE;
        private String closedByEventId;
        private LocalDate closedAt;

        MutableTask(ReproductiveTaskRule rule, String originEventId, LocalDate dueDate, LocalDate windowEnd) {
            this.rule = rule;
            this.originEventId = originEventId;
            this.dueDate = dueDate;
            this.windowEnd = windowEnd;
        }

        String key() {
            return rule.name() + "#" + originEventId;
        }
    }
}
