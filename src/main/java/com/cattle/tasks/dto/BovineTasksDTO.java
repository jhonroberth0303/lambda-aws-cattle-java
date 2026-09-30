package com.cattle.tasks.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Tareas (todas, abiertas primero) y semáforo de un bovino para su detalle. */
@Getter
@Builder
public class BovineTasksDTO {

    private String bovineId;
    private List<ReproductiveTaskDTO> tasks;
    /** {@code null} si la hembra nunca tuvo un servicio. */
    private ServiceFollowUpDTO followUp;
}
