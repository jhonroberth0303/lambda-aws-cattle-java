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
public class ProfilePregnancy extends BaseDdbItem {

    private String calvingDate;         // 2025-11-27
    private String confirmationMethod;  // palpation
    private String createdAt;           // 2025-02-22
    private String expectedDueDate;     // 2025-11-27
    private String notes;               // Descripción
    private String serviceDate;         // 2025-02-22
    private String status;              // CLOSE
}
