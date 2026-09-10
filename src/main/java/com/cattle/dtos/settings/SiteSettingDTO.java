package com.cattle.dtos.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una clave de configuración de negocio para un sitio: su valor vigente (o el
 * valor por defecto del catálogo si el sitio no lo ha personalizado) más los
 * metadatos de la definición. EP-20260909, Fase 3.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Configuración de negocio por sitio")
public class SiteSettingDTO {

    @Schema(description = "Identificador del sitio", example = "001")
    private String siteId;

    @Schema(description = "Clave de la setting", example = "GESTATION_DAYS")
    private String key;

    @Schema(description = "Tipo del valor", example = "NUMBER")
    private String valueType;

    @Schema(description = "Valor vigente (o el valor por defecto si source=DEFAULT)")
    private Object value;

    @Schema(description = "Valor por defecto del catálogo", example = "279")
    private Object defaultValue;

    @Schema(description = "STORED si el sitio lo personalizó, DEFAULT si aún no", example = "DEFAULT")
    private String source;

    @Schema(description = "Etiqueta legible", example = "Días de gestación")
    private String label;

    @Schema(description = "Grupo para agrupar en la UI", example = "reproduccion")
    private String group;

    @Schema(description = "Valor mínimo permitido (solo NUMBER)", example = "150")
    private Double min;

    @Schema(description = "Valor máximo permitido (solo NUMBER)", example = "400")
    private Double max;

    @Schema(description = "Versión del valor almacenado", example = "3")
    private Integer version;

    @Schema(description = "Fecha/hora UTC de última actualización", example = "2026-09-09T18:45:00Z")
    private String updatedAt;

    @Schema(description = "Usuario que actualizó el valor", example = "jhonroberth")
    private String updatedBy;
}
