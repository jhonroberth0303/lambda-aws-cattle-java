package com.cattle.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RotationSemaphoreItemDTO {
    public String id;
    public String farmId;
    public String pastureId;
    public String name;
    public String species;
    public String status;
    public String substatus;
    public boolean blocked;
    public String blockReason;
    public Integer etaOpenDays;
    public String readyAt;
    public String holdUntil;
    public String lastUseAt;
    public Integer currentHeightCm;
    public Integer daysRest;
    public Integer entryHeightCm;
    public Integer exitResidualCm;
    public Integer restDaysMin;
    public Integer cutIntervalDays;
    public String warn;
    public String notes;
    private Double areaHa;
    public LastPreEntryCheckDTO lastPreEntryCheck;
    public String entityType;
}
