package com.cattle.dtos.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload de {@code PUT /site/{siteId}/settings/{key}}. El tipo de {@code value}
 * debe corresponder al tipo declarado para la clave en {@code site-settings.yml}.
 * EP-20260909, Fase 3.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Payload para actualizar una setting de negocio de un sitio")
public class SiteSettingUpdateRequestDTO {

    @NotNull(message = "El valor 'value' es requerido")
    @Schema(description = "Nuevo valor (número, texto, booleano u objeto JSON según el tipo de la clave)",
            example = "280", requiredMode = Schema.RequiredMode.REQUIRED)
    private Object value;

    @Schema(description = "Usuario que realiza la actualización", example = "jhonroberth")
    private String updatedBy;

    @Schema(description = "Motivo del cambio (auditoría)", example = "Ajuste solicitado por el veterinario")
    private String changeReason;
}
