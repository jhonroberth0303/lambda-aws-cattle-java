package com.cattle.dtos.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entrada de un catálogo de dominio: código canónico + metadatos de presentación.
 * Los campos nulos se omiten en la respuesta JSON.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Entrada de un catálogo de dominio")
public class CatalogEntryDTO {

    @Schema(description = "Código canónico (= constante del enum backend)", example = "PARTO")
    private String code;

    @Schema(description = "Etiqueta en español para la UI", example = "Parto")
    private String label;

    @Schema(description = "Posición dentro del dominio (0-based)", example = "10")
    private Integer order;

    @Schema(description = "Grupo lógico del código, si aplica", example = "REPRODUCCION")
    private String group;

    @Schema(description = "Clase CSS del punto en la línea de tiempo, si aplica", example = "timeline-dot--open")
    private String dotClass;

    @Schema(description = "Orden de disponibilidad para estados de potrero, si aplica", example = "0")
    private Integer sortOrder;

    @Schema(description = "Icono (emoji) para la UI, si aplica", example = "🥛")
    private String icon;

    @Schema(description = "Tono visual para la UI, si aplica", example = "warn")
    private String tone;
}
