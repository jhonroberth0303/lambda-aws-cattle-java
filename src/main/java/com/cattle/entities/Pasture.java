package com.cattle.entities;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class Pasture {

    private String pk;
    private String gsi1pk;
    private String gsi2pk;
    private Integer gsi1sk;
    private Integer gsi2sk;

    private String farmId;
    private String id;
    private String name;
    private String species;
    private String status;
    private String substatus;
    private Double areaHa;
    private Integer currentHeightCm;
    private String lastUseAt;
    private String notes;
    private String blockReason;
    private String establishmentDate;
    private String holdUntil;

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1-species-eta")
    public String getGsi1pk() { return gsi1pk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi2-farm-blocked-eta")
    public String getGsi2pk() { return gsi2pk; }

    @DynamoDbSecondarySortKey(indexNames = "gsi1-species-eta")
    public Integer getGsi1sk() { return gsi1sk; }

    @DynamoDbSecondarySortKey(indexNames = "gsi2-farm-blocked-eta")
    public Integer getGsi2sk() { return gsi2sk; }

}
