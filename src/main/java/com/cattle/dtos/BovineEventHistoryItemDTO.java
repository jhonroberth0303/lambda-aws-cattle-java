package com.cattle.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BovineEventHistoryItemDTO {
    private String eventId;
    private String eventType;
    private String eventAt;
    private String createdBy;
    private String notes;
    private String payloadJson;
}
