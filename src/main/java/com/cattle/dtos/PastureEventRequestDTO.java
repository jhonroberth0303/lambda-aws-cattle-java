package com.cattle.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PastureEventRequestDTO {
    private String eventType;
    private String createdBy;
    private Payload payload = new Payload();

    @Getter
    @Setter
    public static class Payload {
        // Campos de eventos operativos
        private String lotId;
        private Integer animals;
        private Integer residualCm;
        private String substatus;
        private String holdUntil;
        private String notes;

        // Campos exclusivos de PRE_ENTRY_CHECK
        private List<CheckItem> items;
        private String completedAt;
        private Boolean allCriticalOk;
        private String performedBy;

        // Campos de labores de pradera (FERTILIZED, LIMED, HEIGHT_MEASURED)
        private Integer heightCm;
        private String productName;
        private Double quantityKg;
    }

    @Getter
    @Setter
    public static class CheckItem {
        private String id;
        private String label;
        private boolean critical;
        private String status;
        private String notes;
    }
}