package com.cattle.tasks.dto;

import lombok.Builder;
import lombok.Getter;

/** Semáforo post-servicio de una hembra, con el color ya calculado para hoy (CA20, CA21). */
@Getter
@Builder
public class ServiceFollowUpDTO {

    private String bovineId;
    private String lastServiceDate;
    private int serviceNumber;
    private long daysSinceService;
    private String color;
    private boolean repeatBreeder;
    private boolean open;
}
