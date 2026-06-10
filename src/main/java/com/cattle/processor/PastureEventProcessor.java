package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureEventRequestDTO;
import com.cattle.dtos.PastureEventResponseDTO;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import com.cattle.enums.EventSource;
import com.cattle.enums.EventType;
import com.cattle.enums.LogType;
import com.cattle.enums.PastureSubstatus;
import com.cattle.events.CloseEvent;
import com.cattle.events.EntityPatch;
import com.cattle.events.LaborEvent;
import com.cattle.events.MaintenanceClearEvent;
import com.cattle.events.MaintenanceSetEvent;
import com.cattle.events.OpenEvent;
import com.cattle.events.PreEntryCheckEvent;
import com.cattle.events.PastureEvent;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.services.PastureEventService;
import com.cattle.services.PastureService;
import com.cattle.services.PlanService;
import com.cattle.utils.PastureStatusEngine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.cattle.events.PatchApplier.applyLocal;

@Component
public class PastureEventProcessor {
    private static final String DEFAULT_CREATED_BY = "manual-web";

    private final PastureService pastureService;
    private final PlanService planService;
    private final PastureStatusEngine pastureStatusEngine;
    private final PastureEventService pastureEventService;
    private final RotationPlanProcessor rotationPlanProcessor;
    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;

    public PastureEventProcessor(
            PastureService pastureService,
            PlanService planService,
            PastureStatusEngine pastureStatusEngine,
            PastureEventService pastureEventService,
            RotationPlanProcessor rotationPlanProcessor,
            ObjectMapper objectMapper, LambdaContext lambdaContext
    ) {
        this.pastureService = pastureService;
        this.planService = planService;
        this.pastureStatusEngine = pastureStatusEngine;
        this.pastureEventService = pastureEventService;
        this.rotationPlanProcessor = rotationPlanProcessor;
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
    }

