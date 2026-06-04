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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
        doNothing().when(pastureService).applyPatch(eq(pasture.getPk()), any());
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
        verify(pastureService, times(1)).applyPatch(eq(pasture.getPk()), any());
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
        doNothing().when(pastureService).applyPatch(eq(pasture.getPk()), any());
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

    private Pasture createPasture(String id, String status) {
        return Pasture.builder()
                .pk("farm#F001#pasture#" + id)
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