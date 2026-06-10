package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.processor.PastureStatisticsProcessor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/farms/{farmId}/pastures")
public class PastureStatisticsController {

    private final PastureStatisticsProcessor processor;
    private final LambdaContext lambdaContext;

    public PastureStatisticsController(PastureStatisticsProcessor processor, LambdaContext lambdaContext) {
        this.processor = processor;
        this.lambdaContext = lambdaContext;
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getFarmStatistics(
            @PathVariable("farmId") String farmId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(90);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        if (effectiveFrom.isAfter(effectiveTo)) {
            lambdaContext.logInfo(LogType.CONTROLLER, "Fechas inválidas: from=" + effectiveFrom + " > to=" + effectiveTo);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        lambdaContext.logInfo(LogType.CONTROLLER,
                "Estadísticas de finca farmId=" + farmId + " from=" + effectiveFrom + " to=" + effectiveTo);
        return ResponseEntity.ok(processor.computeForFarm(farmId, effectiveFrom, effectiveTo));
    }

    @GetMapping("/{pastureId}/statistics")
    public ResponseEntity<?> getPastureStatistics(
            @PathVariable("farmId") String farmId,
            @PathVariable("pastureId") String pastureId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(90);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        if (effectiveFrom.isAfter(effectiveTo)) {
            lambdaContext.logInfo(LogType.CONTROLLER, "Fechas inválidas: from=" + effectiveFrom + " > to=" + effectiveTo);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        lambdaContext.logInfo(LogType.CONTROLLER,
                "Estadísticas de potrero pastureId=" + pastureId + " finca=" + farmId);
        return ResponseEntity.ok(processor.computeForPasture(farmId, pastureId, effectiveFrom, effectiveTo));
    }
}
