package com.cattle.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LastPreEntryCheckDTO {
    private String completedAt;
    private String performedBy;
    private Boolean allCriticalOk;
    private List<CheckItemDTO> items;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckItemDTO {
        private String id;
        private String label;
        private boolean critical;
        private String status;
        private String notes;
    }
}
