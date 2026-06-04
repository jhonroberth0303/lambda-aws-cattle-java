package com.cattle.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MilkingRecord {

    private String pk;          // Partition Key: BOVINE#<id>
    private String sk;          // Sort Key: LACTANCIA#YYYY-MM-DD#AM|PM
    private Integer bovineId;   // Id numérico redundante para GSIs
    private String date;        // YYYY-MM-DD
    private String shift;       // AM | PM
    private Double liters;      // litros ordeñados
    private String status;      // completo | omitido | parcial
    private String observations;// observaciones libres
    private String recordedBy;  // usuario que registró
    private String createdAt;   // timestamp ISO
    private Integer lactationNumber; // Número de lactancia (1, 2, 3...)
    private String gsi1pk;      // BOVINE#<id>#LACT#<nn>
    private String gsi1sk;      // YYYY-MM-DD#AM|PM

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    public String getGsi1pk() {
        return gsi1pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    public String getGsi1sk() {
        return gsi1sk;
    }
}
