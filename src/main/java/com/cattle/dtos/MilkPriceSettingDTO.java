package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Configuración vigente del precio de leche por litro para un sitio")
public class MilkPriceSettingDTO {

    @Schema(description = "Identificador del sitio", example = "001")
    private String siteId;

    @Schema(description = "Precio vigente de leche por litro", example = "1800")
    private Double milkPricePerLiter;

    @Schema(description = "Fecha/hora UTC de última actualización", example = "2026-04-29T18:45:00Z")
    private String updatedAt;

    @Schema(description = "Usuario que actualizó el valor", example = "jhonroberth")
    private String updatedBy;
}