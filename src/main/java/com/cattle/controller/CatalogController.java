package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.catalog.CatalogDomainDTO;
import com.cattle.dtos.catalog.CatalogResponseDTO;
import com.cattle.enums.LogType;
import com.cattle.exceptions.NotFoundException;
import com.cattle.services.CatalogService;
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
 * Catálogos de dominio del producto (tipos de evento, estados, subestados, etc.).
 *
 * <p>Metadata pública: no depende de finca ni requiere autenticación. El front la
 * cachea (ETag + {@code Cache-Control}) y la usa como fuente única, con un bundle
 * local de respaldo. Épica EP-20260909, Fase 1.
 */
@RestController
@RequestMapping("/catalogs")
@Tag(name = "Catalogos", description = "Catálogos de dominio del producto (metadata pública, cacheable)")
public class CatalogController {

    private static final Duration CACHE_TTL = Duration.ofHours(12);

    private final CatalogService catalogService;
    private final LambdaContext lambdaContext;

    public CatalogController(CatalogService catalogService, LambdaContext lambdaContext) {
        this.catalogService = catalogService;
        this.lambdaContext = lambdaContext;
    }

    @Operation(summary = "Todos los catálogos", description = "Devuelve todos los catálogos de dominio con version y ETag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catálogos vigentes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogResponseDTO.class))),
            @ApiResponse(responseCode = "304", description = "Sin cambios respecto al ETag enviado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<CatalogResponseDTO> getAllCatalogs(
            @Parameter(description = "ETag previamente recibido", example = "\"2026-09-09-a1b2c3d4e5f6\"")
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Fetching all catalogs");
        String etag = catalogService.getEtag();
        if (etag.equals(ifNoneMatch)) {
            return notModified(etag);
        }
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(cacheControl())
                .body(catalogService.getCatalog());
    }

    @Operation(summary = "Un catálogo", description = "Devuelve un único catálogo de dominio por nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catálogo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogDomainDTO.class))),
            @ApiResponse(responseCode = "304", description = "Sin cambios respecto al ETag enviado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dominio desconocido", content = @Content)
    })
    @GetMapping("/{domain}")
    public ResponseEntity<CatalogDomainDTO> getCatalogDomain(
            @Parameter(description = "Nombre del dominio", required = true, example = "bovine-events")
            @PathVariable("domain") String domain,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Fetching catalog domain: " + domain);
        CatalogDomainDTO body = catalogService.getDomain(domain);
        if (body == null) {
            throw new NotFoundException("Catálogo de dominio no encontrado: " + domain);
        }
        String etag = catalogService.getEtag();
        if (etag.equals(ifNoneMatch)) {
            return notModified(etag);
        }
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(cacheControl())
                .body(body);
    }

    private <T> ResponseEntity<T> notModified(String etag) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(etag)
                .cacheControl(cacheControl())
                .build();
    }

    private CacheControl cacheControl() {
        return CacheControl.maxAge(CACHE_TTL).cachePublic().mustRevalidate();
    }
}
