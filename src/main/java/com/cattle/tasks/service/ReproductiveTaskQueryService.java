package com.cattle.tasks.service;

import com.cattle.config.AppProperties;
import com.cattle.tasks.FollowUpColor;
import com.cattle.tasks.ReproductiveTaskStatus;
import com.cattle.tasks.dto.BovineTasksDTO;
import com.cattle.tasks.dto.ReproductiveTaskDTO;
import com.cattle.tasks.dto.ServiceFollowUpDTO;
import com.cattle.tasks.entity.ReproductiveTaskItem;
import com.cattle.tasks.entity.ServiceFollowUpItem;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import com.cattle.tasks.planner.ServiceFollowUpEvaluator;
import com.cattle.tasks.repository.ReproductiveTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lecturas de la agenda reproductiva: agenda de la finca, tareas de un bovino y semáforo.
 */
@Service
public class ReproductiveTaskQueryService {

    /** "Esta semana" = hoy + 6 días (CA4). */
    public static final int DEFAULT_AGENDA_DAYS = 6;

    private final ReproductiveTaskRepository repository;
    private final ReproductiveTaskSettingsProvider settingsProvider;
    private final ZoneId zoneId;

    public ReproductiveTaskQueryService(ReproductiveTaskRepository repository,
                                        ReproductiveTaskSettingsProvider settingsProvider,
                                        AppProperties appProperties) {
        this.repository = repository;
        this.settingsProvider = settingsProvider;
        this.zoneId = ZoneId.of(appProperties.getTimezone());
    }

    public LocalDate today() {
        return LocalDate.now(zoneId);
    }

    /** Tareas abiertas de la finca con vencimiento hasta {@code upTo} (incluye vencidas), por fecha. */
    public List<ReproductiveTaskDTO> farmAgenda(String farmId, LocalDate upTo) {
        return farmAgenda(farmId, upTo, today());
    }

    /** Variante con fecha explícita (p. ej. el {@code now} del scheduler de notificaciones). */
    public List<ReproductiveTaskDTO> farmAgenda(String farmId, LocalDate upTo, LocalDate today) {
        LocalDate limit = upTo != null ? upTo : today.plusDays(DEFAULT_AGENDA_DAYS);
        return repository.findOpenTasksByFarm(farmId, limit).stream()
                .map(item -> toDTO(item, today))
                .sorted(Comparator.comparing(ReproductiveTaskDTO::getDueDate))
                .toList();
    }

    /** Todas las tareas del bovino (abiertas primero, por vencimiento) y su semáforo. */
    public BovineTasksDTO bovineTasks(String farmId, String bovineId) {
        LocalDate today = today();
        List<ReproductiveTaskDTO> tasks = repository.findTasksByBovine(bovineId).stream()
                .filter(item -> farmId.equals(item.getFarmId()))
                .map(item -> toDTO(item, today))
                .sorted(Comparator.comparing((ReproductiveTaskDTO t) -> !isOpen(t.getStatus()))
                        .thenComparing(ReproductiveTaskDTO::getDueDate))
                .toList();
        ServiceFollowUpDTO followUp = repository.findFollowUp(bovineId)
                .filter(item -> farmId.equals(item.getFarmId()))
                .map(item -> toDTO(item, today, settingsProvider.current()))
                .orElse(null);
        return BovineTasksDTO.builder().bovineId(bovineId).tasks(tasks).followUp(followUp).build();
    }

    /** Semáforo de las hembras servidas sin preñez confirmada, de rojo a verde y luego por días. */
    public List<ServiceFollowUpDTO> serviceFollowUps(String farmId) {
        LocalDate today = today();
        ReproductiveTaskSettings settings = settingsProvider.current();
        return repository.findOpenFollowUps(farmId).stream()
                .map(item -> toDTO(item, today, settings))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ServiceFollowUpDTO::getColor, Comparator.comparing(this::colorRank))
                        .reversed()
                        .thenComparing(Comparator.comparingLong(ServiceFollowUpDTO::getDaysSinceService).reversed()))
                .toList();
    }

    private int colorRank(String color) {
        return FollowUpColor.valueOf(color).ordinal();
    }

    private static boolean isOpen(String status) {
        try {
            return ReproductiveTaskStatus.valueOf(status).isOpen();
        } catch (IllegalArgumentException | NullPointerException ex) {
            return false;
        }
    }

    private static ReproductiveTaskDTO toDTO(ReproductiveTaskItem item, LocalDate today) {
        String windowEnd = item.getWindowEnd() != null ? item.getWindowEnd() : item.getDueDate();
        boolean overdue = isOpen(item.getStatus()) && windowEnd != null && today.isAfter(LocalDate.parse(windowEnd));
        return ReproductiveTaskDTO.builder()
                .taskId(item.getTaskId())
                .bovineId(item.getBovineId())
                .taskType(item.getTaskType())
                .ruleCode(item.getRuleCode())
                .dueDate(item.getDueDate())
                .windowEnd(windowEnd)
                .status(overdue ? ReproductiveTaskStatus.VENCIDA.name() : item.getStatus())
                .overdue(overdue)
                .originEventId(item.getOriginEventId())
                .closedByEventId(item.getClosedByEventId())
                .closedAt(item.getClosedAt())
                .lateCompletion(Boolean.TRUE.equals(item.getLateCompletion()))
                .build();
    }

    private static ServiceFollowUpDTO toDTO(ServiceFollowUpItem item, LocalDate today,
                                            ReproductiveTaskSettings settings) {
        if (item.getLastServiceDate() == null) {
            return null;
        }
        int serviceNumber = item.getServiceNumber() != null ? item.getServiceNumber() : 1;
        ServiceFollowUpEvaluator.Evaluation evaluation = ServiceFollowUpEvaluator.evaluate(
                LocalDate.parse(item.getLastServiceDate()), serviceNumber, today, settings);
        return ServiceFollowUpDTO.builder()
                .bovineId(item.getBovineId())
                .lastServiceDate(item.getLastServiceDate())
                .serviceNumber(serviceNumber)
                .daysSinceService(evaluation.daysSinceService())
                .color(evaluation.color().name())
                .repeatBreeder(evaluation.repeatBreeder())
                .open(Boolean.TRUE.equals(item.getOpen()))
                .build();
    }
}
