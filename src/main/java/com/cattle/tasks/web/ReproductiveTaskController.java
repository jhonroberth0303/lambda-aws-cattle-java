package com.cattle.tasks.web;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.tasks.dto.BovineTasksDTO;
import com.cattle.tasks.dto.ReproductiveTaskDTO;
import com.cattle.tasks.dto.ServiceFollowUpDTO;
import com.cattle.tasks.service.ReproductiveTaskQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Agenda reproductiva de la finca (HU-20260929). Solo lectura: las tareas se crean, cumplen
 * y cancelan registrando eventos. Hereda la seguridad de {@code /farms/**}.
 */
@RestController
@RequestMapping("/farms/{farmId}")
@Tag(name = "Tareas reproductivas", description = "Agenda del ciclo reproductivo y semáforo post-servicio")
public class ReproductiveTaskController {

    private final ReproductiveTaskQueryService queryService;
    private final LambdaContext lambdaContext;

    public ReproductiveTaskController(ReproductiveTaskQueryService queryService, LambdaContext lambdaContext) {
        this.queryService = queryService;
        this.lambdaContext = lambdaContext;
    }

    @Operation(summary = "Agenda de tareas abiertas de la finca",
            description = "Tareas PENDIENTE/VENCIDA con vencimiento hasta 'to' (por defecto hoy + 6 días), incluidas las vencidas.")
    @GetMapping("/tasks")
    public ResponseEntity<List<ReproductiveTaskDTO>> farmAgenda(
            @Parameter(description = "ID de la finca", required = true) @PathVariable("farmId") String farmId,
            @Parameter(description = "Fecha límite yyyy-MM-dd (opcional)") @RequestParam(value = "to", required = false) String to) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Listing reproductive task agenda. farmId=" + farmId + ", to=" + to);
        return ResponseEntity.ok(queryService.farmAgenda(farmId, parseDate(to)));
    }

    @Operation(summary = "Tareas y semáforo de un bovino")
    @GetMapping("/bovines/{bovineId}/tasks")
    public ResponseEntity<BovineTasksDTO> bovineTasks(
            @Parameter(description = "ID de la finca", required = true) @PathVariable("farmId") String farmId,
            @Parameter(description = "ID del bovino", required = true) @PathVariable("bovineId") String bovineId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Listing bovine reproductive tasks. farmId=" + farmId
                + ", bovineId=" + bovineId);
        return ResponseEntity.ok(queryService.bovineTasks(farmId, bovineId));
    }

    @Operation(summary = "Hembras servidas en seguimiento (semáforo)",
            description = "Ordenadas de rojo a verde y, dentro de cada color, por más días desde el servicio.")
    @GetMapping("/service-followups")
    public ResponseEntity<List<ServiceFollowUpDTO>> serviceFollowUps(
            @Parameter(description = "ID de la finca", required = true) @PathVariable("farmId") String farmId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Listing service follow-ups. farmId=" + farmId);
        return ResponseEntity.ok(queryService.serviceFollowUps(farmId));
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("El parámetro 'to' debe tener formato yyyy-MM-dd, recibido: '" + value + "'");
        }
    }
}
