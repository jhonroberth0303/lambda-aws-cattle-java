package com.cattle.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PastureStatisticsDTO {
    private String pastureId;
    private String pastureName;
    private String species;
    private String from;
    private String to;
    private Integer cyclesCompleted;
    private Double avgDaysInUse;
    private Double avgRestDays;
    private Double utilizationPercent;
    private Integer avgResidualCm;
    private Integer lastResidualCm;
    private Integer laborCount;
    private List<PastureLaborSummaryItemDTO> laborSummary;
}
