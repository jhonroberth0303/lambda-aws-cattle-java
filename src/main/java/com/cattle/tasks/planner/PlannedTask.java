package com.cattle.tasks.planner;

import com.cattle.tasks.ReproductiveTaskRule;
import com.cattle.tasks.ReproductiveTaskStatus;
import com.cattle.tasks.ReproductiveTaskType;

import java.time.LocalDate;

/**
 * Tarea esperada según el replay del historial.
 *
 * @param windowEnd      último día para cumplirla; pasado ese día queda VENCIDA
 * @param currentCycle   pertenece al último ciclo reproductivo: solo estas se crean (CA18);
 *                       las de ciclos anteriores solo actualizan el estado si ya existen
 * @param lateCompletion se cumplió después de {@code windowEnd}
 */
public record PlannedTask(ReproductiveTaskRule rule, String originEventId, LocalDate dueDate,
                          LocalDate windowEnd, ReproductiveTaskStatus status, String closedByEventId,
                          LocalDate closedAt, boolean lateCompletion, boolean currentCycle) {

    public ReproductiveTaskType taskType() {
        return rule.taskType();
    }

    /** Clave determinista de la tarea (G1). */
    public String key() {
        return rule.name() + "#" + originEventId;
    }
}
