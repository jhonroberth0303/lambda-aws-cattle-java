package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.MilkingDTO;
import com.cattle.processor.MilkingProcessor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para MilkingController
 * Fase 7 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Controllers 35% → 75%
 * Tests: 8
 */
@Tag("unit")
@Tag("controller")
class MilkingRecordControllerTest {

    @Mock
    private MilkingProcessor milkingProcessor;

    @Mock
    private LambdaContext lambdaContext;

    private MilkingController milkingController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        milkingController = new MilkingController(milkingProcessor, lambdaContext);
        // Inyección manual ya que no usamos @InjectMocks
        try {
            java.lang.reflect.Field field = MilkingController.class.getDeclaredField("milkingProcessor");
            field.setAccessible(true);
            field.set(milkingController, milkingProcessor);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    // ==================== createMilking Tests ====================

    @Test
    void createMilking_validData_returnsOkWithCreated() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(1, "2026-01-20", "AM", 20.5);
        MilkingDTO savedDTO = createMilkingDTO(1, "2026-01-20", "AM", 20.5);
        String siteID = "FARM#001";
        
        when(milkingProcessor.createMilking(inputDTO)).thenReturn(Optional.of(savedDTO));

        // Act
        ResponseEntity<MilkingDTO> response = milkingController.createMilking(siteID, inputDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(20.5, response.getBody().getLiters());
        verify(milkingProcessor, times(1)).createMilking(inputDTO);
    }

    @Test
    void createMilking_processorFailure_returnsInternalServerError() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(1, "2026-01-20", "AM", 20.5);
        when(milkingProcessor.createMilking(inputDTO)).thenReturn(Optional.empty());
        String siteID = "FARM#001";

        // Act
        ResponseEntity<MilkingDTO> response = milkingController.createMilking(siteID, inputDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createMilking_invalidData_handledByExceptionHandler() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(null, "2026-01-20", "AM", 20.5);
        String siteID = "FARM#001";
        when(milkingProcessor.createMilking(inputDTO))
                .thenThrow(new IllegalArgumentException("bovineId es obligatorio"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> milkingController.createMilking(siteID, inputDTO));
        assertTrue(exception.getMessage().contains("bovineId"));
    }

    // ==================== milkingData Tests ====================

    @Test
    void milkingData_withResults_returnsOkWithList() {
        // Arrange
        Integer bovineId = 1;
        List<MilkingDTO> milkings = new ArrayList<>();
        milkings.add(createMilkingDTO(1, "2026-01-20", "AM", 20.5));
        milkings.add(createMilkingDTO(1, "2026-01-20", "PM", 18.0));
        String siteID = "FARM#001";
        
        when(milkingProcessor.getMilkingData(bovineId, null)).thenReturn(Optional.of(milkings));

        // Act
        ResponseEntity<List<MilkingDTO>> response = milkingController.milkingData(siteID, bovineId, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(milkingProcessor, times(1)).getMilkingData(bovineId, null);
    }

    @Test
    void milkingData_withShiftFilter_returnsFilteredResults() {
        // Arrange
        Integer bovineId = 1;
        String shift = "AM";
        List<MilkingDTO> milkings = new ArrayList<>();
        milkings.add(createMilkingDTO(1, "2026-01-20", "AM", 20.5));
        String siteID = "FARM#001";
        
        when(milkingProcessor.getMilkingData(bovineId, shift)).thenReturn(Optional.of(milkings));

        // Act
        ResponseEntity<List<MilkingDTO>> response = milkingController.milkingData(siteID, bovineId, shift);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("AM", response.getBody().get(0).getShift());
    }

    @Test
    void milkingData_noResults_returnsNotFound() {
        // Arrange
        Integer bovineId = 999;
        String siteID = "FARM#001";
        when(milkingProcessor.getMilkingData(bovineId, null)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<MilkingDTO>> response = milkingController.milkingData(siteID, bovineId, null);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void milkingData_emptyShiftParameter_treatedAsNull() {
        // Arrange
        Integer bovineId = 1;
        List<MilkingDTO> milkings = new ArrayList<>();
        milkings.add(createMilkingDTO(1, "2026-01-20", "AM", 20.5));
        String siteID = "FARM#001";
        
        when(milkingProcessor.getMilkingData(anyInt(), anyString())).thenReturn(Optional.of(milkings));

        // Act
        ResponseEntity<List<MilkingDTO>> response = milkingController.milkingData(siteID, bovineId, "");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(milkingProcessor, times(1)).getMilkingData(bovineId, "");
    }

    // ==================== handleIllegalArgument Tests ====================

    @Test
    void handleIllegalArgument_returnsBadRequestWithMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid bovineId");

        // Act
        ResponseEntity<String> response = milkingController.handleIllegalArgument(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid bovineId", response.getBody());
    }

    // ==================== Helper Methods ====================

    private MilkingDTO createMilkingDTO(Integer bovineId, String date, String shift, Double liters) {
        return MilkingDTO.builder()
                .bovineId(bovineId)
                .date(date)
                .shift(shift)
                .liters(liters)
                .status("completo")
                .build();
    }
}
