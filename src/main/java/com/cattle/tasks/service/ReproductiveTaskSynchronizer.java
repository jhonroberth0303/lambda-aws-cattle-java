package com.cattle.tasks.service;

import com.cattle.config.AppProperties;
import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.LogType;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.repository.BovineEventRepository;
import com.cattle.repository.BovineRepository;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.repository.ProfileLifecycleRepository;
import com.cattle.repository.ProfileReproductiveRepository;
import com.cattle.tasks.ReproductiveTaskStatus;
import com.cattle.tasks.entity.ReproductiveTaskItem;
import com.cattle.tasks.entity.ServiceFollowUpItem;
import com.cattle.tasks.planner.BovineReproductiveContext;
import com.cattle.tasks.planner.PlannedFollowUp;
import com.cattle.tasks.planner.PlannedTask;
import com.cattle.tasks.planner.PlannerEvent;
import com.cattle.tasks.planner.ReproductivePlan;
import com.cattle.tasks.planner.ReproductiveTaskPlanner;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import com.cattle.tasks.repository.ReproductiveTaskRepository;
import com.cattle.utils.EventDates;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sincroniza las tareas guardadas de un bovino con el plan que calcula
 * {@link ReproductiveTaskPlanner} a partir de su historial. Es la única operación de
 * escritura del motor: la usan el registro en línea, la reconciliación diaria y la carga
 * inicial (HU-20260929, Opción D rama temporal).
 * <ul>
 *   <li>crea solo tareas del ciclo actual que no existan (escritura condicional, G1, CA18);</li>
 *   <li>actualiza solo transiciones de estado válidas (G3), con escritura condicional sobre el
 *       estado leído para no pisar una sincronización concurrente (revisión H3);</li>
 *   <li>nunca modifica el vencimiento de una tarea existente (CA12).</li>
 * </ul>
 */
@Service
public class ReproductiveTaskSynchronizer {

    private static final String LIFECYCLE_SK = "PROFILE#LIFECYCLE";
    private static final String REPRODUCTIVE_SK = "PROFILE#REPRODUCTIVE";
    private static final String LACTATING = "LACTATING";
    private static final String FEMALE = "female";

    private final BovineRepository bovineRepository;
    private final ProfileLifecycleRepository lifecycleRepository;
    private final ProfileReproductiveRepository reproductiveRepository;
    private final ProfileLactancyRepository lactancyRepository;
    private final BovineEventRepository bovineEventRepository;
    private final ReproductiveTaskRepository taskRepository;
    private final ReproductiveTaskPlanner planner;
    private final ReproductiveTaskSettingsProvider settingsProvider;
    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;
    private final ZoneId zoneId;

    public ReproductiveTaskSynchronizer(BovineRepository bovineRepository,
                                        ProfileLifecycleRepository lifecycleRepository,
                                        ProfileReproductiveRepository reproductiveRepository,
                                        ProfileLactancyRepository lactancyRepository,
                                        BovineEventRepository bovineEventRepository,
                                        ReproductiveTaskRepository taskRepository,
                                        ReproductiveTaskPlanner planner,
                                        ReproductiveTaskSettingsProvider settingsProvider,
                                        ObjectMapper objectMapper,
                                        LambdaContext lambdaContext,
                                        AppProperties appProperties) {
        this.bovineRepository = bovineRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.reproductiveRepository = reproductiveRepository;
        this.lactancyRepository = lactancyRepository;
        this.bovineEventRepository = bovineEventRepository;
        this.taskRepository = taskRepository;
        this.planner = planner;
        this.settingsProvider = settingsProvider;
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
        this.zoneId = ZoneId.of(appProperties.getTimezone());
    }

    public record SyncResult(int created, int updated, String skippedReason) {
        static SyncResult skipped(String reason) {
            return new SyncResult(0, 0, reason);
        }
    }

    /** Sincroniza con los parámetros vigentes y la fecha operativa de hoy. */
    public SyncResult sync(String requestFarmId, String bovineId) {
        return sync(requestFarmId, bovineId, settingsProvider.current(), LocalDate.now(zoneId));
    }

    /** Variante con parámetros explícitos: busca la identidad del bovino. */
    public SyncResult sync(String farmId, String bovineId, ReproductiveTaskSettings settings, LocalDate today) {
        Optional<BovineIdentityItem> identity = findIdentity(bovineId);
        if (identity.isEmpty()) {
            return SyncResult.skipped("bovino sin identidad");
        }
        return sync(farmId, identity.get(), settings, today);
    }

