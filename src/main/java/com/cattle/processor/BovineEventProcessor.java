package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineEventRequestDTO;
import com.cattle.dtos.BovineEventResponseDTO;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.EventSource;
import com.cattle.enums.LogType;
import com.cattle.services.BovineEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Component
public class BovineEventProcessor {

    private static final String DEFAULT_CREATED_BY = "manual-web";

    private final BovineEventService bovineEventService;
    private final ObjectMapper objectMapper;
    private final LambdaContext lambdaContext;

    public BovineEventProcessor(BovineEventService bovineEventService, ObjectMapper objectMapper, LambdaContext lambdaContext) {
        this.bovineEventService = bovineEventService;
        this.objectMapper = objectMapper;
        this.lambdaContext = lambdaContext;
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

    private void validatePayloadByType(BovineEventType eventType, Map<String, Object> payload) {
        switch (eventType) {
            case SEGUIMIENTO, OBSERVACION -> requireField(payload, "notes");
            case BANO_GARRAPATAS, DESPARASITACION -> requireField(payload, "product");
            case VACUNACION -> requireField(payload, "vaccineName");
            case TRATAMIENTO -> {
                requireField(payload, "diagnosis");
                requireField(payload, "product");
            }
            case PESAJE -> requireNonNull(payload, "weightKg");
            case MONTA -> requireField(payload, "bullBreed");
            case INSEMINACION -> {
                boolean hasBullId = hasValue(payload, "bullId");
                boolean hasSemenBatch = hasValue(payload, "semenBatch");
                if (!hasBullId && !hasSemenBatch) {
                    throw new IllegalArgumentException("Se requiere bullId o semenBatch para el evento INSEMINACION");
                }
            }
            case PARTO -> requireField(payload, "calfGender");
            case PRODUCCION_LECHE -> requireNonNull(payload, "liters");
            case CASTRACION -> requireField(payload, "method");
            case DESCORNE -> requireField(payload, "method");
            case MARCACION -> requireField(payload, "newTag");
            case TRASLADO -> requireField(payload, "destinationPaddockId");
            case VENTA -> {
                requireField(payload, "buyerName");
                requireNonNull(payload, "amountCOP");
            }
            case MUERTE -> requireField(payload, "cause");
            default -> { /* COMPRA, ABORTO, DESTETE, SECADO, DIAGNOSTICO_PRENEZ: sin campo obligatorio adicional */ }
        }
    }

    private void requireField(Map<String, Object> payload, String field) {
        Object v = payload != null ? payload.get(field) : null;
        if (v == null || v.toString().isBlank()) {
            throw new IllegalArgumentException("El campo " + field + " es requerido para este tipo de evento");
        }
    }

    private void requireNonNull(Map<String, Object> payload, String field) {
        Object v = payload != null ? payload.get(field) : null;
        if (v == null) {
            throw new IllegalArgumentException("El campo " + field + " es requerido para este tipo de evento");
        }
    }

    private boolean hasValue(Map<String, Object> payload, String field) {
        if (payload == null) return false;
        Object v = payload.get(field);
        return v != null && !v.toString().isBlank();
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
