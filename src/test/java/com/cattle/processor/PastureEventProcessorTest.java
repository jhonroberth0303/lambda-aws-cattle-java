package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureEventRequestDTO;
import com.cattle.dtos.PastureEventResponseDTO;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import com.cattle.enums.PastureStatus;
import com.cattle.enums.PastureSubstatus;
import com.cattle.events.EntityPatch;
import com.cattle.services.PastureEventService;
import com.cattle.services.PastureService;
import com.cattle.services.PlanService;
import com.cattle.utils.PastureStatusEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("processor")
class PastureEventProcessorTest {

    @Mock
    private PastureService pastureService;

    @Mock
    private PlanService planService;

    @Mock
    private PastureStatusEngine pastureStatusEngine;

    @Mock
    private PastureEventService pastureEventService;

    @Mock
    private RotationPlanProcessor rotationPlanProcessor;

    @Mock
    private LambdaContext lambdaContext;

    private PastureEventProcessor pastureEventProcessor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        pastureEventProcessor = new PastureEventProcessor(
                pastureService,
                planService,
                pastureStatusEngine,
                pastureEventService,
                rotationPlanProcessor,
                new ObjectMapper(),
                lambdaContext
        );
    }

    @Test
    void applyEvent_openEventPersistsAndReturnsUpdatedPasture() {
        String farmId = "F001";
        String pastureId = "P-01";
        Pasture pasture = createPasture(pastureId, "DISPONIBLE");
        Plan plan = createPlan("RYEGRASS");
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_USO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.applyEvent(eq(pasture), eq(plan), any())).thenReturn(patch);
        doNothing().when(pastureService).applyPatch(eq(pasture.getPk()), eq(pasture.getSk()), any());
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("EN_USO").build()
        )));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        request.setCreatedBy("operario@finca.test");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setLotId("L-001");
        payload.setAnimals(12);
        payload.setNotes("Ingreso controlado");
        request.setPayload(payload);

        PastureEventResponseDTO response = pastureEventProcessor.applyEvent(farmId, pastureId, request);

        assertEquals("OPEN", response.getEventType());
        assertEquals("EN_USO", response.getPasture().getStatus());
        verify(pastureService, times(1)).applyPatch(eq(pasture.getPk()), eq(pasture.getSk()), any());
        verify(pastureEventService, times(1)).save(any());
    }

    @Test
    void applyEvent_rejectsMissingRequiredField() {
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        request.setPayload(new PastureEventRequestDTO.Payload());

        assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-01", request));
    }

    @Test
    void applyEvent_maintenanceSetUsesSubstatusAndHoldUntil() {
        String farmId = "F001";
        String pastureId = "P-02";
        Pasture pasture = createPasture(pastureId, "EN_DESCANSO");
        Plan plan = createPlan("RYEGRASS");
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "MANTENIMIENTO");
        patch.set("substatus", PastureSubstatus.FERTILIZACION.name());

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.applyEvent(eq(pasture), eq(plan), any())).thenReturn(patch);
        doNothing().when(pastureService).applyPatch(eq(pasture.getPk()), eq(pasture.getSk()), any());
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("MANTENIMIENTO").substatus(PastureSubstatus.FERTILIZACION.name()).build()
        )));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("MAINTENANCE_SET");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setSubstatus("FERTILIZACION");
        payload.setHoldUntil("2026-06-30");
        request.setPayload(payload);

        PastureEventResponseDTO response = pastureEventProcessor.applyEvent(farmId, pastureId, request);

        assertEquals("MANTENIMIENTO", response.getPasture().getStatus());
        assertEquals(PastureSubstatus.FERTILIZACION.name(), response.getPasture().getSubstatus());
    }

    @Test
    void applyEvent_laborEventFertilized_persistsEventWithoutMutatingState() {
        String farmId = "F001";
        String pastureId = "P-03";
        Pasture pasture = createPasture(pastureId, "EN_DESCANSO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("EN_DESCANSO").build()
        )));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("FERTILIZED");
        request.setCreatedBy("operario@finca.test");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setProductName("Urea 46%");
        payload.setQuantityKg(50.0);
        payload.setNotes("Aplicación en todo el potrero");
        request.setPayload(payload);

        PastureEventResponseDTO response = pastureEventProcessor.applyEvent(farmId, pastureId, request);

        assertEquals("FERTILIZED", response.getEventType());
        assertEquals("EN_DESCANSO", response.getPasture().getStatus());
        verify(pastureService, never()).applyPatch(any(), any(), any());
        verify(pastureEventService, times(1)).save(any());
    }

    @Test
    void applyEvent_laborEventHeightMeasured_persistsEventWithoutMutatingState() {
        String farmId = "F001";
        String pastureId = "P-04";
        Pasture pasture = createPasture(pastureId, "EN_DESCANSO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("EN_DESCANSO").build()
        )));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("HEIGHT_MEASURED");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setHeightCm(22);
        payload.setNotes("Medición en punto central");
        request.setPayload(payload);

        PastureEventResponseDTO response = pastureEventProcessor.applyEvent(farmId, pastureId, request);

        assertEquals("HEIGHT_MEASURED", response.getEventType());
        verify(pastureService, never()).applyPatch(any(), any(), any());
        verify(pastureEventService, times(1)).save(any());
    }

    @Test
    void applyEvent_observationAdded_persistsEventWithoutMutatingState() {
        String farmId = "F001";
        String pastureId = "P-05";
        Pasture pasture = createPasture(pastureId, "DISPONIBLE");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("DISPONIBLE").build()
        )));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OBSERVATION_ADDED");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setNotes("Se detectó presencia de kikuyo en el borde norte");
        request.setPayload(payload);

        PastureEventResponseDTO response = pastureEventProcessor.applyEvent(farmId, pastureId, request);

        assertEquals("OBSERVATION_ADDED", response.getEventType());
        verify(pastureService, never()).applyPatch(any(), any(), any());
        verify(pastureEventService, times(1)).save(any());
    }

    @Test
    void applyEvent_closeEvent_updatesCurrentHeightCmToResidualCm() {
        String farmId = "F001";
        String pastureId = "P-04";
        Pasture pasture = createPasture(pastureId, "EN_USO");
        Plan plan = createPlan("RYEGRASS");
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_DESCANSO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(plan)));
        when(pastureStatusEngine.applyEvent(eq(pasture), eq(plan), any())).thenReturn(patch);
        doNothing().when(pastureService).applyPatch(eq(pasture.getPk()), eq(pasture.getSk()), any());
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("EN_DESCANSO").build()
        )));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("CLOSE");
        request.setCreatedBy("operario@finca.test");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setLotId("L01");
        payload.setAnimals(3);
        payload.setResidualCm(8);
        request.setPayload(payload);

        ArgumentCaptor<EntityPatch> patchCaptor = ArgumentCaptor.forClass(EntityPatch.class);

        PastureEventResponseDTO response = pastureEventProcessor.applyEvent(farmId, pastureId, request);

        verify(pastureService).applyPatch(eq(pasture.getPk()), eq(pasture.getSk()), patchCaptor.capture());
        EntityPatch appliedPatch = patchCaptor.getValue();

        assertEquals("CLOSE", response.getEventType());
        assertEquals("EN_DESCANSO", response.getPasture().getStatus());
        assertEquals(8, appliedPatch.set().get("currentHeightCm"));
        assertEquals(true, appliedPatch.set().containsKey("lastUseAt"));
        assertEquals(true, appliedPatch.remove().contains("blockReason"));
    }

    @Test
    void applyEvent_closeEvent_rejectsNullResidualCm() {
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("CLOSE");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setResidualCm(null);
        request.setPayload(payload);

        assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-04", request));
    }

    @Test
    void applyEvent_nullRequest_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-01", null));
        assertEquals("El body del evento es requerido", ex.getMessage());
    }

    @Test
    void applyEvent_blankFarmId_throws() {
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("  ", "P-01", request));
        assertEquals("El campo farmId es requerido", ex.getMessage());
    }

    @Test
    void applyEvent_blankPastureId_throws() {
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", " ", request));
        assertEquals("El campo pastureId es requerido", ex.getMessage());
    }

    @Test
    void applyEvent_blankEventType_throws() {
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("   ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-01", request));
        assertEquals("El campo eventType es requerido", ex.getMessage());
    }

    @Test
    void applyEvent_unknownEventType_throwsWithName() {
        when(pastureService.getPastures("F001")).thenReturn(Optional.of(List.of(createPasture("P-01", "DISPONIBLE"))));
        when(planService.getPlans("F001")).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("TELEPORT");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-01", request));
        assertEquals("Tipo de evento no soportado: TELEPORT", ex.getMessage());
    }

    @Test
    void applyEvent_pastureNotFound_throws() {
        when(pastureService.getPastures("F001")).thenReturn(Optional.of(List.of()));
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-404", request));
        assertEquals("No se encontró el potrero P-404 en la finca F001", ex.getMessage());
    }

    @Test
    void applyEvent_maintenanceSetInvalidSubstatus_throws() {
        when(pastureService.getPastures("F001")).thenReturn(Optional.of(List.of(createPasture("P-01", "EN_DESCANSO"))));
        when(planService.getPlans("F001")).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("MAINTENANCE_SET");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setSubstatus("NO_EXISTE");
        request.setPayload(payload);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-01", request));
        assertEquals("Subestado no soportado: NO_EXISTE", ex.getMessage());
    }

    @Test
    void applyEvent_maintenanceClear_removesBlockReason() {
        String farmId = "F001";
        String pastureId = "P-06";
        Pasture pasture = createPasture(pastureId, "MANTENIMIENTO");
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "EN_DESCANSO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        when(pastureStatusEngine.applyEvent(eq(pasture), any(), any())).thenReturn(patch);
        doNothing().when(pastureService).applyPatch(any(), any(), any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("EN_DESCANSO").build())));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("MAINTENANCE_CLEAR");
        request.setPayload(new PastureEventRequestDTO.Payload());

        ArgumentCaptor<EntityPatch> captor = ArgumentCaptor.forClass(EntityPatch.class);
        pastureEventProcessor.applyEvent(farmId, pastureId, request);

        verify(pastureService).applyPatch(any(), any(), captor.capture());
        assertEquals(true, captor.getValue().remove().contains("blockReason"));
    }

    @Test
    void applyEvent_preEntryCheck_serializesCheckSummaryIntoPatch() {
        String farmId = "F001";
        String pastureId = "P-07";
        Pasture pasture = createPasture(pastureId, "EN_DESCANSO");
        EntityPatch patch = EntityPatch.of();
        patch.set("status", "DISPONIBLE");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        when(pastureStatusEngine.applyEvent(eq(pasture), any(), any())).thenReturn(patch);
        doNothing().when(pastureService).applyPatch(any(), any(), any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("DISPONIBLE").build())));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("PRE_ENTRY_CHECK");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setAllCriticalOk(true);
        payload.setPerformedBy("tecnico@finca.test");
        request.setPayload(payload);

        ArgumentCaptor<EntityPatch> captor = ArgumentCaptor.forClass(EntityPatch.class);
        pastureEventProcessor.applyEvent(farmId, pastureId, request);

        verify(pastureService).applyPatch(any(), any(), captor.capture());
        Object json = captor.getValue().set().get("lastPreEntryCheckJson");
        assertEquals(true, json instanceof String && ((String) json).contains("allCriticalOk"));
    }

    @Test
    void applyEvent_statusEngineRejects_wrappedAsIllegalArgument() {
        String farmId = "F001";
        String pastureId = "P-08";
        Pasture pasture = createPasture(pastureId, "EN_USO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        when(pastureStatusEngine.applyEvent(eq(pasture), any(), any()))
                .thenThrow(new IllegalStateException("transición inválida"));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setLotId("L1");
        payload.setAnimals(5);
        request.setPayload(payload);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent(farmId, pastureId, request));
        assertEquals("transición inválida", ex.getMessage());
        verify(pastureService, never()).applyPatch(any(), any(), any());
    }

    @Test
    void applyEvent_invalidEventAt_fallsBackToNowAndLogs() {
        String farmId = "F001";
        String pastureId = "P-09";
        Pasture pasture = createPasture(pastureId, "EN_DESCANSO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of(
                RotationSemaphoreItemDTO.builder().pastureId(pastureId).status("EN_DESCANSO").build())));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("FERTILIZED");
        request.setEventAt("no-es-fecha");
        request.setPayload(new PastureEventRequestDTO.Payload());

        PastureEventResponseDTO response = pastureEventProcessor.applyEvent(farmId, pastureId, request);

        assertEquals("FERTILIZED", response.getEventType());
        verify(lambdaContext, org.mockito.Mockito.atLeastOnce())
                .logInfo(eq(com.cattle.enums.LogType.PROCESSOR), org.mockito.ArgumentMatchers.contains("no-es-fecha"));
    }

    @Test
    void applyEvent_laborEventRotationItemMissing_throws() {
        String farmId = "F001";
        String pastureId = "P-10";
        Pasture pasture = createPasture(pastureId, "EN_DESCANSO");

        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(List.of(pasture)));
        when(planService.getPlans(farmId)).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        doNothing().when(pastureEventService).save(any());
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(List.of()));

        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("LIMED");
        request.setPayload(new PastureEventRequestDTO.Payload());

        assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent(farmId, pastureId, request));
    }

    @Test
    void applyEvent_openMissingLotId_throws() {
        when(pastureService.getPastures("F001")).thenReturn(Optional.of(List.of(createPasture("P-01", "DISPONIBLE"))));
        when(planService.getPlans("F001")).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setAnimals(5);
        request.setPayload(payload);

        assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-01", request));
    }

    @Test
    void applyEvent_openNonPositiveAnimals_throws() {
        when(pastureService.getPastures("F001")).thenReturn(Optional.of(List.of(createPasture("P-01", "DISPONIBLE"))));
        when(planService.getPlans("F001")).thenReturn(Optional.of(List.of(createPlan("RYEGRASS"))));
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setLotId("L1");
        payload.setAnimals(0);
        request.setPayload(payload);

        assertThrows(IllegalArgumentException.class,
                () -> pastureEventProcessor.applyEvent("F001", "P-01", request));
    }

    private Pasture createPasture(String id, String status) {
        return Pasture.builder()
                .pk("farm#F001")
                .sk("pasture#" + id)
                .farmId("F001")
                .id(id)
                .name("Potrero " + id)
                .species("RYEGRASS")
                .status(status)
                .substatus(PastureStatus.NINGUNO.name())
                .build();
    }

    private Plan createPlan(String species) {
        return Plan.builder()
                .farmId("F001")
                .species(species)
                .rules(Plan.Rules.builder().entryHeightCm(30).restDaysMin(21).exitResidualCm(8).build())
                .build();
    }
}