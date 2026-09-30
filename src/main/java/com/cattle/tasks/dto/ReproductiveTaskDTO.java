package com.cattle.tasks.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Tarea reproductiva para la agenda y el detalle del bovino.
 * {@code overdue} se calcula al leer (hoy > fin de ventana): la agenda no espera al batch
 * de las 03:00 para mostrar una tarea como vencida.
 */
@Getter
@Builder
public class ReproductiveTaskDTO {

    private String taskId;
    private String bovineId;
    private String taskType;
    private String ruleCode;
    private String dueDate;
    private String windowEnd;
    private String status;
    private boolean overdue;
    private String originEventId;
    private String closedByEventId;
    private String closedAt;
    private boolean lateCompletion;
}
