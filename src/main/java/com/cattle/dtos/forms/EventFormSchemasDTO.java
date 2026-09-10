package com.cattle.dtos.forms;

import com.cattle.forms.EventFormSchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Respuesta de {@code GET /catalogs/{domain}/schema}: los esquemas de formulario
 * de todos los tipos de evento migrados de un dominio (EP-20260909, Fase 2).
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Esquemas de formulario de eventos de un dominio")
public class EventFormSchemasDTO {

    @Schema(description = "Versión declarada en event-forms.yml", example = "2026-09-09")
    private String version;

    @Schema(description = "Hash corto del contenido", example = "a1b2c3d4e5f6")
    private String hash;

    @Schema(description = "Nombre del dominio", example = "bovine-events")
    private String domain;

    @Schema(description = "Esquema por código de evento (solo los tipos ya migrados)")
    private Map<String, EventFormSchema> schemas;
}
