package com.cattle.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PastureEventResponseDTO {
    private String eventId;
    private String eventType;
    private RotationSemaphoreItemDTO pasture;
}