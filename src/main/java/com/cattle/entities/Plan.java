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
    private String sk;
    private String entityType;
    private String farmId;
    private double growthRateCmPerDay;
    private String gsi1pk;
    private String gsi1sk;
    private String notes;
    private String planType;
    private String species;
    private String updatedAt;
    private Integer version;
    private Rules rules;
    private List<String> fertWindows;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() { return pk; }

    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() { return sk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1pk") public String getGsi1pk() { return gsi1pk; }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1sk") public String getGsi1sk() { return gsi1sk; }

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
