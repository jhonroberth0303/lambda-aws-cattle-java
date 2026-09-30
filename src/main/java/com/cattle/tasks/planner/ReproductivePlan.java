package com.cattle.tasks.planner;

import java.util.List;

/**
 * Resultado del planner para un bovino.
 *
 * @param followUp               {@code null} si nunca tuvo un servicio
 * @param diagnosisWithoutService hubo un diagnóstico PRENIADA sin servicio previo (CA17, P1)
 */
public record ReproductivePlan(List<PlannedTask> tasks, PlannedFollowUp followUp,
                               boolean diagnosisWithoutService) {

    public static ReproductivePlan empty() {
        return new ReproductivePlan(List.of(), null, false);
    }
}
