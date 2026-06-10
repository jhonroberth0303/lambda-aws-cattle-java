package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureEventHistoryItemDTO;
import com.cattle.dtos.PastureEventRequestDTO;
import com.cattle.dtos.PastureEventResponseDTO;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.enums.LogType;
import com.cattle.processor.PastureEventProcessor;
import com.cattle.processor.RotationPlanProcessor;
import com.cattle.services.PastureEventService;
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

@RestController
@RequestMapping("/farms/{farmId}/pastures")
@Tag(name = "Potreros", description = "Gestión de potreros y rotación de pasturas")
public class PastureController {

    private final RotationPlanProcessor rotationPlanProcessor;
    private final PastureEventProcessor pastureEventProcessor;
    private final PastureEventService pastureEventService;
    private final LambdaContext lambdaContext;

    public PastureController(RotationPlanProcessor rotationPlanProcessor, PastureEventProcessor pastureEventProcessor, PastureEventService pastureEventService, LambdaContext lambdaContext) {
        this.rotationPlanProcessor = rotationPlanProcessor;
        this.pastureEventProcessor = pastureEventProcessor;
        this.pastureEventService = pastureEventService;
        this.lambdaContext = lambdaContext;
    }

    @Operation(
            summary = "Obtener semáforo de rotación",
            description = "Obtiene el estado actual de rotación de todos los potreros de una finca (verde/amarillo/rojo)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de rotación obtenido exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RotationSemaphoreItemDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<RotationSemaphoreItemDTO>> getRotationSemaphore(
            @Parameter(description = "ID de la finca", required = true, example = "FARM#001")
            @PathVariable("farmId") String farmId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Obteniendo semáforo de rotación para finca: " + farmId);
        return ResponseEntity.ok(rotationPlanProcessor.getRotationSemaphoreItems(farmId).orElse(List.of()));
    }

    @GetMapping("/{pastureId}/events")
    public ResponseEntity<List<PastureEventHistoryItemDTO>> getEventHistory(
            @PathVariable("farmId") String farmId,
            @PathVariable("pastureId") String pastureId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Obteniendo historial de eventos para potrero: " + pastureId + " finca: " + farmId);
        return ResponseEntity.ok(pastureEventService.findByPasture(farmId, pastureId, 20));
    }

    @PostMapping("/{pastureId}/events")
    public ResponseEntity<PastureEventResponseDTO> applyEvent(
            @PathVariable("farmId") String farmId,
            @PathVariable("pastureId") String pastureId,
            @RequestBody PastureEventRequestDTO request
    ) {
        lambdaContext.logInfo(LogType.CONTROLLER, String.format("Aplicando evento '%s' al potrero '%s' de la finca '%s'", request.getEventType(), pastureId, farmId));
        return ResponseEntity.ok(pastureEventProcessor.applyEvent(farmId, pastureId, request));
    }
}
