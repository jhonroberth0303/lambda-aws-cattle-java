package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resumen de una lactancia")
public class LactationSummaryDTO {

    @Schema(description = "Número de lactancia", example = "LACT#001")
    private String lactationNumber;

    @Schema(description = "Fecha de inicio de la lactancia", example = "2025-11-27")
    private String startDate;

    @Schema(description = "Fecha de fin de la lactancia (null si está activa)", example = "2026-03-15")
    private String endDate;

    @Schema(description = "Estado de la lactancia", example = "OPEN", allowableValues = {"OPEN", "CLOSED"})
    private String status;
}
