package com.cattle.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * DTO para la respuesta del endpoint /bovines/summary.
 * Contiene los datos consolidados de un bovino para tarjetas de presentación.
 * Incluye estados calculados y alertas para simplificar la lógica del frontend.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resumen consolidado de un bovino para tarjetas de presentación")
public class BovineSummaryDTO {

    // ==== IDENTITY ====
    @Schema(description = "ID único del bovino", example = "167")
    private Integer bovineId;

    @Schema(description = "Nombre del bovino", example = "Luna")
    private String name;

    @Schema(description = "Género del bovino", example = "FEMALE")
    private String gender;

    @Schema(description = "Raza del bovino", example = "Holstein")
    private String breed;

    @Schema(description = "Fecha de nacimiento (YYYY-MM-DD)", example = "2022-03-15")
    private String bornDate;

    @Schema(description = "ID de la finca", example = "FARM#001")
    private String farmId;

    // ==== LIFECYCLE ====
    @Schema(description = "Categoría del bovino", example = "COW", 
            allowableValues = {"CALF", "HEIFER", "COW", "YOUNG_BULL", "BULL", "OX"})
    private String category;

    @Schema(description = "Estado del bovino en el hato", example = "OPEN",
            allowableValues = {"OPEN", "SOLD", "DEAD", "TRANSFERRED"})
    private String status;

    @Schema(description = "Etapa de vida", example = "ADULT",
            allowableValues = {"NEWBORN", "CALF", "WEANED", "GROWER", "ADULT"})
    private String lifeStage;

    @Schema(description = "Indica si el bovino está habilitado (activo en el hato)", example = "true")
    private Boolean enabled;

    // ==== PREGNANCY ====
    @Schema(description = "Indica si el bovino tiene preñez activa", example = "true")
    private Boolean isPregnant;

    @Schema(description = "Estado de la preñez", example = "ACTIVE",
            allowableValues = {"ACTIVE", "CLOSED"})
    private String pregnancyStatus;

    @Schema(description = "Fecha esperada de parto (YYYY-MM-DD)", example = "2026-05-15")
    private String expectedDueDate;

    @Schema(description = "Fecha del último parto (YYYY-MM-DD)", example = "2025-11-20")
    private String calvingDate;

    // ==== LACTATION ====
    @Schema(description = "Indica si el bovino está lactando activamente", example = "true")
    private Boolean isLactating;

    @Schema(description = "Estado de la lactancia", example = "LACTATING",
            allowableValues = {"LACTATING", "DRY", "CLOSED"})
    private String lactationStatus;

    @Schema(description = "Número de lactancia", example = "3")
    private String lactationNumber;

    @Schema(description = "Fecha de inicio de lactancia (YYYY-MM-DD)", example = "2025-01-25")
    private String lactationStartDate;

    // ==== CALCULATED STATES ====
    @Schema(description = "Estado reproductivo calculado", example = "PREGNANT",
            allowableValues = {"OPEN", "PREGNANT", "PRE_PARTO", "POST_PARTO"})
    private String reproductiveState;

    @Schema(description = "Estado productivo combinado (preñez + lactancia)", example = "PREGNANT_LACTATING",
            allowableValues = {"OPEN", "OPEN_LACTATING", "PREGNANT", "PREGNANT_LACTATING", "PREGNANT_DRY", "PRE_PARTO", "POST_PARTO", "DRY"})
    private String productiveState;

    @Schema(description = "Lista de alertas activas", example = "[\"PREPARTUM\", \"DRY_OFF_SOON\"]")
    private List<String> alerts;

    @Schema(description = "Días hasta la fecha esperada de parto (null si no aplica)", example = "45")
    private Integer daysUntilDue;

    @Schema(description = "Días desde el inicio de lactancia (null si no aplica)", example = "120")
    private Integer daysInLactation;

    @Schema(description = "Días desde el último parto (null si no aplica)", example = "30")
    private Integer daysSinceCalving;

    // ==== METADATA ====
    @Schema(description = "Fecha de última actualización (ISO-8601)", example = "2026-02-02T08:00:00Z")
    private String updatedAt;
}
