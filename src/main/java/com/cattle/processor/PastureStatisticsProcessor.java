package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureStatisticsDTO;
import com.cattle.entities.Pasture;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.repository.PastureStatisticsRepository;
import com.cattle.services.PastureService;
import com.cattle.services.PastureStatisticsService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PastureStatisticsProcessor {

    private final PastureService pastureService;
    private final PastureStatisticsRepository repository;
    private final PastureStatisticsService statisticsService;
    private final LambdaContext lambdaContext;

    public PastureStatisticsProcessor(
            PastureService pastureService,
            PastureStatisticsRepository repository,
            PastureStatisticsService statisticsService,
            LambdaContext lambdaContext
    ) {
        this.pastureService = pastureService;
        this.repository = repository;
        this.statisticsService = statisticsService;
        this.lambdaContext = lambdaContext;
    }

    public PastureStatisticsDTO computeForPasture(String farmId, String pastureId, LocalDate from, LocalDate to) {
        lambdaContext.logInfo(LogType.PROCESSOR,
                "Calculando estadísticas para potrero=" + pastureId + " finca=" + farmId);

        Pasture pasture = pastureService.getPastures(farmId)
                .orElse(List.of())
                .stream()
                .filter(p -> pastureId.equals(p.getId()))
                .findFirst()
                .orElse(null);

        String pastureName = pasture != null ? pasture.getName() : pastureId;
        String species = pasture != null ? pasture.getSpecies() : null;

        List<PastureEventItem> events = repository.findByPastureInRange(farmId, pastureId, from, to);
        return statisticsService.aggregate(pastureId, pastureName, species, events, from, to);
    }

    public List<PastureStatisticsDTO> computeForFarm(String farmId, LocalDate from, LocalDate to) {
        lambdaContext.logInfo(LogType.PROCESSOR,
                "Calculando estadísticas para finca=" + farmId + " from=" + from + " to=" + to);

        List<Pasture> pastures = pastureService.getPastures(farmId).orElse(List.of());

        return pastures.stream()
                .map(pasture -> {
                    List<PastureEventItem> events = repository.findByPastureInRange(
                            farmId, pasture.getId(), from, to);
                    return statisticsService.aggregate(
                            pasture.getId(), pasture.getName(), pasture.getSpecies(), events, from, to);
                })
                .sorted(Comparator.comparingDouble(
                        (PastureStatisticsDTO dto) -> dto.getUtilizationPercent() != null
                                ? dto.getUtilizationPercent() : -1.0
                ).reversed())
                .collect(Collectors.toList());
    }
}
