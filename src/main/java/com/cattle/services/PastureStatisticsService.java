package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureLaborSummaryItemDTO;
import com.cattle.dtos.PastureStatisticsDTO;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PastureStatisticsService {

    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;

    public PastureStatisticsService(ObjectMapper objectMapper, LambdaContext lambdaContext) {
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
    }

    public PastureStatisticsDTO aggregate(
            String pastureId,
            String pastureName,
            String species,
            List<PastureEventItem> events,
            LocalDate from,
            LocalDate to
    ) {
        List<PastureEventItem> sorted = events.stream()
                .filter(e -> e.getEventAt() != null)
                .sorted(Comparator.comparing(PastureEventItem::getEventAt))
                .collect(Collectors.toList());

        List<PastureEventItem> laborEvents = sorted.stream()
                .filter(e -> isLaborEvent(e.getEventType()))
                .collect(Collectors.toList());

        List<long[]> cycles = new ArrayList<>(); // [daysInUse, residualCm (-1 si nulo)]
        List<Long> restDaysList = new ArrayList<>();

        Instant openAt = null;
        Instant prevCloseAt = null;

        for (PastureEventItem event : sorted) {
            String type = event.getEventType();
            if ("OPEN".equals(type)) {
                openAt = event.getEventAt();
                if (prevCloseAt != null) {
                    long restDays = ChronoUnit.DAYS.between(prevCloseAt, openAt);
                    if (restDays >= 0) restDaysList.add(restDays);
                }
            } else if ("CLOSE".equals(type) && openAt != null) {
                long daysInUse = Math.max(1L, ChronoUnit.DAYS.between(openAt, event.getEventAt()));
                int residual = parseResidualCm(event.getPayloadJson());
                cycles.add(new long[]{daysInUse, residual});
                prevCloseAt = event.getEventAt();
                openAt = null;
            }
        }

        long totalPeriodDays = ChronoUnit.DAYS.between(from, to) + 1;
        totalPeriodDays = Math.max(1, totalPeriodDays);

        Integer cyclesCompleted = cycles.isEmpty() ? null : cycles.size();
        Double avgDaysInUse = cycles.isEmpty() ? null : cycles.stream().mapToLong(c -> c[0]).average().orElse(0);
        Double avgRestDays = restDaysList.isEmpty() ? null : restDaysList.stream().mapToLong(Long::longValue).average().orElse(0);

        long totalDaysInUse = cycles.stream().mapToLong(c -> c[0]).sum();
        Double utilizationPercent = cycles.isEmpty() ? null
                : Math.min(100.0, Math.round((totalDaysInUse * 1000.0 / totalPeriodDays)) / 10.0);

        List<Integer> residuals = cycles.stream()
                .filter(c -> c[1] >= 0)
                .map(c -> (int) c[1])
                .collect(Collectors.toList());
        Integer avgResidualCm = residuals.isEmpty() ? null
                : (int) residuals.stream().mapToInt(Integer::intValue).average().orElse(0);
        Integer lastResidualCm = residuals.isEmpty() ? null : residuals.get(residuals.size() - 1);

        Integer laborCount = laborEvents.isEmpty() ? null : laborEvents.size();
        List<PastureLaborSummaryItemDTO> laborSummary = buildLaborSummary(laborEvents);

        lambdaContext.logInfo(LogType.SERVICE,
                "Estadísticas calculadas para potrero=" + pastureId + " ciclos=" + cyclesCompleted);

        return PastureStatisticsDTO.builder()
                .pastureId(pastureId)
                .pastureName(pastureName)
                .species(species)
                .from(from.toString())
                .to(to.toString())
                .cyclesCompleted(cyclesCompleted)
                .avgDaysInUse(avgDaysInUse)
                .avgRestDays(avgRestDays)
                .utilizationPercent(utilizationPercent)
                .avgResidualCm(avgResidualCm)
                .lastResidualCm(lastResidualCm)
                .laborCount(laborCount)
                .laborSummary(laborSummary.isEmpty() ? null : laborSummary)
                .build();
    }

    private int parseResidualCm(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) return -1;
        try {
            JsonNode node = objectMapper.readTree(payloadJson);
            JsonNode field = node.get("residualCm");
            if (field != null && !field.isNull()) return field.asInt(-1);
        } catch (Exception ex) {
            lambdaContext.logInfo(LogType.SERVICE, "No se pudo parsear residualCm: " + ex.getMessage());
        }
        return -1;
    }

    private List<PastureLaborSummaryItemDTO> buildLaborSummary(List<PastureEventItem> laborEvents) {
        Map<String, LaborAccumulator> acc = new LinkedHashMap<>();
        for (PastureEventItem e : laborEvents) {
            acc.computeIfAbsent(e.getEventType(), t -> new LaborAccumulator()).add(e.getEventAt());
        }
        return acc.entrySet().stream()
                .map(entry -> PastureLaborSummaryItemDTO.builder()
                        .eventType(entry.getKey())
                        .count(entry.getValue().count)
                        .lastAt(entry.getValue().lastAt != null ? entry.getValue().lastAt.toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private boolean isLaborEvent(String eventType) {
        return "FERTILIZED".equals(eventType) || "LIMED".equals(eventType)
                || "HEIGHT_MEASURED".equals(eventType) || "OBSERVATION_ADDED".equals(eventType);
    }

    private static class LaborAccumulator {
        int count = 0;
        Instant lastAt = null;

        void add(Instant at) {
            count++;
            if (at != null && (lastAt == null || at.isAfter(lastAt))) {
                lastAt = at;
            }
        }
    }
}
