package com.cattle.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BovineEventResponseDTO {
    private String eventId;
    private String eventType;
    private String eventAt;
}
