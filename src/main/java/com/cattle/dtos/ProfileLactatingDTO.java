package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de una lactancia de un bovino")
public class ProfileLactatingDTO {
    @Schema(description = "Identificador único del bovino", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer bovineId;

    @Schema(description = "Fecha de inicio de la lactancia", example = "2025-01-25")
    private String startDate;

    @Schema(description = "Fecha de secado", example = "2025-10-10")
    private String dryDate;

    @Schema(description = "Fecha de fin de la lactancia", example = "2026-04-10")
    private String endDate;

    @Schema(description = "Clave primaria del índice GSI1", example = "LACT#LACTATING")
    private String GSI1PK;

    @Schema(description = "Clave de ordenamiento del índice GSI1", example = "2025-01-25#BOVINE#167")
    private String GSI1SK;


    @Schema(description = "Notas de la lactancia", example = "Primera lactancia con perdida de cría, la cría perdida era una hembra, hay fiebre de leche y se trata con exito")
    private String notes;

    @Schema(description = "Estado de la lactancia", example = "ACTIVE")
    private String status;

    @Override
    public String toString() {
        return "-bovineId:" + bovineId
                + "-startDate:" + startDate
                + "-dryDate:" + dryDate
                + "-endDate:" + endDate
                + "-GSI1PK:" + GSI1PK
                + "-GSI1SK:" + GSI1SK
                + "-notes:" + notes
                + "-status:" + status;
    }
}
