package com.cattle.entities.bovines;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class ProfileReproductive extends BaseDdbItem {

    private String currentLactationId; // null
    private String currentPregnancyId; // PREG#2025-07-06
    private String updatedAt; // 2025-07-06T08:00:00Z

}
