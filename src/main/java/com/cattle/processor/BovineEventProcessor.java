package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineEventRequestDTO;
import com.cattle.dtos.BovineEventResponseDTO;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.EventSource;
import com.cattle.enums.LogType;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.exceptions.NotFoundException;
import com.cattle.forms.EventFormSchema;
import com.cattle.forms.EventPayloadValidator;
import com.cattle.repository.BovineRepository;
import com.cattle.repository.ProfileLifecycleRepository;
import com.cattle.services.BovineEventService;
import com.cattle.services.EventFormCatalog;
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

    /** Estados del ciclo de vida que impiden registrar nuevos eventos. */
    private static final Set<LifecycleStatus> INACTIVE_STATUSES = EnumSet.of(
            LifecycleStatus.SOLD, LifecycleStatus.DEAD, LifecycleStatus.CULLED,
            LifecycleStatus.TRANSFERRED, LifecycleStatus.INACTIVE);

    private final BovineEventService bovineEventService;
    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;
    private final EventFormCatalog eventFormCatalog;
    private final EventPayloadValidator payloadValidator;
    private final BovineRepository bovineRepository;
    private final ProfileLifecycleRepository lifecycleRepository;

    public BovineEventProcessor(BovineEventService bovineEventService, ObjectMapper objectMapper,
                                LambdaContext lambdaContext, EventFormCatalog eventFormCatalog,
                                EventPayloadValidator payloadValidator, BovineRepository bovineRepository,
                                ProfileLifecycleRepository lifecycleRepository) {
        this.bovineEventService = bovineEventService;
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
        this.eventFormCatalog = eventFormCatalog;
        this.payloadValidator = payloadValidator;
        this.bovineRepository = bovineRepository;
        this.lifecycleRepository = lifecycleRepository;
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

        return BovineEventResponseDTO.builder()
                .eventId(eventId)
                .eventType(eventType.name())
                .eventAt(eventAt.toString())
                .build();
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
            boolean inactive = lifecycle.getStatus() != null && INACTIVE_STATUSES.contains(lifecycle.getStatus());
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
     * Los 22 tipos de {@link BovineEventType} tienen esquema en {@code event-forms.yml};
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
