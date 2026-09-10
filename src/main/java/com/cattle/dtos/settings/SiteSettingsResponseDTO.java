package com.cattle.dtos.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respuesta de {@code GET /site/{siteId}/settings}: todas las settings del sitio,
 * mezclando valores almacenados con los valores por defecto del catálogo.
 * EP-20260909, Fase 3.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Todas las settings de configuración de negocio de un sitio")
public class SiteSettingsResponseDTO {

    @Schema(description = "Identificador del sitio", example = "001")
    private String siteId;

    @Schema(description = "Versión del catálogo site-settings.yml", example = "2026-09-09")
    private String catalogVersion;

    @Schema(description = "Settings del sitio, en el orden del catálogo")
    private List<SiteSettingDTO> settings;
}
