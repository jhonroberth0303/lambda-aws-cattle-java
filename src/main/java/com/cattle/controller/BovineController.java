package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineDTO;
import com.cattle.dtos.BovineEventHistoryItemDTO;
import com.cattle.dtos.BovineEventRequestDTO;
import com.cattle.dtos.BovineEventResponseDTO;
import com.cattle.enums.LogType;
import com.cattle.processor.BovineEventProcessor;
import com.cattle.processor.BovineProcessor;
import com.cattle.services.BovineEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/farms/{farmId}/bovines")
@Tag(name = "Bovinos", description = "CRUD de bovinos y registro de eventos del ciclo de vida")
public class BovineController {

    private final BovineProcessor bovineProcessor;
    private final BovineEventProcessor bovineEventProcessor;
    private final BovineEventService bovineEventService;
    private final LambdaContext lambdaContext;

    public BovineController(BovineProcessor bovineProcessor,
                            BovineEventProcessor bovineEventProcessor,
                            BovineEventService bovineEventService,
                            LambdaContext lambdaContext) {
        this.bovineProcessor = bovineProcessor;
        this.bovineEventProcessor = bovineEventProcessor;
        this.bovineEventService = bovineEventService;
        this.lambdaContext = lambdaContext;
    }

    @Operation(
            summary = "Listar bovinos de la finca",
            description = "Obtiene la lista completa de bovinos registrados en la finca"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "204", description = "No hay bovinos registrados", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<BovineDTO>> getAll(
            @Parameter(description = "ID de la finca", required = true, example = "F001")
            @PathVariable("farmId") String farmId) {
        List<BovineDTO> list = bovineProcessor.findAll();
        if (list == null || list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Buscar bovino por ID",
            description = "Obtiene los detalles de un bovino específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bovino encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bovino no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BovineDTO> findById(
            @Parameter(description = "ID de la finca", required = true, example = "F001")
            @PathVariable("farmId") String farmId,
            @Parameter(description = "ID del bovino", required = true, example = "123")
            @PathVariable Integer id) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Buscando bovino ID: " + id + " finca: " + farmId);
        if (Objects.isNull(id) || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return bovineProcessor.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Registrar nuevo bovino",
            description = "Crea un nuevo registro de bovino en el sistema. La finca se toma de la ruta "
                    + "(formato F + 3 dígitos, ej. F001); el farmId del cuerpo se ignora."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bovino creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o finca con formato inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BovineDTO> save(
            @Parameter(description = "ID de la finca", required = true, example = "F001")
            @PathVariable("farmId") String farmId,
            @Valid @RequestBody BovineDTO bovineDTO) {
        if (!isCanonicalFarmId(farmId)) {
            lambdaContext.logInfo(LogType.CONTROLLER, "Alta rechazada: farmId con formato inválido: " + sanitizeForLog(farmId));
            return ResponseEntity.badRequest().build();
        }
        // La finca de la ruta es la fuente de verdad (HU-20261120, RF2).
        bovineDTO.setFarmId(farmId);
        return bovineProcessor.save(bovineDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).build());
    }

    @Operation(
            summary = "Actualizar bovino",
            description = "Edita un bovino existente haciendo merge: solo cambian los campos enviados; los "
                    + "omitidos (o null) conservan su valor. En los textos opcionales (color, raza, padres) "
                    + "un \"\" vacía el campo. createdAt nunca cambia. La finca se toma de la ruta "
                    + "(formato F + 3 dígitos, ej. F001)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bovino actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, ID no coincide o finca con formato inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BovineDTO> update(
            @Parameter(description = "ID de la finca", required = true, example = "F001")
            @PathVariable("farmId") String farmId,
            @Parameter(description = "ID del bovino a actualizar", required = true, example = "123")
            @PathVariable Long id,
            @Valid @RequestBody BovineDTO bovineDTO) {
        if (bovineDTO.getBovineId() == null || bovineDTO.getBovineId() <= 0
                || !Objects.equals(Long.valueOf(bovineDTO.getBovineId()), id)) {
            return ResponseEntity.badRequest().build();
        }
        if (!isCanonicalFarmId(farmId)) {
            lambdaContext.logInfo(LogType.CONTROLLER, "Edición rechazada: farmId con formato inválido: " + sanitizeForLog(farmId));
            return ResponseEntity.badRequest().build();
        }
        // La finca de la ruta es la fuente de verdad (HU-20261120, RF2).
        bovineDTO.setFarmId(farmId);
        return bovineProcessor.update(bovineDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).build());
    }

    @Operation(
            summary = "Obtener historial de eventos del bovino",
            description = "Retorna los últimos 20 eventos registrados para un bovino"
    )
    @GetMapping("/{bovineId}/events")
    public ResponseEntity<List<BovineEventHistoryItemDTO>> getEventHistory(
            @Parameter(description = "ID de la finca", required = true, example = "F001")
            @PathVariable("farmId") String farmId,
            @Parameter(description = "ID del bovino", required = true)
            @PathVariable("bovineId") String bovineId) {
        lambdaContext.logInfo(LogType.CONTROLLER,
                "Obteniendo historial de eventos para bovino: " + bovineId + " finca: " + farmId);
        return ResponseEntity.ok(bovineEventService.findByBovine(farmId, bovineId, 20));
    }

    @Operation(
            summary = "Registrar evento bovino",
            description = "Registra un evento del ciclo de vida de un bovino"
    )
    @PostMapping("/{bovineId}/events")
    public ResponseEntity<BovineEventResponseDTO> applyEvent(
            @Parameter(description = "ID de la finca", required = true, example = "F001")
            @PathVariable("farmId") String farmId,
            @Parameter(description = "ID del bovino", required = true)
            @PathVariable("bovineId") String bovineId,
            @RequestBody BovineEventRequestDTO request) {
        lambdaContext.logInfo(LogType.CONTROLLER, String.format(
                "Aplicando evento '%s' al bovino '%s' de la finca '%s'",
                request != null ? request.getEventType() : "null", bovineId, farmId));
        return ResponseEntity.ok(bovineEventProcessor.applyEvent(farmId, bovineId, request));
    }

    /** Formato canónico de finca: F + 3 dígitos (HU-20261120, RF1). Se exige donde la finca se persiste. */
    private static final java.util.regex.Pattern CANONICAL_FARM_ID = java.util.regex.Pattern.compile("^F[0-9]{3}$");

    static boolean isCanonicalFarmId(String farmId) {
        return farmId != null && CANONICAL_FARM_ID.matcher(farmId).matches();
    }

    /** Evita inyección en logs (CWE-117): solo caracteres seguros y como máximo 20; el resto se reemplaza por '_'. */
    static String sanitizeForLog(String value) {
        if (value == null) {
            return "null";
        }
        String truncated = value.length() > 20 ? value.substring(0, 20) + "…" : value;
        return truncated.replaceAll("[^A-Za-z0-9#_\\-…]", "_");
    }
}
