package com.cattle.dtos.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Respuesta de GET /catalogs: todos los catálogos de dominio del producto.
 * `version` + `hash` identifican el contenido y alimentan el ETag.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Catálogos de dominio del producto")
public class CatalogResponseDTO {

    @Schema(description = "Versión declarada en catalog.yml", example = "2026-09-09")
    private String version;

    @Schema(description = "Hash corto del contenido (cambia si cambia cualquier entrada)", example = "a1b2c3d4e5f6")
    private String hash;

    @Schema(description = "Catálogos por dominio, en orden de definición")
    private Map<String, List<CatalogEntryDTO>> domains;
}
