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
 * Tarea de la agenda de la finca en la tabla {@code tasks-prod} (HU-20260929, D1).
 * <pre>
 * PK     = BOVINE#&lt;bovineId&gt;                    (esquema genérico &lt;ENTIDAD&gt;#&lt;id&gt;)
 * SK     = TASK#&lt;ruleCode&gt;#&lt;originEventId&gt;       (determinista → idempotencia)
 * GSI1PK = FARM#&lt;farmId&gt;#OPEN                   solo si PENDIENTE/VENCIDA (índice disperso)
 * GSI1SK = &lt;dueDate&gt;#BOVINE#&lt;bovineId&gt;#&lt;ruleCode&gt;
 * </pre>
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class ReproductiveTaskItem {

    public static final String ENTITY_BOVINE = "BOVINE";
    public static final String SK_PREFIX = "TASK#";
    public static final String GSI1 = "gsi1";

    private String pk;
    private String sk;
    private String gsi1pk;
    private String gsi1sk;

    private String taskId;
    private String entityType;
    private String taskType;
    private String ruleCode;
    private String bovineId;
    private String farmId;
    private String dueDate;
    private String windowEnd;
    private String status;
    private String originEventId;
    private String closedByEventId;
    private String closedAt;
    private Boolean lateCompletion;
    private String createdAt;
    private String updatedAt;

    public static String partitionKey(String bovineId) {
        return ENTITY_BOVINE + "#" + bovineId;
    }

    public static String sortKey(String taskKey) {
        return SK_PREFIX + taskKey;
    }

    public static String openIndexKey(String farmId) {
        return "FARM#" + farmId + "#OPEN";
    }

    /** Coloca la tarea en el índice de abiertas o la retira según su estado. */
    public void applyOpenIndex(boolean open) {
        if (open) {
            this.gsi1pk = openIndexKey(farmId);
            this.gsi1sk = dueDate + "#" + ENTITY_BOVINE + "#" + bovineId + "#" + ruleCode;
        } else {
            this.gsi1pk = null;
            this.gsi1sk = null;
        }
    }

    @DynamoDbPartitionKey
    public String getPk() { return pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }

    @DynamoDbSecondaryPartitionKey(indexNames = GSI1)
    public String getGsi1pk() { return gsi1pk; }

    @DynamoDbSecondarySortKey(indexNames = GSI1)
    public String getGsi1sk() { return gsi1sk; }
}
