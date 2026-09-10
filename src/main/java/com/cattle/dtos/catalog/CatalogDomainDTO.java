package com.cattle.dtos.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respuesta de GET /catalogs/{domain}: un único catálogo de dominio.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Un catálogo de dominio")
public class CatalogDomainDTO {

    @Schema(description = "Versión declarada en catalog.yml", example = "2026-09-09")
    private String version;

    @Schema(description = "Hash corto del contenido completo de catálogos", example = "a1b2c3d4e5f6")
    private String hash;

    @Schema(description = "Nombre del dominio", example = "bovine-events")
    private String domain;

    @Schema(description = "Entradas del dominio, en orden de definición")
    private List<CatalogEntryDTO> entries;
}
