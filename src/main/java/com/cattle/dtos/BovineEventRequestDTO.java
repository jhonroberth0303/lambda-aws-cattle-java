package com.cattle.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BovineEventRequestDTO {
    private String eventType;
    private String eventAt;
    private String createdBy;
    private Map<String, Object> payload;
}
