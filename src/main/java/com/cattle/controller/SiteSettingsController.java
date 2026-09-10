package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.settings.SiteSettingDTO;
import com.cattle.dtos.settings.SiteSettingUpdateRequestDTO;
import com.cattle.dtos.settings.SiteSettingsResponseDTO;
import com.cattle.enums.LogType;
import com.cattle.processor.SiteSettingsProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuración de negocio por sitio (categoría C de la épica EP-20260909).
 * Endpoints genéricos por clave, respaldados por {@code SiteSettingItem}
 * (versionado, con historial). El precio de leche mantiene además su endpoint
 * dedicado {@code /site/{siteId}/settings/milk-price}.
 */
@RestController
@RequestMapping("/site/{siteId}/settings")
@Tag(name = "Configuracion de negocio", description = "Settings de negocio por sitio (genérico por clave)")
public class SiteSettingsController {

    private final SiteSettingsProcessor siteSettingsProcessor;
    private final LambdaContext lambdaContext;

    public SiteSettingsController(SiteSettingsProcessor siteSettingsProcessor, LambdaContext lambdaContext) {
        this.siteSettingsProcessor = siteSettingsProcessor;
        this.lambdaContext = lambdaContext;
    }

    @Operation(summary = "Listar settings del sitio",
            description = "Devuelve todas las settings de negocio vigentes del sitio, mezclando valores almacenados con los valores por defecto del catálogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settings del sitio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SiteSettingsResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<SiteSettingsResponseDTO> getAll(
            @Parameter(description = "ID del sitio", required = true, example = "001")
            @PathVariable("siteId") String siteId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Listing site settings for site: " + siteId);
        return ResponseEntity.ok(siteSettingsProcessor.getAll(siteId));
    }

    @Operation(summary = "Consultar una setting", description = "Devuelve una setting por clave (valor vigente o por defecto)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Setting encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SiteSettingDTO.class))),
            @ApiResponse(responseCode = "404", description = "Clave desconocida", content = @Content)
    })
    @GetMapping("/{key}")
    public ResponseEntity<SiteSettingDTO> getByKey(
            @Parameter(description = "ID del sitio", required = true, example = "001")
            @PathVariable("siteId") String siteId,
            @Parameter(description = "Clave de la setting", required = true, example = "GESTATION_DAYS")
            @PathVariable("key") String key) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Fetching site setting " + key + " for site: " + siteId);
        return ResponseEntity.ok(siteSettingsProcessor.getByKey(siteId, key));
    }

    @Operation(summary = "Actualizar una setting", description = "Actualiza el valor vigente de una setting por clave")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Setting actualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SiteSettingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Clave desconocida", content = @Content)
    })
    @PutMapping("/{key}")
    public ResponseEntity<SiteSettingDTO> updateByKey(
            @Parameter(description = "ID del sitio", required = true, example = "001")
            @PathVariable("siteId") String siteId,
            @Parameter(description = "Clave de la setting", required = true, example = "GESTATION_DAYS")
            @PathVariable("key") String key,
            @Valid @RequestBody SiteSettingUpdateRequestDTO request) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Updating site setting " + key + " for site: " + siteId);
        return ResponseEntity.ok(siteSettingsProcessor.updateByKey(siteId, key, request));
    }
}
