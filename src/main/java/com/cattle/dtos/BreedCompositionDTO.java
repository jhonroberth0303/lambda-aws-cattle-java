package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Elemento de composición racial")
public class BreedCompositionDTO {
    @NotBlank(message = "breed es requerido")
    @Size(max = 50, message = "breed no puede exceder 50 caracteres")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String breed;

    @NotNull(message = "pct es requerido")
    @Min(value = 0, message = "pct no puede ser menor que 0")
    @Max(value = 100, message = "pct no puede ser mayor que 100")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pct;
}
