package com.cattle.tasks.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Seguimiento post-servicio (semáforo) de una hembra en {@code tasks-prod}. Uno por bovino.
 * <pre>
 * PK     = BOVINE#&lt;bovineId&gt;
 * SK     = FOLLOWUP#SERVICE
 * GSI1PK = FARMFOLLOWUP#&lt;farmId&gt;#OPEN   solo mientras está abierto
 * GSI1SK = &lt;lastServiceDate&gt;#&lt;bovineId&gt;
 * </pre>
 * El color no se persiste: depende de la fecha del día (ver {@code ServiceFollowUpEvaluator}).
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class ServiceFollowUpItem {

    public static final String SORT_KEY = "FOLLOWUP#SERVICE";

    private String pk;
    private String sk;
    private String gsi1pk;
    private String gsi1sk;

    private String bovineId;
    private String farmId;
    private String lastServiceDate;
    private String lastServiceEventId;
    private Integer serviceNumber;
    private Boolean open;
    private String closedByEventId;
    private String updatedAt;

    public static String openIndexKey(String farmId) {
        return "FARMFOLLOWUP#" + farmId + "#OPEN";
    }

    public void applyOpenIndex() {
        if (Boolean.TRUE.equals(open)) {
            this.gsi1pk = openIndexKey(farmId);
            this.gsi1sk = lastServiceDate + "#" + bovineId;
        } else {
            this.gsi1pk = null;
            this.gsi1sk = null;
        }
    }

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    @DynamoDbSecondaryPartitionKey(indexNames = ReproductiveTaskItem.GSI1)
    public String getGsi1pk() { return gsi1pk; }

    @DynamoDbSecondarySortKey(indexNames = ReproductiveTaskItem.GSI1)
    public String getGsi1sk() { return gsi1sk; }
}
