package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineDTO;
import com.cattle.enums.LogType;
import com.cattle.processor.BovineProcessor;
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
@RequestMapping("/bovines")
@Tag(name = "Bovinos", description = "Operaciones CRUD para gestión de bovinos")
public class BovineController {

    private final BovineProcessor bovineProcessor;
    private final LambdaContext lambdaContext;

    public BovineController(BovineProcessor bovineProcessor, LambdaContext lambdaContext) {
        this.bovineProcessor = bovineProcessor;
        this.lambdaContext = lambdaContext;
    }

    @Operation(
            summary = "Listar todos los bovinos",
            description = "Obtiene la lista completa de bovinos registrados en la finca"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de bovinos obtenida exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "204", description = "No hay bovinos registrados", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<BovineDTO>> getAll() {
        List<BovineDTO> bovinesDTOList = bovineProcessor.findAll();
        if (bovinesDTOList == null || bovinesDTOList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bovinesDTOList);
    }

    @Operation(
            summary = "Buscar bovino por ID",
            description = "Obtiene los detalles de un bovino específico por su identificador único"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bovino encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bovino no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BovineDTO> findById(
            @Parameter(description = "ID del bovino", required = true, example = "123")
            @PathVariable Integer id) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Received request to find bovine with ID: " + id);
        if (Objects.isNull(id) || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return bovineProcessor.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Registrar nuevo bovino",
            description = "Crea un nuevo registro de bovino en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bovino creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BovineDTO> save(@Valid @RequestBody BovineDTO bovineDTO) {
        return bovineProcessor.save(bovineDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).build());
    }

    @Operation(
            summary = "Actualizar bovino",
            description = "Actualiza los datos de un bovino existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bovino actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BovineDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o ID no coincide", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BovineDTO> update(
            @Parameter(description = "ID del bovino a actualizar", required = true, example = "123")
            @PathVariable Long id,
            @Valid @RequestBody BovineDTO bovineDTO) {
        if (bovineDTO.getBovineId() == null || bovineDTO.getBovineId() <= 0
                || !Objects.equals(Long.valueOf(bovineDTO.getBovineId()), id)) {
            return ResponseEntity.badRequest().build();
        }
        return bovineProcessor.update(bovineDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).build());
    }

}
