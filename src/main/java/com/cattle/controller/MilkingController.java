package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.CowWithLactationsDTO;
import com.cattle.dtos.MilkingDTO;
import com.cattle.enums.LogType;
import com.cattle.processor.MilkingProcessor;
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
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/site/{siteId}/milking")
@Tag(name = "Ordeño", description = "Registro y consulta de datos de producción de leche")
public class MilkingController {

    private final MilkingProcessor milkingProcessor;
    private final LambdaContext lambdaContext;

    public MilkingController(MilkingProcessor milkingProcessor, LambdaContext lambdaContext) {
        this.milkingProcessor = milkingProcessor;
        this.lambdaContext = lambdaContext;
    }

    @Operation(
            summary = "Registrar ordeño",
            description = "Registra los datos de un ordeño (producción de leche) para un bovino"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordeño registrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MilkingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<MilkingDTO> createMilking(
            @Parameter(description = "ID de la finca", required = true, example = "FARM#001")
            @PathVariable("siteId") String siteId,
            @Valid @RequestBody MilkingDTO milkingDTO) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Creating milking for site: " + siteId);
        return milkingProcessor.createMilking(milkingDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(
            summary = "Obtener vacas ordeñables",
            description = "Obtiene la lista operativa de vacas habilitadas para ordeño con su lactancia vigente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista operativa de vacas ordeñables",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CowWithLactationsDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron vacas con lactancias", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<CowWithLactationsDTO>> getCowsWithLactations(
            @Parameter(description = "ID de la finca", required = true, example = "FARM#001")
            @PathVariable("siteId") String siteId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Received request to find milking cows for site: " + siteId);
        return milkingProcessor.getCowsWithLactations(siteId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Obtener histórico de lactancias",
            description = "Obtiene la lista histórica de vacas con sus lactancias activas e históricas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista histórica de vacas con lactancias",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CowWithLactationsDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron vacas con lactancias históricas", content = @Content)
    })
    @GetMapping("/history")
    public ResponseEntity<List<CowWithLactationsDTO>> getCowsWithLactationsHistory(
            @Parameter(description = "ID de la finca", required = true, example = "FARM#001")
            @PathVariable("siteId") String siteId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Received request to find milking cows history for site: " + siteId);
        return milkingProcessor.getCowsWithLactationsHistory(siteId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Consultar historial de ordeños",
            description = "Obtiene el historial de ordeños de un bovino, opcionalmente filtrado por turno (AM/PM)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos de ordeño obtenidos exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MilkingDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros de ordeño", content = @Content)
    })
    @GetMapping("/{idBovine}")
    public ResponseEntity<List<MilkingDTO>> milkingData(
            @Parameter(description = "ID de la finca", required = true, example = "FARM#001")
            @PathVariable("siteId") String siteId,
            @Parameter(description = "ID del bovino", required = true, example = "123")
            @PathVariable("idBovine") Integer idBovine,
            @Parameter(description = "Turno de ordeño (AM o PM)", required = false, example = "AM")
            @RequestParam(value = "shift", required = false) String shift) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Fetching milking data for site: " + siteId + ", bovine: " + idBovine);
        return milkingProcessor.getMilkingData(idBovine, shift)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @Operation(
            summary = "Consultar ordeños por lactancia",
            description = "Obtiene los registros de ordeño de un bovino filtrados por número de lactancia"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registros de ordeño por lactancia",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MilkingDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros para esta lactancia", content = @Content)
    })
    @GetMapping("/{idBovine}/lactation/{lactationNumber}")
    public ResponseEntity<List<MilkingDTO>> getMilkingByLactation(
            @Parameter(description = "ID de la finca", required = true, example = "FARM#001")
            @PathVariable("siteId") String siteId,
            @Parameter(description = "ID del bovino", required = true, example = "172")
            @PathVariable("idBovine") Integer idBovine,
            @Parameter(description = "Número de lactancia", required = true, example = "1")
            @PathVariable("lactationNumber") String lactationNumber,
            @Parameter(description = "Turno de ordeño (AM o PM)", required = false, example = "AM")
            @RequestParam(value = "shift", required = false) String shift) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Fetching milking by lactation for site: " + siteId);
        return milkingProcessor.getMilkingByLactation(idBovine, lactationNumber, shift)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
