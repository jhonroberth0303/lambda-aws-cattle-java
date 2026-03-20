package com.cattle.entities.bovines;

import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.enums.profiles.Source;
import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class ProfileLifecycle extends BaseDdbItem {

    private LifeStage lifeStage;
    private Source lifeStageSource;
    private BovineCategory category;
    private Source categorySource;
    private LifecycleStatus status;
    private Boolean enabled;
    private String notes;

    // ==== BATCH SCHEDULING ====
    private String lastEvaluatedAt;
    private String nextRecalcDate;
}

