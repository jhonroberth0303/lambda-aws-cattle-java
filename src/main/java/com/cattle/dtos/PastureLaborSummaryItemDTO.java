package com.cattle.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PastureLaborSummaryItemDTO {
    private String eventType;
    private int count;
    private String lastAt;
}
