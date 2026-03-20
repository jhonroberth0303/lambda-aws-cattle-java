package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de una preñez de un bovino")
public class ProfilePregnancyDTO {
    @Schema(description = "Clave primaria (PK)", example = "BOVINE#167")
    private String PK;

    @Schema(description = "Clave de ordenamiento (SK)", example = "PREG#2025-07-06")
    private String SK;

    @Schema(description = "Método de confirmación de la preñez", example = "palpation")
    private String confirmationMethod;

    @Schema(description = "Fecha estimada de parto", example = "2026-04-10")
    private String expectedDueDate;

    @Schema(description = "Fecha real de parto", example = "2026-04-10")
    private String calvingDate;

    @Schema(description = "Clave primaria del índice GSI2", example = "PREG#ACTIVE")
    private String GSI2PK;

    @Schema(description = "Clave de ordenamiento del índice GSI2", example = "2026-03-10#BOVINE#169")
    private String GSI2SK;

    @Schema(description = "Notas de la preñez", example = "Se preña con toro común holstein")
    private String notes;

    @Schema(description = "Fecha de servicio", example = "2025-07-06")
    private String serviceDate;

    @Schema(description = "Estado de la preñez", example = "ACTIVE")
    private String status;

    @Override
    public String toString() {
        return "PK:" + PK
                + "-SK:" + SK
                + "-confirmationMethod:" + confirmationMethod
                + "-expectedDueDate:" + expectedDueDate
                + "-calvingDate:" + calvingDate
                + "-GSI2PK:" + GSI2PK
                + "-GSI2SK:" + GSI2SK
                + "-notes:" + notes
                + "-serviceDate:" + serviceDate
                + "-status:" + status;
    }
}
