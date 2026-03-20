package com.cattle.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class Event {

    // ---- Claves base ----
    private String pk;   // farm#F001#pasture#P-01
    private String sk;   // eventAt#2025-09-24T10:00:00Z#GRAZING_END

    // ---- GSI: gsi1-type-date ----
    private String gsi1pk; // farm#F001#type#GRAZING_END
    private String gsi1sk; // 2025-09-24T10:00:00Z

    // ---- Atributos del evento ----
    private String eventType;   // GRAZING_END
    private String eventAt;     // 2025-09-24T10:00:00Z (ISO)
    private Integer animals;    // 5
    private Integer residualCm; // 7
    private String user;        // jhon

    // ---- Mapeo DynamoDB ----
    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1-type-date")
    public String getGsi1pk() { return gsi1pk; }

    @DynamoDbSecondarySortKey(indexNames = "gsi1-type-date")
    public String getGsi1sk() { return gsi1sk; }
}

