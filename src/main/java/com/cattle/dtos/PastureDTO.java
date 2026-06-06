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
    private String farmId;
    private String id;
    private String name;
    private String notes;
    private String species;
    private String status;
    private String substatus;
    private String lastUseAt;
    private Double areaHa;
    private Integer daysRest;
    private Integer currentHeightCm;
    private Integer etaOpenDays;
    private String gsi1pk;
    private String gsi2pk;
    private Integer gsi1sk;
    private Integer gsi2sk;

    // Lombok @ToString y @EqualsAndHashCode generarán los métodos automáticamente
}
