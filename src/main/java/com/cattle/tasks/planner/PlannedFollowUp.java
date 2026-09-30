package com.cattle.tasks.planner;

import java.time.LocalDate;

/**
 * Seguimiento post-servicio de una hembra (semáforo). El color no se guarda: se calcula al
 * leer con {@link ServiceFollowUpEvaluator}.
 *
 * @param serviceNumber servicios desde el último parto, incluido el actual (el aborto no reinicia, H4)
 * @param open          sin preñez confirmada; se cierra con PRENIADA, parto, aborto o salida
 */
public record PlannedFollowUp(LocalDate lastServiceDate, String lastServiceEventId, int serviceNumber,
                              boolean open, String closedByEventId) {
}
