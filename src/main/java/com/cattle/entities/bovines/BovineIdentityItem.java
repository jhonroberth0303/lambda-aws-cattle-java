package com.cattle.entities.bovines;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class BovineIdentityItem extends BaseDdbItem{

    private Integer bovineId;
    private String farmId;
    private String name;
    private String gender; // male | female
    private String breed;
    private List<BreedComposition> breedComposition;
    private String bornDate; // YYYY-MM-DD
    private String color;
    private String origin;   // born | bought
    private String fatherId;
    private String fatherNameSnapshot;
    private String motherId;
    private String motherNameSnapshot;

}
