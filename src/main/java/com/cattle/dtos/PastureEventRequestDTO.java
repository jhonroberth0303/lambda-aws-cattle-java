package com.cattle.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PastureEventRequestDTO {
    private String eventType;
    private String createdBy;
    private Payload payload = new Payload();

    @Getter
    @Setter
    public static class Payload {
        private String lotId;
        private Integer animals;
        private Integer residualCm;
        private String substatus;
        private String holdUntil;
        private String notes;
    }
}