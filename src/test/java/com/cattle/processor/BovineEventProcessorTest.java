package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineEventRequestDTO;
import com.cattle.dtos.BovineEventResponseDTO;
import com.cattle.enums.EventSource;
import com.cattle.enums.LogType;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.forms.EventPayloadValidator;
import com.cattle.services.BovineEventService;
import com.cattle.services.EventFormCatalog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BovineEventProcessor.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Processor
 */
@Tag("unit")
@Tag("fast")
@Tag("processor")
class BovineEventProcessorTest {

    @Mock
    private BovineEventService bovineEventService;

    @Mock
    private LambdaContext lambdaContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BovineEventProcessor processor;
    private EventFormCatalog eventFormCatalog;
    private final EventPayloadValidator payloadValidator = new EventPayloadValidator();

    @BeforeEach
    void setUp() {
        openMocks(this);
        eventFormCatalog = new EventFormCatalog(lambdaContext);
        processor = new BovineEventProcessor(bovineEventService, objectMapper, lambdaContext,
                eventFormCatalog, payloadValidator);
    }

    private BovineEventRequestDTO request(String type, Map<String, Object> payload) {
        BovineEventRequestDTO dto = new BovineEventRequestDTO();
        dto.setEventType(type);
        dto.setPayload(payload);
        return dto;
    }

    private BovineEventItem capturedItem() {
        ArgumentCaptor<BovineEventItem> captor = ArgumentCaptor.forClass(BovineEventItem.class);
        verify(bovineEventService).save(captor.capture());
        return captor.getValue();
    }

    // ==================== Happy path ====================

    @Test
    void applyEvent_seguimiento_buildsItemAndReturnsResponse() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notes", "  animal en observación  ");
        BovineEventRequestDTO req = request("SEGUIMIENTO", payload);
        req.setEventAt("2026-01-15");

        BovineEventResponseDTO response = processor.applyEvent("F001", "B-9", req);

        assertEquals("SEGUIMIENTO", response.getEventType());
        assertEquals("2026-01-15T00:00:00Z", response.getEventAt());
        assertNotNull(response.getEventId());

