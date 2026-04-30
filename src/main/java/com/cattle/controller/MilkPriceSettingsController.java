package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.MilkPriceSettingDTO;
import com.cattle.dtos.MilkPriceSettingUpdateRequestDTO;
import com.cattle.enums.LogType;
import com.cattle.processor.MilkPriceSettingsProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/site/{siteId}/settings/milk-price")
@Tag(name = "Configuracion de leche", description = "Consulta y actualización del precio de leche por litro por sitio")
public class MilkPriceSettingsController {

    private final MilkPriceSettingsProcessor milkPriceSettingsProcessor;
    private final LambdaContext lambdaContext;

    public MilkPriceSettingsController(MilkPriceSettingsProcessor milkPriceSettingsProcessor, LambdaContext lambdaContext) {
        this.milkPriceSettingsProcessor = milkPriceSettingsProcessor;
        this.lambdaContext = lambdaContext;
    }

    @Operation(summary = "Consultar precio vigente de leche", description = "Obtiene el precio vigente de leche por litro para un sitio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Precio vigente encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MilkPriceSettingDTO.class)))
    })
    @GetMapping
    public ResponseEntity<MilkPriceSettingDTO> getMilkPrice(
            @Parameter(description = "ID del sitio", required = true, example = "001")
            @PathVariable("siteId") String siteId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Fetching milk price setting for site: " + siteId);
        return ResponseEntity.ok(milkPriceSettingsProcessor.getMilkPrice(siteId));
    }

    @Operation(summary = "Actualizar precio vigente de leche", description = "Actualiza el precio de leche por litro para un sitio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Precio actualizado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MilkPriceSettingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido", content = @Content)
    })
    @PutMapping
    public ResponseEntity<MilkPriceSettingDTO> updateMilkPrice(
            @Parameter(description = "ID del sitio", required = true, example = "001")
            @PathVariable("siteId") String siteId,
            @Valid @RequestBody MilkPriceSettingUpdateRequestDTO request) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Updating milk price setting for site: " + siteId);
        return ResponseEntity.ok(milkPriceSettingsProcessor.updateMilkPrice(siteId, request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}