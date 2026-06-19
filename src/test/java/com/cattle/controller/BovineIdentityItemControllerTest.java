package com.cattle.controller;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineDTO;
import com.cattle.processor.BovineEventProcessor;
import com.cattle.processor.BovineProcessor;
import com.cattle.services.BovineEventService;
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
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class BovineIdentityItemControllerTest {

    private static final String FARM_ID = "FARM#TEST";

    @Mock
    private BovineProcessor bovineProcessor;

    @Mock
    private BovineEventProcessor bovineEventProcessor;

    @Mock
    private BovineEventService bovineEventService;

    @Mock
    private LambdaContext lambdaContext;

    private BovineController bovineController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        bovineController = new BovineController(bovineProcessor, bovineEventProcessor, bovineEventService, lambdaContext);
    }

    // ==================== getAll Tests ====================

    @Test
    void getAll_withBovines_returnsOkWithList() {
        // Arrange
        List<BovineDTO> bovines = createBovineDTOList(5);
        when(bovineProcessor.findAll()).thenReturn(bovines);

        // Act
        ResponseEntity<List<BovineDTO>> response = bovineController.getAll(FARM_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().size());
    }

    @Test
    void getAll_emptyList_returnsNoContent() {
        // Arrange
        when(bovineProcessor.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<BovineDTO>> response = bovineController.getAll(FARM_ID);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void getAll_nullList_returnsNoContent() {
        // Arrange
        when(bovineProcessor.findAll()).thenReturn(null);

        // Act
        ResponseEntity<List<BovineDTO>> response = bovineController.getAll(FARM_ID);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // ==================== findById Tests ====================

    @Test
    void findById_existingId_returnsOk() {
        // Arrange
        Integer id = 123;
        BovineDTO bovineDTO = createBovineDTO(id);
        when(bovineProcessor.findById(id)).thenReturn(Optional.of(bovineDTO));

        // Act
        ResponseEntity<BovineDTO> response = bovineController.findById(FARM_ID, id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().getBovineId());
    }

    @Test
    void findById_nonExistingId_returnsNotFound() {
        // Arrange
        Integer id = 999;
        when(bovineProcessor.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<BovineDTO> response = bovineController.findById(FARM_ID, id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void findById_nullId_returnsBadRequest() {
        // Act
        ResponseEntity<BovineDTO> response = bovineController.findById(FARM_ID, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void findById_negativeId_returnsBadRequest() {
        // Act
        ResponseEntity<BovineDTO> response = bovineController.findById(FARM_ID, -1);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void findById_zeroId_returnsBadRequest() {
        // Act
        ResponseEntity<BovineDTO> response = bovineController.findById(FARM_ID, 0);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== save Tests ====================

    @Test
    void save_validBovine_returnsOk() {
        // Arrange
        BovineDTO inputDTO = createBovineDTO(null);
        BovineDTO savedDTO = createBovineDTO(1);
        when(bovineProcessor.save(any(BovineDTO.class))).thenReturn(Optional.of(savedDTO));

        // Act
        ResponseEntity<BovineDTO> response = bovineController.save(FARM_ID, inputDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getBovineId());
    }

    @Test
    void save_validBovine_callsProcessor() {
        // Arrange - @Valid es manejado por Spring MVC antes de llegar al controller
        // Este test verifica que un DTO válido llega correctamente al processor
        BovineDTO inputDTO = createBovineDTO(null);
        BovineDTO savedDTO = createBovineDTO(1);
        when(bovineProcessor.save(any(BovineDTO.class))).thenReturn(Optional.of(savedDTO));

        // Act
        bovineController.save(FARM_ID, inputDTO);

        // Assert
        verify(bovineProcessor, times(1)).save(any(BovineDTO.class));
    }

    @Test
    void save_processorReturnsEmpty_returnsInternalServerError() {
        // Arrange
        BovineDTO inputDTO = createBovineDTO(null);
        when(bovineProcessor.save(any(BovineDTO.class))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<BovineDTO> response = bovineController.save(FARM_ID, inputDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ==================== update Tests ====================

    @Test
    void update_shouldReturnBadRequest_whenInvalidData() {

        BovineDTO invalidDto = new BovineDTO();
        invalidDto.setBovineId(2);

        ResponseEntity<BovineDTO> response = bovineController.update(FARM_ID, 1L, invalidDto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void update_shouldReturnOk_whenValidData() {

        BovineDTO validDto = new BovineDTO();
        validDto.setBovineId(1);

        when(bovineProcessor.update(any(BovineDTO.class))).thenReturn(Optional.of(validDto));

        ResponseEntity<BovineDTO> response = bovineController.update(FARM_ID, 1L, validDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validDto, response.getBody());
    }

    @Test
    void update_shouldReturnInternalServerError_whenProcessorFails() {

        BovineDTO validDto = new BovineDTO();
        validDto.setBovineId(1);

        when(bovineProcessor.update(any(BovineDTO.class))).thenReturn(Optional.empty());

        ResponseEntity<BovineDTO> response = bovineController.update(FARM_ID, 1L, validDto);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void update_validBovine_callsProcessor() {
        // Arrange - @Valid es manejado por Spring MVC antes de llegar al controller
        // Este test verifica que un DTO válido con ID matching llega correctamente al processor
        BovineDTO validDto = createBovineDTO(123);
        when(bovineProcessor.update(any(BovineDTO.class))).thenReturn(Optional.of(validDto));

        // Act
        bovineController.update(FARM_ID, 123L, validDto);

        // Assert
        verify(bovineProcessor, times(1)).update(any(BovineDTO.class));
    }

    @Test
    void update_zeroIdInBody_returnsBadRequest() {
        // Arrange
        BovineDTO inputDTO = createBovineDTO(0);

        // Act
        ResponseEntity<BovineDTO> response = bovineController.update(FARM_ID, 0L, inputDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== Summary Endpoints Tests ====================
    // Las pruebas de endpoints de resumen han sido migradas a BovinesSummaryControllerTest.
    // ==================== Helper Methods ====================

    private BovineDTO createBovineDTO(Integer id) {
        return BovineDTO.builder()
                .bovineId(id)
                .name("Test Bovine " + id)
                .gender("female")
                .breed("Holstein")
                .bornDate("2020-01-01")
                .farmId("farm-001")
                .build();
    }

    private List<BovineDTO> createBovineDTOList(int count) {
        List<BovineDTO> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(createBovineDTO(i));
        }
        return list;
    }
}
