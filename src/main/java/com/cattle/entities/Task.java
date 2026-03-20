package com.cattle.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class Task {

    // ---- Claves base ----
    private String pk;        // farm#F001
    private String sk;        // dueDate#2025-10-02T23:00:00Z#T10

    // ---- GSI: gsi1-by-pasture-date ----
    private String gsi1pk;    // farm#F001#pasture#P-04
    private String gsi1sk;    // 2025-10-02T23:00:00Z

    // ---- Atributos ----
    private String taskId;    // T10
    private String dueDate;   // 2025-10-02T23:00:00Z (ISO-8601)
    private String kind;      // Registrar cierre de pastoreo y residual (P-04)
    private String pastureId; // P-04
    private String status;    // PENDIENTE

    // ---- Mapeo DynamoDB ----
    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1-by-pasture-date")
    public String getGsi1pk() { return gsi1pk; }

    @DynamoDbSecondarySortKey(indexNames = "gsi1-by-pasture-date")
    public String getGsi1sk() { return gsi1sk; }
}