        BovineEventItem item = capturedItem();
        assertEquals("BOVINE#B-9", item.getPk());
        assertTrue(item.getSk().startsWith("EVT#2026-01-15T00:00:00Z#SEGUIMIENTO#"));
        assertTrue(item.getSk().endsWith(response.getEventId()));
        assertEquals("B-9", item.getBovineId());
        assertEquals("F001", item.getFarmId());
        assertEquals("SEGUIMIENTO", item.getEventType());
        assertEquals(EventSource.MANUAL, item.getSource());
        assertEquals("manual-web", item.getCreatedBy());
        assertEquals("animal en observación", item.getNotes());
        assertNotNull(item.getCreatedAt());
        assertNotNull(item.getUpdatedAt());
        assertTrue(item.getPayloadJson().contains("animal en observación"));
    }

    @Test
    void applyEvent_normalizesEventTypeCaseAndWhitespace() {
        BovineEventResponseDTO response = processor.applyEvent("F1", "B1",
                request("  observacion  ", Map.of("notes", "n")));

        assertEquals("OBSERVACION", response.getEventType());
    }

    @Test
    void applyEvent_usesProvidedCreatedByTrimmed() {
        BovineEventRequestDTO req = request("SEGUIMIENTO", Map.of("notes", "n"));
        req.setCreatedBy("  vet@farm.test  ");

        processor.applyEvent("F1", "B1", req);

        assertEquals("vet@farm.test", capturedItem().getCreatedBy());
    }

    @Test
    void applyEvent_nullEventAt_usesNowAndReturnsParseableInstant() {
        BovineEventResponseDTO response = processor.applyEvent("F1", "B1",
                request("SEGUIMIENTO", Map.of("notes", "n")));

        assertNotNull(response.getEventAt());
        assertDoesNotThrow(() -> Instant.parse(response.getEventAt()));
    }

    @Test
    void applyEvent_compraWithoutPayload_serializesNullPayloadAndNullNotes() {
        BovineEventResponseDTO response = processor.applyEvent("F1", "B1", request("COMPRA", null));

        assertEquals("COMPRA", response.getEventType());
        BovineEventItem item = capturedItem();
        assertNull(item.getPayloadJson());
        assertNull(item.getNotes());
    }

    @Test
    void applyEvent_inseminacionWithSemenBatch_isAccepted() {
        assertDoesNotThrow(() -> processor.applyEvent("F1", "B1",
                request("INSEMINACION", Map.of("semenBatch", "SB-22"))));
    }

    @Test
    void applyEvent_pesajeWithZeroWeight_isAcceptedBecauseNonNull() {
        assertDoesNotThrow(() -> processor.applyEvent("F1", "B1",
                request("PESAJE", Map.of("weightKg", 0))));
    }

    @Test
    void applyEvent_logsProcessorInfo() {
        processor.applyEvent("F1", "B1", request("SEGUIMIENTO", Map.of("notes", "n")));

        verify(lambdaContext).logInfo(eq(LogType.PROCESSOR), anyString());
    }

    // ==================== Identifier / request validation ====================

    @Test
    void applyEvent_blankFarmId_throwsAndNeverSaves() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("  ", "B1", request("SEGUIMIENTO", Map.of("notes", "n"))));

        assertEquals("El campo farmId es requerido", ex.getMessage());
        verify(bovineEventService, never()).save(any());
    }

    @Test
    void applyEvent_nullBovineId_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", null, request("SEGUIMIENTO", Map.of("notes", "n"))));

        assertEquals("El campo bovineId es requerido", ex.getMessage());
    }

    @Test
    void applyEvent_nullRequest_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", null));

        assertEquals("El body del evento es requerido", ex.getMessage());
    }

    @Test
    void applyEvent_blankEventType_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("   ", Map.of())));

        assertEquals("El campo eventType es requerido", ex.getMessage());
    }

    @Test
    void applyEvent_unknownEventType_throwsWithName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("NOPE", Map.of())));

        assertEquals("Tipo de evento bovino no soportado: NOPE", ex.getMessage());
    }

    @Test
    void applyEvent_invalidEventAtFormat_throws() {
        BovineEventRequestDTO req = request("SEGUIMIENTO", Map.of("notes", "n"));
        req.setEventAt("15/01/2026");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", req));

        assertTrue(ex.getMessage().contains("15/01/2026"));
        assertTrue(ex.getMessage().contains("yyyy-MM-dd"));
    }

    // ==================== Payload validation by type ====================

    @Test
    void applyEvent_seguimientoWithoutNotes_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("SEGUIMIENTO", Map.of())));

        assertEquals("El campo notes es requerido para este tipo de evento", ex.getMessage());
        verify(bovineEventService, never()).save(any());
    }

    @Test
    void applyEvent_seguimientoWithBlankNotes_throws() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notes", "   ");
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("SEGUIMIENTO", payload)));
    }

    @Test
    void applyEvent_vacunacionWithoutVaccineName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("VACUNACION", Map.of())));
    }

    @Test
    void applyEvent_tratamientoRequiresDiagnosisAndProduct() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("TRATAMIENTO", Map.of("diagnosis", "mastitis"))));
        assertDoesNotThrow(() -> processor.applyEvent("F1", "B1",
                request("TRATAMIENTO", Map.of("diagnosis", "mastitis", "product", "antibiotic"))));
    }

    @Test
    void applyEvent_pesajeWithoutWeight_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("PESAJE", Map.of())));
    }

    @Test
    void applyEvent_inseminacionWithoutBullIdOrSemenBatch_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("INSEMINACION", Map.of())));

        assertEquals("Se requiere bullId o semenBatch para el evento INSEMINACION", ex.getMessage());
    }

    @Test
    void applyEvent_ventaRequiresBuyerNameAndAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("VENTA", Map.of("buyerName", "Coop"))));
        assertDoesNotThrow(() -> processor.applyEvent("F1", "B1",
                request("VENTA", Map.of("buyerName", "Coop", "amountCOP", 1500000))));
    }

    @Test
    void applyEvent_muerteRequiresCause() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("MUERTE", Map.of())));
    }

    @Test
    void applyEvent_trasladoRequiresDestinationPaddock() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("TRASLADO", Map.of())));
    }

    @Test
    void applyEvent_partoRequiresCalfGenderAndBirthType() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("PARTO", Map.of())));
        // calfGender solo no basta: birthType también es requerido (schema-driven)
        assertThrows(IllegalArgumentException.class,
                () -> processor.applyEvent("F1", "B1", request("PARTO", Map.of("calfGender", "HEMBRA"))));
        assertDoesNotThrow(() -> processor.applyEvent("F1", "B1",
                request("PARTO", Map.of("calfGender", "HEMBRA", "birthType", "NORMAL"))));
    }

    @Test
    void applyEvent_partoRejectsUnknownBirthType() {
        assertThrows(IllegalArgumentException.class, () -> processor.applyEvent("F1", "B1",
                request("PARTO", Map.of("calfGender", "HEMBRA", "birthType", "RARO"))));
    }

    @Test
    void applyEvent_destete_hasNoExtraRequiredField() {
        assertDoesNotThrow(() -> processor.applyEvent("F1", "B1", request("DESTETE", Map.of())));
    }

    // ==================== Serialization failure ====================

    @Test
    void applyEvent_payloadSerializationFails_throwsIllegalArgument() throws JsonProcessingException {
        ObjectMapper failing = org.mockito.Mockito.mock(ObjectMapper.class);
        when(failing.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") {});
        BovineEventProcessor failingProcessor = new BovineEventProcessor(bovineEventService, failing,
                lambdaContext, eventFormCatalog, payloadValidator);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> failingProcessor.applyEvent("F1", "B1", request("SEGUIMIENTO", Map.of("notes", "n"))));

        assertTrue(ex.getMessage().contains("serializar"));
        verify(bovineEventService, never()).save(any());
    }
}
