package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos de registro de ordeño")
public class MilkingDTO {

    @NotNull(message = "El ID del bovino es requerido")
    @Positive(message = "El ID del bovino debe ser positivo")
    @Schema(description = "ID del bovino ordeñado", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer bovineId;

    @NotBlank(message = "La fecha es requerida")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha debe tener formato YYYY-MM-DD")
    @Schema(description = "Fecha del ordeño", example = "2026-01-21", requiredMode = Schema.RequiredMode.REQUIRED)
    private String date;

    @NotBlank(message = "El turno es requerido")
    @Pattern(regexp = "AM|PM", message = "El turno debe ser 'AM' o 'PM'")
    @Schema(description = "Turno del ordeño", example = "AM", allowableValues = {"AM", "PM"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String shift;

    @NotNull(message = "Los litros son requeridos")
    @PositiveOrZero(message = "Los litros no pueden ser negativos")
    @DecimalMax(value = "100.0", message = "Los litros no pueden exceder 100")
    @Schema(description = "Litros de leche producidos", example = "8.5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double liters;

    @Pattern(regexp = "completo|omitido|parcial", message = "Estado inválido")
    @Schema(description = "Estado del ordeño", example = "completo", allowableValues = {"completo", "omitido", "parcial"})
    private String status;

    @Size(max = 300, message = "Las observaciones no pueden exceder 300 caracteres")
    @Schema(description = "Observaciones del ordeño", example = "Producción normal")
    private String observations;

    @Schema(description = "Usuario que registró el ordeño", example = "juan.perez")
    private String recordedBy;

    @Schema(description = "Número de lactancia", example = "1")
    private Integer lactationNumber;

    @Override
    public String toString() {
        return "bovineId:" + bovineId
                + "-date:" + date
                + "-shift:" + shift
                + "-liters:" + liters
                + "-status:" + status
                + "-observations:" + observations
                + "-recordedBy:" + recordedBy;
    }

}
