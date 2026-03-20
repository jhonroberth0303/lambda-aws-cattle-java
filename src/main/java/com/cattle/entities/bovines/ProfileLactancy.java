package com.cattle.entities.bovines;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class ProfileLactancy extends BaseDdbItem {

    private String createdAt; // 2025-01-25
    private String dryDate;   // 2025-10-10
    private String endDate;   // 2026-04-10
    private String lactationNumber; // "1"
    private String notes;     // Descripción
    private String startDate; // 2025-01-25
    private String status;    // CLOSE
}
