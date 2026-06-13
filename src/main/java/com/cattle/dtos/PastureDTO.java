package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Schema(description = "Datos de registro de Pasturas")
public class PastureDTO {
    private String pk;
    private String sk;
    private String gsi1pk;
    private String gsi2pk;
    private Integer gsi1sk;
    private Integer gsi2sk;

    private String farmId;
    private String id;
    private String name;
    private String notes;
    private String species;
    private String status;
    private String substatus;
    private Double areaHa;
    private Integer currentHeightCm;
    private String lastUseAt;
    private String blockReason;
    private String establishmentDate;
    private String holdUntil;
    private String lastPreEntryCheckJson;
    private String entityType;

    private Integer daysRest;
    private Integer etaOpenDays;
}