    /**
     * Variante para lotes: reutiliza la identidad y los parámetros ya cargados.
     * <p>
     * {@code farmId} es la finca de contexto de quien llama (la ruta del registro o la finca
     * configurada del batch), la misma con la que se guardan los eventos y se consulta la
     * agenda. No se usa {@code BovineIdentityItem.farmId}: en los datos reales tiene otro
     * formato ({@code FARM#001}) o falta (revisión H9, DT-20260914).
     */
    public SyncResult sync(String farmId, BovineIdentityItem identity, ReproductiveTaskSettings settings,
                           LocalDate today) {
        String bovineId = String.valueOf(identity.getBovineId());
        if (farmId == null || farmId.isBlank()) {
            lambdaContext.logInfo(LogType.SERVICE, "Tareas reproductivas omitidas: sin finca de contexto. bovineId=" + bovineId);
            return SyncResult.skipped("sin finca de contexto");
        }
        boolean female = FEMALE.equalsIgnoreCase(identity.getGender());
        if (!female) {
            return SyncResult.skipped("no es hembra");
        }

        String pk = "BOVINE#" + bovineId;
        BovineReproductiveContext context = new BovineReproductiveContext(
                bovineId, farmId, true, isActive(pk), isLactating(pk));
        List<PlannerEvent> events = bovineEventRepository.findAllByBovine(bovineId).stream()
                .map(this::toPlannerEvent)
                .filter(Objects::nonNull)
                .toList();

        ReproductivePlan plan = planner.plan(context, events, settings, today);
        SyncResult result = applyTasks(context, plan.tasks());
        applyFollowUp(context, plan.followUp());
        if (plan.diagnosisWithoutService()) {
            lambdaContext.logInfo(LogType.SERVICE,
                    "Diagnóstico positivo sin servicio registrado; no se programa gestación. bovineId=" + bovineId);
        }
        return result;
    }

    private SyncResult applyTasks(BovineReproductiveContext context, List<PlannedTask> planned) {
        Map<String, ReproductiveTaskItem> stored = taskRepository.findTasksByBovine(context.bovineId()).stream()
                .collect(Collectors.toMap(ReproductiveTaskItem::getSk, Function.identity(), (a, b) -> a));
        String now = Instant.now().toString();
        int created = 0;
        int updated = 0;
        for (PlannedTask task : planned) {
            ReproductiveTaskItem existing = stored.get(ReproductiveTaskItem.sortKey(task.key()));
            if (existing != null) {
                String readStatus = existing.getStatus();
                if (applyTransition(existing, task, now)) {
                    if (taskRepository.saveIfStatus(existing, readStatus)) {
                        updated++;
                    } else {
                        // H3: otra sincronización la cambió después de leerla; su estado gana y
                        // la reconciliación diaria vuelve a converger si hiciera falta.
                        lambdaContext.logInfo(LogType.SERVICE, "Transición de tarea descartada por concurrencia. sk="
                                + existing.getSk());
                    }
                }
            } else if (task.currentCycle() && context.active()
                    && taskRepository.createIfAbsent(newItem(context, task, now))) {
                created++;
            }
        }
        return new SyncResult(created, updated, null);
    }

    /** G3: solo transiciones válidas; el vencimiento guardado no se toca (CA12). */
    private boolean applyTransition(ReproductiveTaskItem existing, PlannedTask task, String now) {
        ReproductiveTaskStatus current = parseStatus(existing.getStatus());
        if (current == null || !current.canTransitionTo(task.status())) {
            return false;
        }
        existing.setStatus(task.status().name());
        existing.setClosedByEventId(task.closedByEventId());
        existing.setClosedAt(task.closedAt() != null ? task.closedAt().toString() : null);
        existing.setLateCompletion(task.lateCompletion());
        existing.setUpdatedAt(now);
        existing.applyOpenIndex(task.status().isOpen());
        return true;
    }

