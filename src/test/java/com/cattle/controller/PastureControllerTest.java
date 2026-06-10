package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureEventHistoryItemDTO;
import com.cattle.dtos.PastureEventRequestDTO;
import com.cattle.dtos.PastureEventResponseDTO;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.processor.PastureEventProcessor;
import com.cattle.processor.RotationPlanProcessor;
import com.cattle.services.PastureEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para PasturesController
 * Fase 7 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Controllers 35% → 75%
 * Tests: 6
 */
@Tag("unit")
@Tag("controller")
class PastureControllerTest {

    @Mock
    private RotationPlanProcessor rotationPlanProcessor;

    @Mock
    private PastureEventProcessor pastureEventProcessor;

    @Mock
    private PastureEventService pastureEventService;

    @Mock
    private LambdaContext lambdaContext;

    private PastureController pastureController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        pastureController = new PastureController(rotationPlanProcessor, pastureEventProcessor, pastureEventService, lambdaContext);
    }

    // ==================== getRotationSemaphore Tests ====================

    @Test
    void getRotationSemaphore_withResults_returnsOkWithList() {
        // Arrange
        String farmId = "farm-001";
        List<RotationSemaphoreItemDTO> items = new ArrayList<>();
        items.add(createRotationItem("pasture-1", "DISPONIBLE"));
        items.add(createRotationItem("pasture-2", "EN_DESCANSO"));
        
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(items));

        // Act
        ResponseEntity<List<RotationSemaphoreItemDTO>> response = 
                pastureController.getRotationSemaphore(farmId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(rotationPlanProcessor, times(1)).getRotationSemaphoreItems(farmId);
    }

    @Test
    void getRotationSemaphore_emptyResults_returnsOkWithEmptyList() {
        // Arrange
        String farmId = "farm-empty";
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<RotationSemaphoreItemDTO>> response = 
                pastureController.getRotationSemaphore(farmId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getRotationSemaphore_validFarmId_callsProcessor() {
        // Arrange
        String farmId = "farm-123";
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId))
                .thenReturn(Optional.of(new ArrayList<>()));

        // Act
        pastureController.getRotationSemaphore(farmId);

        // Assert
        verify(rotationPlanProcessor, times(1)).getRotationSemaphoreItems(farmId);
    }

    @Test
    void getRotationSemaphore_multiplePastures_returnsAllInOrder() {
        // Arrange
        String farmId = "farm-001";
        List<RotationSemaphoreItemDTO> items = new ArrayList<>();
        items.add(createRotationItem("pasture-1", "DISPONIBLE"));
        items.add(createRotationItem("pasture-2", "EN_DESCANSO"));
        items.add(createRotationItem("pasture-3", "EN_USO"));
        items.add(createRotationItem("pasture-4", "MANTENIMIENTO"));
        
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId)).thenReturn(Optional.of(items));

        // Act
        ResponseEntity<List<RotationSemaphoreItemDTO>> response = 
                pastureController.getRotationSemaphore(farmId);

        // Assert
        assertEquals(4, response.getBody().size());
        assertEquals("pasture-1", response.getBody().get(0).getId());
        assertEquals("pasture-4", response.getBody().get(3).getId());
    }

    @Test
    void getRotationSemaphore_differentFarmIds_processesCorrectly() {
        // Arrange
        String farmId1 = "farm-001";
        String farmId2 = "farm-002";
        
        List<RotationSemaphoreItemDTO> items1 = List.of(createRotationItem("p1", "DISPONIBLE"));
        List<RotationSemaphoreItemDTO> items2 = List.of(createRotationItem("p2", "EN_USO"));
        
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId1)).thenReturn(Optional.of(items1));
        when(rotationPlanProcessor.getRotationSemaphoreItems(farmId2)).thenReturn(Optional.of(items2));

        // Act
        ResponseEntity<List<RotationSemaphoreItemDTO>> response1 = 
                pastureController.getRotationSemaphore(farmId1);
        ResponseEntity<List<RotationSemaphoreItemDTO>> response2 = 
                pastureController.getRotationSemaphore(farmId2);

        // Assert
        assertEquals("p1", response1.getBody().get(0).getId());
        assertEquals("p2", response2.getBody().get(0).getId());
        verify(rotationPlanProcessor, times(1)).getRotationSemaphoreItems(farmId1);
        verify(rotationPlanProcessor, times(1)).getRotationSemaphoreItems(farmId2);
    }

    @Test
    void getRotationSemaphore_processorReturnsEmptyOptional_returnsEmptyList() {
        // Arrange
        String farmId = "farm-999";
        when(rotationPlanProcessor.getRotationSemaphoreItems(anyString()))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<RotationSemaphoreItemDTO>> response = 
                pastureController.getRotationSemaphore(farmId);

        // Assert
        assertTrue(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void applyEvent_delegatesToProcessorAndReturnsOk() {
        String farmId = "farm-001";
        String pastureId = "P-01";
        PastureEventRequestDTO request = new PastureEventRequestDTO();
        request.setEventType("OPEN");
        PastureEventRequestDTO.Payload payload = new PastureEventRequestDTO.Payload();
        payload.setLotId("L-001");
        payload.setAnimals(10);
        request.setPayload(payload);

        PastureEventResponseDTO responseDTO = PastureEventResponseDTO.builder()
                .eventId("evt-1")
                .eventType("OPEN")
                .pasture(createRotationItem("P-01", "EN_USO"))
                .build();

        when(pastureEventProcessor.applyEvent(farmId, pastureId, request)).thenReturn(responseDTO);

        ResponseEntity<PastureEventResponseDTO> response = pastureController.applyEvent(farmId, pastureId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("evt-1", response.getBody().getEventId());
        verify(pastureEventProcessor).applyEvent(farmId, pastureId, request);
    }

    @Test
    void getEventHistory_returnsOkWithList() {
        String farmId = "farm-001";
        String pastureId = "P-01";
        List<PastureEventHistoryItemDTO> history = List.of(
                PastureEventHistoryItemDTO.builder().eventId("e1").eventType("OPEN").createdBy("op1").build(),
                PastureEventHistoryItemDTO.builder().eventId("e2").eventType("CLOSE").createdBy("op1").build()
        );
        when(pastureEventService.findByPasture(farmId, pastureId, 20)).thenReturn(history);

        ResponseEntity<List<PastureEventHistoryItemDTO>> response = pastureController.getEventHistory(farmId, pastureId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("OPEN", response.getBody().get(0).getEventType());
        verify(pastureEventService).findByPasture(farmId, pastureId, 20);
    }

    @Test
    void getEventHistory_emptyHistory_returnsOkWithEmptyList() {
        when(pastureEventService.findByPasture(anyString(), anyString(), eq(20))).thenReturn(List.of());

        ResponseEntity<List<PastureEventHistoryItemDTO>> response = pastureController.getEventHistory("farm-001", "P-99");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ==================== Helper Methods ====================

    private RotationSemaphoreItemDTO createRotationItem(String id, String status) {
        return RotationSemaphoreItemDTO.builder()
                .id(id)
                .name("Potrero " + id)
                .status(status)
                .build();
    }
}
