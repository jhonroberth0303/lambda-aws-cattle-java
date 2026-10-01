package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineEventRequestDTO;
import com.cattle.dtos.BovineEventResponseDTO;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.EventSource;
import com.cattle.enums.LogType;
import com.cattle.exceptions.NotFoundException;
import com.cattle.forms.EventFormSchema;
import com.cattle.forms.EventPayloadValidator;
import com.cattle.repository.BovineRepository;
import com.cattle.repository.ProfileLifecycleRepository;
import com.cattle.services.BovineEventService;
import com.cattle.services.EventFormCatalog;
import com.cattle.services.BovineSummaryService;
import com.cattle.services.ExitEventProjector;
import com.cattle.services.ReproductiveProfileProjector;
import com.cattle.tasks.service.ReproductiveTaskSynchronizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class BovineEventProcessor {

    private static final String DEFAULT_CREATED_BY = "manual-web";
    private static final String LIFECYCLE_SK = "PROFILE#LIFECYCLE";

    /** Eventos que dan de baja al animal: se permiten aunque el bovino ya no esté activo. */
    private static final Set<BovineEventType> EXIT_EVENTS =
            EnumSet.of(BovineEventType.MUERTE, BovineEventType.VENTA);

    private final BovineEventService bovineEventService;
    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;
    private final EventFormCatalog eventFormCatalog;
    private final EventPayloadValidator payloadValidator;
    private final BovineRepository bovineRepository;
    private final ProfileLifecycleRepository lifecycleRepository;
    private final ExitEventProjector exitEventProjector;
    private final ReproductiveTaskSynchronizer reproductiveTaskSynchronizer;
    private final ReproductiveProfileProjector reproductiveProfileProjector;
    private final BovineSummaryService bovineSummaryService;

    /**
     * Eventos que cambian la agenda reproductiva (HU-20260929): la generan, la cumplen o la
     * cancelan (VENTA/MUERTE, C4).
     */
    private static final Set<BovineEventType> REPRODUCTIVE_TASK_EVENTS = EnumSet.of(
            BovineEventType.MONTA, BovineEventType.INSEMINACION, BovineEventType.DIAGNOSTICO_PRENEZ,
            BovineEventType.CELO, BovineEventType.PREPARTO, BovineEventType.SECADO, BovineEventType.PARTO,
            BovineEventType.CONTROL_POSPARTO, BovineEventType.ABORTO, BovineEventType.VENTA, BovineEventType.MUERTE);

    public BovineEventProcessor(BovineEventService bovineEventService, ObjectMapper objectMapper,
                                LambdaContext lambdaContext, EventFormCatalog eventFormCatalog,
                                EventPayloadValidator payloadValidator, BovineRepository bovineRepository,
                                ProfileLifecycleRepository lifecycleRepository, ExitEventProjector exitEventProjector,
                                ReproductiveTaskSynchronizer reproductiveTaskSynchronizer,
                                ReproductiveProfileProjector reproductiveProfileProjector,
                                BovineSummaryService bovineSummaryService) {
        this.bovineEventService = bovineEventService;
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
        this.eventFormCatalog = eventFormCatalog;
        this.payloadValidator = payloadValidator;
        this.bovineRepository = bovineRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.exitEventProjector = exitEventProjector;
        this.reproductiveTaskSynchronizer = reproductiveTaskSynchronizer;
        this.reproductiveProfileProjector = reproductiveProfileProjector;
        this.bovineSummaryService = bovineSummaryService;
    }

    public BovineEventResponseDTO applyEvent(String farmId, String bovineId, BovineEventRequestDTO request) {
        lambdaContext.logInfo(LogType.PROCESSOR, "Aplicando evento bovino. farmId: " + farmId
                + ", bovineId: " + bovineId
                + ", eventType: " + (request != null ? request.getEventType() : "null"));

        validateIdentifiers(farmId, bovineId);
        validateRequest(request);

        BovineEventType eventType;
        try {
            eventType = BovineEventType.valueOf(request.getEventType().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de evento bovino no soportado: " + request.getEventType(), ex);
        }

        enforceBovineIsRegistrable(bovineId, eventType);

        Map<String, Object> payload = request.getPayload();
        validatePayloadByType(eventType, payload);

        String eventId = UUID.randomUUID().toString();
        Instant eventAt = resolveEventAt(request.getEventAt());
        String payloadJson = serializePayload(payload);
        String createdBy = normalizeCreatedBy(request.getCreatedBy());
        Instant now = Instant.now();

        BovineEventItem item = new BovineEventItem();
        item.setPk("BOVINE#" + bovineId);
        item.setSk("EVT#" + eventAt.toString() + "#" + eventType.name() + "#" + eventId);
        item.setBovineId(bovineId);
        item.setFarmId(farmId);
        item.setEventId(eventId);
        item.setEventType(eventType.name());
        item.setEventAt(eventAt);
        item.setSource(EventSource.MANUAL);
        item.setCreatedBy(createdBy);
        item.setPayloadJson(payloadJson);
        item.setNotes(extractNotes(payload));
        item.setCreatedAt(now.toString());
        item.setUpdatedAt(now.toString());

        bovineEventService.save(item);

        if (EXIT_EVENTS.contains(eventType)) {
            projectExitEvent(bovineId, eventType, eventAt);
        }
        // HU-20260930: perfiles antes que tareas (la regla SECAR lee la lactancia vigente) y la
        // tarjeta al final para que refleje ambos. Cada paso está aislado (PE10).
        if (ReproductiveProfileProjector.PROFILE_EVENTS.contains(eventType)) {
            projectReproductiveProfiles(bovineId, eventType, item);
        }
        if (REPRODUCTIVE_TASK_EVENTS.contains(eventType)) {
            syncReproductiveTasks(farmId, bovineId, eventType);
            refreshSummary(bovineId, eventType);
        }

        return BovineEventResponseDTO.builder()
                .eventId(eventId)
                .eventType(eventType.name())
                .eventAt(eventAt.toString())
                .build();
    }

    /**
     * Aplica la proyección de salida (VENTA/MUERTE -> perfiles) de forma aislada: el evento
     * ya quedó persistido en el timeline, así que un fallo aquí no debe reportarse como error
     * de la operación (evitaría reintentos del cliente que dupliquen el evento). Se registra
     * para investigación; la reconciliación queda pendiente de diseño (riesgo conocido).
     */
    private void projectExitEvent(String bovineId, BovineEventType eventType, Instant eventAt) {
        try {
            exitEventProjector.project(bovineId, eventType, eventAt);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.PROCESSOR, "Fallo al proyectar evento de salida. bovineId: "
                    + bovineId + ", eventType: " + eventType, ex);
        }
    }

    /**
     * Actualiza la agenda reproductiva con el mismo aislamiento que {@link #projectExitEvent}:
     * el evento ya está guardado y la reconciliación diaria cubre un fallo aquí (HU-20260929, RT2).
     */
    private void syncReproductiveTasks(String farmId, String bovineId, BovineEventType eventType) {
        long start = System.nanoTime();
        try {
            reproductiveTaskSynchronizer.sync(farmId, bovineId);
            // TT3 (H6): duración real de la sincronización en línea, para medirla en CloudWatch.
            lambdaContext.logInfo(LogType.PROCESSOR, "Tareas reproductivas sincronizadas. bovineId: " + bovineId
                    + ", eventType: " + eventType + ", durationMs: " + (System.nanoTime() - start) / 1_000_000);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.PROCESSOR, "Fallo al sincronizar tareas reproductivas. bovineId: "
                    + bovineId + ", eventType: " + eventType, ex);
        }
    }

    /** HU-20260930 (PE1–PE5): preñez y lactancia desde el evento; aislado como la proyección de salida (PE10). */
    private void projectReproductiveProfiles(String bovineId, BovineEventType eventType, BovineEventItem item) {
        try {
            reproductiveProfileProjector.project(bovineId, item);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.PROCESSOR, "Fallo al proyectar perfiles reproductivos. bovineId: "
                    + bovineId + ", eventType: " + eventType, ex);
        }
    }

    /**
     * HU-20260930 (D3): regenera la tarjeta para que refleje el evento sin esperar al job de las
     * 03:00. Ids no numéricos (legacy) no tienen summary.
     */
    private void refreshSummary(String bovineId, BovineEventType eventType) {
        Integer numericId = parseNumericId(bovineId);
        if (numericId == null) {
            return;
        }
        try {
            bovineSummaryService.refreshSummary(numericId);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.PROCESSOR, "Fallo al refrescar el summary. bovineId: "
                    + bovineId + ", eventType: " + eventType, ex);
        }
    }

    private void validateIdentifiers(String farmId, String bovineId) {
        if (farmId == null || farmId.isBlank()) {
            throw new IllegalArgumentException("El campo farmId es requerido");
        }
        if (bovineId == null || bovineId.isBlank()) {
            throw new IllegalArgumentException("El campo bovineId es requerido");
        }
    }

    private void validateRequest(BovineEventRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("El body del evento es requerido");
        }
        if (request.getEventType() == null || request.getEventType().isBlank()) {
            throw new IllegalArgumentException("El campo eventType es requerido");
        }
    }

    /**
     * Guard: un bovino inexistente (id numérico sin identidad) o dado de baja
     * (`enabled == false` / {@code status} de baja) no admite nuevos eventos,
     * salvo el propio evento que causa la baja (MUERTE / VENTA).
     *
     * <p>Si el bovino no tiene perfil de ciclo de vida (dato legacy) el guard no
     * bloquea — no hay estado que comprobar.
     */
    private void enforceBovineIsRegistrable(String bovineId, BovineEventType eventType) {
        Integer numericId = parseNumericId(bovineId);
        if (numericId != null && bovineRepository.findById(numericId).isEmpty()) {
            throw new NotFoundException("Bovino no encontrado: " + bovineId);
        }

        if (EXIT_EVENTS.contains(eventType)) {
            return;
        }

        lifecycleRepository.findById("BOVINE#" + bovineId, LIFECYCLE_SK).ifPresent(lifecycle -> {
            boolean disabled = Boolean.FALSE.equals(lifecycle.getEnabled());
            boolean inactive = lifecycle.getStatus() != null && lifecycle.getStatus().isInactive();
            if (disabled || inactive) {
                String detail = lifecycle.getStatus() != null ? " (" + lifecycle.getStatus().name() + ")" : "";
                throw new IllegalArgumentException(
                        "El bovino " + bovineId + " está dado de baja" + detail
                                + "; no se pueden registrar nuevos eventos");
            }
        });
    }

    private Integer parseNumericId(String bovineId) {
        try {
            return Integer.valueOf(bovineId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Valida el payload contra el esquema del tipo de evento (EP-20260909, Fase 2).
     * Los 25 tipos de {@link BovineEventType} tienen esquema en {@code event-forms.yml};
     * {@link EventFormCatalog} garantiza esa cobertura al arrancar.
     */
    private void validatePayloadByType(BovineEventType eventType, Map<String, Object> payload) {
        EventFormSchema schema = eventFormCatalog.findBovineEvent(eventType.name())
                .orElseThrow(() -> new IllegalStateException(
                        "No hay esquema de formulario para el evento " + eventType.name()));
        payloadValidator.validate(schema, payload);
    }

    private String extractNotes(Map<String, Object> payload) {
        if (payload == null) return null;
        Object notes = payload.get("notes");
        if (notes == null || notes.toString().isBlank()) return null;
        return notes.toString().trim();
    }

    private Instant resolveEventAt(String eventAt) {
        if (eventAt == null || eventAt.isBlank()) return Instant.now();
        try {
            return LocalDate.parse(eventAt).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (Exception ex) {
            throw new IllegalArgumentException("El campo eventAt tiene un formato inválido. Se esperaba yyyy-MM-dd, recibido: '" + eventAt + "'");
        }
    }

    private String serializePayload(Map<String, Object> payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("No fue posible serializar el payload del evento bovino", ex);
        }
    }

    private String normalizeCreatedBy(String createdBy) {
        return createdBy == null || createdBy.isBlank() ? DEFAULT_CREATED_BY : createdBy.trim();
    }
}