    public PastureEventResponseDTO applyEvent(String farmId, String pastureId, PastureEventRequestDTO request) {

        lambdaContext.logInfo(LogType.PROCESSOR, "Aplicando evento al potrero. farmId: " + farmId + ", pastureId: " + pastureId + ", eventType: " + (request != null ? request.getEventType() : "null"));
        validateIdentifiers(farmId, pastureId);
        validateRequest(request);

        Pasture pasture = pastureService.getPastures(farmId)
                .orElse(List.of())
                .stream()
                .filter(item -> pastureId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el potrero " + pastureId + " en la finca " + farmId));

        Plan plan = planService.getPlans(farmId)
                .orElse(List.of())
                .stream()
                .filter(item -> pasture.getSpecies() != null && pasture.getSpecies().equals(item.getSpecies()))
                .findFirst()
                .orElse(null);

        PastureEvent event = toDomainEvent(request);

        if (event instanceof LaborEvent) {
            String eventId = persistEvent(farmId, pastureId, request, event);
            RotationSemaphoreItemDTO currentPasture = rotationPlanProcessor.getRotationSemaphoreItems(farmId)
                    .orElse(List.of())
                    .stream()
                    .filter(item -> pastureId.equals(item.getPastureId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el potrero " + pastureId + " en la finca " + farmId));
            return PastureEventResponseDTO.builder()
                    .eventId(eventId)
                    .eventType(event.type().name())
                    .pasture(currentPasture)
                    .build();
        }

        EntityPatch patch;
        try {
            patch = pastureStatusEngine.applyEvent(pasture, plan, event);
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        enrichPatchForOperationalFields(request, event, patch);

        pastureService.applyPatch(pasture.getPk(), pasture.getSk(), patch);
        applyLocal(pasture, patch);

        String eventId = persistEvent(farmId, pastureId, request, event);
        RotationSemaphoreItemDTO updatedPasture = rotationPlanProcessor.getRotationSemaphoreItems(farmId)
                .orElse(List.of())
                .stream()
                .filter(item -> pastureId.equals(item.getPastureId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se pudo reconstruir la lectura actualizada del potrero " + pastureId));

        return PastureEventResponseDTO.builder()
                .eventId(eventId)
                .eventType(event.type().name())
                .pasture(updatedPasture)
                .build();
    }

    private void validateIdentifiers(String farmId, String pastureId) {
        if (farmId == null || farmId.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo farmId es requerido");
        }
        if (pastureId == null || pastureId.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo pastureId es requerido");
        }
    }

    private void validateRequest(PastureEventRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("El body del evento es requerido");
        }
        if (request.getEventType() == null || request.getEventType().isBlank()) {
            throw new IllegalArgumentException("El campo eventType es requerido");
        }
    }

    private PastureEvent toDomainEvent(PastureEventRequestDTO request) {
        EventType eventType;
        try {
            eventType = EventType.valueOf(request.getEventType().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de evento no soportado: " + request.getEventType(), ex);
        }

        PastureEventRequestDTO.Payload payload = request.getPayload() != null ? request.getPayload() : new PastureEventRequestDTO.Payload();
        String createdBy = normalizeCreatedBy(request.getCreatedBy());

        return switch (eventType) {
            case OPEN -> {
                requireNonBlank(payload.getLotId(), "lotId");
                requirePositive(payload.getAnimals(), "animals");
                yield new OpenEvent(createdBy, payload.getLotId().trim(), payload.getAnimals());
            }
            case CLOSE -> {
                requirePositive(payload.getResidualCm(), "residualCm");
                yield new CloseEvent(createdBy, blankToNull(payload.getLotId()), payload.getAnimals(), payload.getResidualCm());
            }
            case MAINTENANCE_SET -> {
                requireNonBlank(payload.getSubstatus(), "substatus");
                PastureSubstatus substatus;
                try {
                    substatus = PastureSubstatus.valueOf(payload.getSubstatus().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Subestado no soportado: " + payload.getSubstatus(), ex);
                }
                yield new MaintenanceSetEvent(createdBy, substatus, blankToNull(payload.getHoldUntil()));
            }
            case MAINTENANCE_CLEAR -> new MaintenanceClearEvent(createdBy);
            case PRE_ENTRY_CHECK -> new PreEntryCheckEvent(
                    createdBy,
                    Boolean.TRUE.equals(payload.getAllCriticalOk()),
                    payload.getCompletedAt()
            );
            case FERTILIZED, LIMED -> new LaborEvent(eventType, createdBy);
            case HEIGHT_MEASURED -> {
                requirePositive(payload.getHeightCm(), "heightCm");
                yield new LaborEvent(eventType, createdBy);
            }
            case OBSERVATION_ADDED -> {
                requireNonBlank(payload.getNotes(), "notes");
                yield new LaborEvent(eventType, createdBy);
            }
        };
    }

    private void enrichPatchForOperationalFields(PastureEventRequestDTO request, PastureEvent event, EntityPatch patch) {
        PastureEventRequestDTO.Payload payload = request.getPayload() != null ? request.getPayload() : new PastureEventRequestDTO.Payload();

        if (event.type() == EventType.CLOSE) {
            patch.remove("blockReason");
            patch.set("lastUseAt", LocalDate.now(ZoneOffset.UTC).toString());
        }

        if (event.type() == EventType.MAINTENANCE_SET) {
            patch.set("blockReason", payload.getSubstatus().trim().toUpperCase());
            if (payload.getNotes() != null && !payload.getNotes().isBlank()) {
                patch.set("notes", payload.getNotes().trim());
            }
        }

        if (event.type() == EventType.MAINTENANCE_CLEAR) {
            patch.remove("blockReason");
        }

        if (event.type() == EventType.OPEN && payload.getNotes() != null && !payload.getNotes().isBlank()) {
            patch.set("notes", payload.getNotes().trim());
        }

        if (event.type() == EventType.CLOSE && payload.getNotes() != null && !payload.getNotes().isBlank()) {
            patch.set("notes", payload.getNotes().trim());
        }

        if (event.type() == EventType.PRE_ENTRY_CHECK) {
            try {
                String checkJson = objectMapper.writeValueAsString(buildCheckSummary(request));
                patch.set("lastPreEntryCheckJson", checkJson);
            } catch (JsonProcessingException ex) {
                lambdaContext.logInfo(LogType.PROCESSOR, "No fue posible serializar lastPreEntryCheck: " + ex.getMessage());
            }
        }
    }

    private Map<String, Object> buildCheckSummary(PastureEventRequestDTO request) {
        PastureEventRequestDTO.Payload p = request.getPayload() != null ? request.getPayload() : new PastureEventRequestDTO.Payload();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("completedAt", p.getCompletedAt());
        summary.put("performedBy", blankToNull(p.getPerformedBy()));
        summary.put("allCriticalOk", p.getAllCriticalOk());
        summary.put("items", p.getItems());
        return summary;
    }

    private String persistEvent(String farmId, String pastureId, PastureEventRequestDTO request, PastureEvent event) {
        String eventId = UUID.randomUUID().toString();
        Instant eventInstant = resolveEventAt(request.getEventAt());
        Instant now = Instant.now();
        PastureEventRequestDTO.Payload payload = request.getPayload() != null ? request.getPayload() : new PastureEventRequestDTO.Payload();
        String payloadJson = serializePayload(payload);

        PastureEventItem item = new PastureEventItem();
        item.setPk("PASTURE#" + pastureId);
        item.setSk("EVT#" + eventInstant.toString() + "#" + event.type().name() + "#" + eventId);
        item.setPastureId(pastureId);
        item.setFarmId(farmId);
        item.setEventId(eventId);
        item.setEventType(event.type().name());
        item.setEventAt(eventInstant);
        item.setSource(EventSource.MANUAL);
        item.setCreatedBy(normalizeCreatedBy(request.getCreatedBy()));
        item.setPayloadJson(payloadJson);
        item.setNotes(blankToNull(payload.getNotes()));
        item.setCreatedAt(now.toString());
        item.setUpdatedAt(now.toString());
        item.setGsi1pk("farm#" + farmId + "#type#" + event.type().name());
        item.setGsi1sk(eventInstant.toString());

        pastureEventService.save(item);
        return eventId;
    }

    private Instant resolveEventAt(String eventAt) {
        if (eventAt == null || eventAt.isBlank()) return Instant.now();
        try {
            return LocalDate.parse(eventAt).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (Exception ex) {
            lambdaContext.logInfo(LogType.PROCESSOR, "eventAt inválido '" + eventAt + "', usando Instant.now()");
            return Instant.now();
        }
    }

    private String serializePayload(PastureEventRequestDTO.Payload payload) {
        try {
            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("lotId", blankToNull(payload.getLotId()));
            payloadMap.put("animals", payload.getAnimals());
            payloadMap.put("residualCm", payload.getResidualCm());
            payloadMap.put("substatus", blankToNull(payload.getSubstatus()));
            payloadMap.put("holdUntil", blankToNull(payload.getHoldUntil()));
            payloadMap.put("notes", blankToNull(payload.getNotes()));
            payloadMap.put("completedAt", payload.getCompletedAt());
            payloadMap.put("allCriticalOk", payload.getAllCriticalOk());
            payloadMap.put("performedBy", blankToNull(payload.getPerformedBy()));
            payloadMap.put("items", payload.getItems());
            payloadMap.put("heightCm", payload.getHeightCm());
            payloadMap.put("productName", blankToNull(payload.getProductName()));
            payloadMap.put("quantityKg", payload.getQuantityKg());
            return objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("No fue posible serializar el payload del evento", ex);
        }
    }

    private void requireNonBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + field + " es requerido");
        }
    }

    private void requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("El campo " + field + " debe ser mayor que cero");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String normalizeCreatedBy(String createdBy) {
        return createdBy == null || createdBy.trim().isEmpty() ? DEFAULT_CREATED_BY : createdBy.trim();
    }
}