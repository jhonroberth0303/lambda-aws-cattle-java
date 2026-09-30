package com.cattle.tasks;

/**
 * Regla que origina una tarea. El par (regla, evento origen) identifica la tarea de forma
 * determinista: es su clave de idempotencia (G1).
 */
public enum ReproductiveTaskRule {
    /** R1: servicio → diagnóstico de preñez. */
    R1_DIAGNOSIS(ReproductiveTaskType.DIAGNOSTICAR_PRENEZ),
    /** R2: diagnóstico positivo → parto esperado. */
    R2_CALVING(ReproductiveTaskType.PARTO_ESPERADO),
    /** R2: diagnóstico positivo → preparto. */
    R2_PREPARTUM(ReproductiveTaskType.PREPARTO),
    /** R2: diagnóstico positivo → secado (solo si lacta). */
    R2_DRY_OFF(ReproductiveTaskType.SECAR),
    /** R4: diagnóstico dudoso → rediagnóstico. */
    R4_RECHECK(ReproductiveTaskType.DIAGNOSTICAR_PRENEZ),
    /** R5: parto → control posparto. */
    R5_POSTPARTUM_CHECK(ReproductiveTaskType.CONTROL_POSPARTO),
    /** R5: parto → servir tras el periodo voluntario de espera. */
    R5_SERVE(ReproductiveTaskType.SERVIR),
    /** R6: aborto → servir tras el periodo voluntario de espera. */
    R6_SERVE(ReproductiveTaskType.SERVIR),
    /** R6: aborto → control post-aborto (P6, decisión del PO 2026-09-30); se cumple con CONTROL_POSPARTO. */
    R6_POSTABORTION_CHECK(ReproductiveTaskType.CONTROL_POSPARTO),
    /** R7: celo fuera del periodo de espera → servir en ese celo. */
    R7_SERVE_IN_HEAT(ReproductiveTaskType.SERVIR_EN_CELO);

    private final ReproductiveTaskType taskType;

    ReproductiveTaskRule(ReproductiveTaskType taskType) {
        this.taskType = taskType;
    }

    public ReproductiveTaskType taskType() {
        return taskType;
    }
}
