package com.cattle.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class Plan {

    private String pk;
    private String farmId;
    private String species;
    private String planType;
    private Rules rules;
    private List<String> fertWindows;
    private String notes;
    private Integer version;
    private String updatedAt;
    private double growthRateCmPerDay;

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "farmId")
    public String getFarmId() { return farmId; }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @DynamoDbBean
    public static class Rules {
        private Integer restDaysMin;
        private Integer entryHeightCm;
        private Integer exitResidualCm;
        private Integer cutIntervalDays;
        private Integer harvestDaysAfterSowing;
        private String harvestCue;
        private Integer rowSpacingCm;
        private Integer targetDryMatterPercent;
    }
}
