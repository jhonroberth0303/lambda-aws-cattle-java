package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.forms.EventFormSchemasDTO;
import com.cattle.enums.LogType;
import com.cattle.exceptions.NotFoundException;
import com.cattle.services.EventFormCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Esquemas de formulario de eventos schema-driven (EP-20260909, Fase 2).
 *
 * <p>{@code GET /catalogs/{domain}/schema} — metadata pública cacheable, misma
 * naturaleza que {@link CatalogController}: no depende de finca ni requiere JWT.
 * El front la usa como fuente única para renderizar el formulario y el backend
 * la usa para validar el payload.
 */
@RestController
@RequestMapping("/catalogs")
@Tag(name = "Catalogos", description = "Esquemas de formulario de eventos (metadata pública, cacheable)")
public class EventFormController {

    private static final Duration CACHE_TTL = Duration.ofHours(12);

    private final EventFormCatalog eventFormCatalog;
    private final LambdaContext lambdaContext;

    public EventFormController(EventFormCatalog eventFormCatalog, LambdaContext lambdaContext) {
        this.eventFormCatalog = eventFormCatalog;
        this.lambdaContext = lambdaContext;
    }

    @Operation(summary = "Esquemas de formulario de un dominio",
            description = "Devuelve el esquema de formulario de cada tipo de evento ya migrado a schema-driven")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Esquemas del dominio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventFormSchemasDTO.class))),
            @ApiResponse(responseCode = "304", description = "Sin cambios respecto al ETag enviado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dominio sin esquemas de formulario", content = @Content)
    })
    @GetMapping("/{domain}/schema")
    public ResponseEntity<EventFormSchemasDTO> getDomainSchema(
            @Parameter(description = "Nombre del dominio", required = true, example = "bovine-events")
            @PathVariable("domain") String domain,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Fetching event form schema for domain: " + domain);

        EventFormSchemasDTO body = eventFormCatalog.getDomainSchemas(domain);
        if (body == null) {
            throw new NotFoundException("No hay esquemas de formulario para el dominio: " + domain);
        }
        String etag = eventFormCatalog.getEtag();
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(cacheControl())
                    .build();
        }
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(cacheControl())
                .body(body);
    }

    private CacheControl cacheControl() {
        return CacheControl.maxAge(CACHE_TTL).cachePublic().mustRevalidate();
    }
}
