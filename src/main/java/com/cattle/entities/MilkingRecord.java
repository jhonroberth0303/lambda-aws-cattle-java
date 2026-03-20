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

    private String PK;          // Partition Key: BOVINE#<id>
    private String SK;          // Sort Key: LACTANCIA#YYYY-MM-DD#AM|PM
    private Integer bovineId;   // Id numérico redundante para GSIs
    private String date;        // YYYY-MM-DD
    private String shift;       // AM | PM
    private Double liters;      // litros ordeñados
    private String status;      // completo | omitido | parcial
    private String observations;// observaciones libres
    private String recordedBy;  // usuario que registró
    private String createdAt;   // timestamp ISO
    private Integer lactationNumber; // Número de lactancia (1, 2, 3...)
    private String gsi2pk;      // BOVINE#<id>#LACT#<nn>
    private String gsi2sk;      // YYYY-MM-DD#AM|PM

    @DynamoDbPartitionKey
    public String getPK() {
        return PK;
    }

    @DynamoDbSortKey
    public String getSK() {
        return SK;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "GSI2-bovine-lactation-index")
    public String getGsi2pk() {
        return gsi2pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "GSI2-bovine-lactation-index")
    public String getGsi2sk() {
        return gsi2sk;
    }
}
