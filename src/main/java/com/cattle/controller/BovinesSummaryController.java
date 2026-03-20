package com.cattle.controller;

import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.enums.LogType;
import com.cattle.processor.BovinesSummaryProcessor;
import com.cattle.config.LambdaContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/summary")
@Tag(name = "Resumen Bovinos", description = "Operaciones para gestión de resúmenes de bovinos")
public class BovinesSummaryController {
    private final BovinesSummaryProcessor bovinesSummaryProcessor;
    private final LambdaContext lambdaContext;

    public BovinesSummaryController(BovinesSummaryProcessor bovinesSummaryProcessor, LambdaContext lambdaContext) {
        this.bovinesSummaryProcessor = bovinesSummaryProcessor;
        this.lambdaContext = lambdaContext;
    }

    @Operation(
            summary = "Listar resúmenes de bovinos",
            description = "Obtiene la lista de tarjetas resumen de todos los bovinos para presentación rápida"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de resúmenes obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineSummaryDTO.class))),
            @ApiResponse(responseCode = "204", description = "No hay resúmenes registrados", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<BovineSummaryDTO>> getAllSummaries() {
        lambdaContext.logInfo(LogType.CONTROLLER, "Received request to get all bovine summaries");
        List<BovineSummaryDTO> summaries = bovinesSummaryProcessor.findAll();
        if (summaries == null || summaries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(summaries);
    }

    @Operation(
            summary = "Obtener resumen de un bovino",
            description = "Obtiene la tarjeta resumen de un bovino específico por su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineSummaryDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Resumen no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BovineSummaryDTO> getSummaryById(
            @Parameter(description = "ID del bovino", required = true, example = "167")
            @PathVariable Integer id) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Received request to get summary for bovine ID: " + id);
        if (Objects.isNull(id) || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return bovinesSummaryProcessor.findById(id)
                .map(dto -> ResponseEntity.ok(dto))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Regenerar resumen de un bovino",
            description = "Recalcula y actualiza la tarjeta resumen de un bovino específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen regenerado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineSummaryDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PutMapping("/{id}/refresh")
    public ResponseEntity<BovineSummaryDTO> refreshSummaryById(
            @Parameter(description = "ID del bovino", required = true, example = "167")
            @PathVariable Integer id) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Received request to refresh summary for bovine ID: " + id);
        if (Objects.isNull(id) || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            BovineSummaryDTO refreshed = bovinesSummaryProcessor.refreshSummary(id);
            return ResponseEntity.ok(refreshed);
        } catch (Exception e) {
            lambdaContext.logException(LogType.CONTROLLER, "Error refreshing summary: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(
            summary = "Regenerar todos los resúmenes (batch)",
            description = "Recalcula y actualiza las tarjetas resumen de todos los bovinos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resúmenes regenerados exitosamente",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshAllSummaries() {
        lambdaContext.logInfo(LogType.CONTROLLER, "Received request to refresh all bovine summaries");
        try {
            int count = bovinesSummaryProcessor.refreshAllSummaries();
            return ResponseEntity.ok(Map.of(
                    "message", "Summaries refreshed successfully",
                    "count", count
            ));
        } catch (Exception e) {
            lambdaContext.logException(LogType.CONTROLLER, "Error refreshing all summaries: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to refresh summaries",
                    "message", e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Regenerar todas las categorías de resúmenes",
            description = "Recalcula y actualiza las categorías de todos los resúmenes de bovinos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorías de resúmenes regeneradas exitosamente",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @GetMapping("/categories")
    public ResponseEntity<Integer> refreshAllCategoriesSummary() {
        int updated = bovinesSummaryProcessor.refreshAllCategoriesSummary();
        return ResponseEntity.ok(updated);
    }
}
