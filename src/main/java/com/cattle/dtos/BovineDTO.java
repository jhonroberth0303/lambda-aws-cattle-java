package com.cattle.dtos;

import com.cattle.enums.profiles.BovineOrigin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import com.cattle.dtos.BreedCompositionDTO;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "Identidad del bovino (datos base y estables).")
public class BovineDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Integer bovineId;

    // La fija el controlador con la finca de la ruta (/farms/{farmId}/bovines); el valor del cuerpo se ignora.
    @Schema(description = "Finca del bovino; se toma de la ruta", example = "F001", accessMode = Schema.AccessMode.READ_ONLY)
    private String farmId;

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema( requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "El género es requerido")
    // Acepta mayúsculas o minúsculas (el formulario envía "female"/"male"); BovineService lo guarda en mayúsculas (RF6).
    @Pattern(regexp = "(?i)MALE|FEMALE", message = "El género debe ser 'MALE' o 'FEMALE'")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String gender;

    @NotBlank(message = "La fecha de nacimiento es requerida")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "bornDate debe tener formato YYYY-MM-DD")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String bornDate;

    private String breedType; // PURE | CROSSBRED
    private String breed; // opcional si tienes breedComposition

    @Valid
    private List<BreedCompositionDTO> breedComposition;

    @Size(max = 30, message = "El color no puede exceder 30 caracteres")
    @Schema(description = "Color del pelaje", example = "Colorado")
    private String color;

    // Opcional: el formulario aún no lo captura. Antes tenía @NotBlank, que sobre un enum hace fallar
    // toda validación del DTO (HV000030) — HU-20261120, DT3.
    @Schema(description = "Origen del bovino", example = "BOUGHT")
    private BovineOrigin origin;

    private String fatherId;
    private String fatherNameSnapshot;
    private String motherId;
    private String motherNameSnapshot;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;
    private Instant updatedAt;

    // BreedCompositionDTO ahora será una clase externa para facilitar el mapeo y evitar problemas con DynamoDB
}