    private ReproductiveTaskItem newItem(BovineReproductiveContext context, PlannedTask task, String now) {
        ReproductiveTaskItem item = new ReproductiveTaskItem();
        item.setPk(ReproductiveTaskItem.partitionKey(context.bovineId()));
        item.setSk(ReproductiveTaskItem.sortKey(task.key()));
        item.setTaskId(task.key());
        item.setEntityType(ReproductiveTaskItem.ENTITY_BOVINE);
        item.setTaskType(task.taskType().name());
        item.setRuleCode(task.rule().name());
        item.setBovineId(context.bovineId());
        item.setFarmId(context.farmId());
        item.setDueDate(task.dueDate().toString());
        item.setWindowEnd(task.windowEnd().toString());
        item.setStatus(task.status().name());
        item.setOriginEventId(task.originEventId());
        item.setClosedByEventId(task.closedByEventId());
        item.setClosedAt(task.closedAt() != null ? task.closedAt().toString() : null);
        item.setLateCompletion(task.lateCompletion());
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item.applyOpenIndex(task.status().isOpen());
        return item;
    }

    private void applyFollowUp(BovineReproductiveContext context, PlannedFollowUp planned) {
        if (planned == null) {
            return;
        }
        Optional<ServiceFollowUpItem> existing = taskRepository.findFollowUp(context.bovineId());
        if (existing.isPresent() && sameFollowUp(existing.get(), planned)) {
            return;
        }
        ServiceFollowUpItem item = new ServiceFollowUpItem();
        item.setPk(ReproductiveTaskItem.partitionKey(context.bovineId()));
        item.setSk(ServiceFollowUpItem.SORT_KEY);
        item.setBovineId(context.bovineId());
        item.setFarmId(context.farmId());
        item.setLastServiceDate(planned.lastServiceDate().toString());
        item.setLastServiceEventId(planned.lastServiceEventId());
        item.setServiceNumber(planned.serviceNumber());
        item.setOpen(planned.open());
        item.setClosedByEventId(planned.closedByEventId());
        item.setUpdatedAt(Instant.now().toString());
        item.applyOpenIndex();
        taskRepository.saveFollowUp(item);
    }

    private static boolean sameFollowUp(ServiceFollowUpItem stored, PlannedFollowUp planned) {
        return Objects.equals(stored.getLastServiceEventId(), planned.lastServiceEventId())
                && Objects.equals(stored.getServiceNumber(), planned.serviceNumber())
                && Objects.equals(stored.getOpen(), planned.open())
                && Objects.equals(stored.getClosedByEventId(), planned.closedByEventId());
    }

    private Optional<BovineIdentityItem> findIdentity(String bovineId) {
        try {
            return bovineRepository.findById(Integer.valueOf(bovineId.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /** G4: sin perfil de ciclo de vida (dato legacy) se asume activo, como el guard del processor. */
    private boolean isActive(String pk) {
        return lifecycleRepository.findById(pk, LIFECYCLE_SK)
                .map(this::isActive)
                .orElse(true);
    }

    private boolean isActive(ProfileLifecycle lifecycle) {
        boolean disabled = Boolean.FALSE.equals(lifecycle.getEnabled());
        boolean inactive = lifecycle.getStatus() != null && lifecycle.getStatus().isInactive();
        return !disabled && !inactive;
    }

    /** Misma definición de lactancia activa que MilkingProcessor: LACTATING y sin fecha de fin. */
    private boolean isLactating(String pk) {
        return reproductiveRepository.findById(pk, REPRODUCTIVE_SK)
                .map(reproductive -> reproductive.getCurrentLactationId())
                .filter(id -> !id.isBlank())
                .flatMap(id -> lactancyRepository.findById(pk, id))
                .map(ReproductiveTaskSynchronizer::isLactating)
                .orElse(false);
    }

    private static boolean isLactating(ProfileLactancy lactancy) {
        return LACTATING.equalsIgnoreCase(lactancy.getStatus()) && lactancy.getEndDate() == null;
    }

    private PlannerEvent toPlannerEvent(BovineEventItem item) {
        BovineEventType type;
        try {
            type = BovineEventType.valueOf(item.getEventType());
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
        LocalDate date = EventDates.toOperationalDate(item.getEventAt(), zoneId);
        if (date == null) {
            return null;
        }
        return new PlannerEvent(item.getEventId(), type, date, item.getCreatedAt(), parsePayload(item));
    }

    private Map<String, Object> parsePayload(BovineEventItem item) {
        if (item.getPayloadJson() == null || item.getPayloadJson().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(item.getPayloadJson(), new TypeReference<Map<String, Object>>() { });
        } catch (Exception ex) {
            lambdaContext.logException(LogType.SERVICE, "Payload de evento ilegible; se ignora. eventId="
                    + item.getEventId(), ex);
            return Map.of();
        }
    }

    private static ReproductiveTaskStatus parseStatus(String status) {
        try {
            return status == null ? null : ReproductiveTaskStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
