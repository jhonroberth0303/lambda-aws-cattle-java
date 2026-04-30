package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
@Schema(description = "Payload para actualizar el precio de leche por litro de un sitio")
public class MilkPriceSettingUpdateRequestDTO {

    @NotNull(message = "El valor milkPricePerLiter es requerido")
    @PositiveOrZero(message = "El valor milkPricePerLiter no puede ser negativo")
    @Schema(description = "Nuevo precio de leche por litro", example = "1800", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double milkPricePerLiter;

    @Schema(description = "Usuario que realiza la actualización", example = "jhonroberth")
    private String updatedBy;
}