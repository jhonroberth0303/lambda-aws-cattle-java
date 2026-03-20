package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.entities.Pasture;
import com.cattle.enums.LogType;
import com.cattle.exceptions.ServiceException;
import com.cattle.mapper.PasturesMapper;
import com.cattle.services.PastureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para PastureProcessor
 * Fase 11 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Processors 48% → 75%
 * Tests: 6
 */
@Tag("unit")
@Tag("processor")
class PastureProcessorTest {

    @Mock
    private PastureService pastureService;

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private PasturesMapper pasturesMapper;

    private PastureProcessor pastureProcessor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        pastureProcessor = new PastureProcessor(pastureService, lambdaContext, pasturesMapper);
    }

    // ==================== listPastures Tests ====================

    @Test
    void listPastures_withPastures_returnsEmptyList() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = List.of(
                createPasture("P-01"),
                createPasture("P-02")
        );
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = pastureProcessor.listPastures(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty()); // Current implementation returns empty list
        verify(pastureService, times(1)).getPastures(farmId);
        verify(lambdaContext, times(1)).logInfo(eq(LogType.PROCESSOR), contains("farm#"));
        verify(lambdaContext, times(1)).logInfo(eq(LogType.PROCESSOR), eq("success"));
    }

    @Test
    void listPastures_noPastures_returnsEmpty() throws ServiceException {
        // Arrange
        String farmId = "farm-empty";
        when(pastureService.getPastures(farmId)).thenReturn(Optional.empty());

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = pastureProcessor.listPastures(farmId);

        // Assert
        assertFalse(result.isPresent());
        verify(pastureService, times(1)).getPastures(farmId);
    }

    @Test
    void listPastures_emptyList_returnsEmpty() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(Collections.emptyList()));

        // Act
        Optional<List<RotationSemaphoreItemDTO>> result = pastureProcessor.listPastures(farmId);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void listPastures_serviceException_throwsProcessingException() throws ServiceException {
        // Arrange
        String farmId = "farm-error";
        when(pastureService.getPastures(farmId)).thenThrow(new ServiceException("Service error"));

        // Act & Assert
        assertThrows(com.cattle.exceptions.ProcessingException.class, 
                () -> pastureProcessor.listPastures(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.PROCESSOR), contains("Failed to list"));
    }

    @Test
    void listPastures_unexpectedException_throwsProcessingException() throws ServiceException {
        // Arrange
        String farmId = "farm-error";
        when(pastureService.getPastures(farmId)).thenThrow(new RuntimeException("Unexpected"));

        // Act & Assert
        assertThrows(com.cattle.exceptions.ProcessingException.class, 
                () -> pastureProcessor.listPastures(farmId));
        verify(lambdaContext, times(1)).logException(eq(LogType.PROCESSOR), contains("Unexpected error"));
    }

    @Test
    void listPastures_callsMapperWithPastures() throws ServiceException {
        // Arrange
        String farmId = "farm-001";
        List<Pasture> pastures = List.of(createPasture("P-01"));
        
        when(pastureService.getPastures(farmId)).thenReturn(Optional.of(pastures));

        // Act
        pastureProcessor.listPastures(farmId);

        // Assert
        verify(pasturesMapper, times(1)).toDTOList(pastures);
    }

    // ==================== Helper Methods ====================

    private Pasture createPasture(String id) {
        return Pasture.builder()
                .pk("farm#F001#pasture#" + id)
                .farmId("F001")
                .id(id)
                .name("Potrero " + id)
                .status("DISPONIBLE")
                .species("RYEGRASS")
                .build();
    }
}
