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

    private static final String FARM_ID = "F001"; // formato canónico (HU-20261120)

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
    // ==================== HU-20261120: la finca de la ruta es la fuente de verdad ====================

    @Test
    void save_setsFarmIdFromPath_overridingBody() {
        // CA1 / CA4
        BovineDTO inputDTO = createBovineDTO(null);
        inputDTO.setFarmId("OTRA-FINCA");
        when(bovineProcessor.save(any(BovineDTO.class))).thenReturn(Optional.of(createBovineDTO(1)));

        bovineController.save("F001", inputDTO);

        org.mockito.ArgumentCaptor<BovineDTO> captor = org.mockito.ArgumentCaptor.forClass(BovineDTO.class);
        verify(bovineProcessor).save(captor.capture());
        assertEquals("F001", captor.getValue().getFarmId());
    }

    @Test
    void save_bodyWithoutFarmId_getsFarmIdFromPath() {
        BovineDTO inputDTO = createBovineDTO(null);
        inputDTO.setFarmId(null);
        when(bovineProcessor.save(any(BovineDTO.class))).thenReturn(Optional.of(createBovineDTO(1)));

        bovineController.save("F001", inputDTO);

        org.mockito.ArgumentCaptor<BovineDTO> captor = org.mockito.ArgumentCaptor.forClass(BovineDTO.class);
        verify(bovineProcessor).save(captor.capture());
        assertEquals("F001", captor.getValue().getFarmId());
    }

    @Test
    void update_setsFarmIdFromPath_overridingBody() {
        // CA4
        BovineDTO dto = new BovineDTO();
        dto.setBovineId(9010);
        dto.setFarmId("FARM#001");
        when(bovineProcessor.update(any(BovineDTO.class))).thenReturn(Optional.of(dto));

        bovineController.update("F001", 9010L, dto);

        org.mockito.ArgumentCaptor<BovineDTO> captor = org.mockito.ArgumentCaptor.forClass(BovineDTO.class);
        verify(bovineProcessor).update(captor.capture());
        assertEquals("F001", captor.getValue().getFarmId());
    }

    // ==================== HU-20261120 (revisión H2): formato de finca validado ====================

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"FARM#001", "f001", "F01", "F0001", "001", "default", " "})
    void save_invalidFarmIdFormat_returnsBadRequestWithoutSaving(String invalidFarmId) {
        ResponseEntity<BovineDTO> response = bovineController.save(invalidFarmId, createBovineDTO(null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bovineProcessor, never()).save(any(BovineDTO.class));
    }

    @Test
    void save_nullFarmId_returnsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, bovineController.save(null, createBovineDTO(null)).getStatusCode());
        verify(bovineProcessor, never()).save(any(BovineDTO.class));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"FARM#001", "F1", "X001"})
    void update_invalidFarmIdFormat_returnsBadRequestWithoutUpdating(String invalidFarmId) {
        BovineDTO dto = new BovineDTO();
        dto.setBovineId(9011);

        ResponseEntity<BovineDTO> response = bovineController.update(invalidFarmId, 9011L, dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bovineProcessor, never()).update(any(BovineDTO.class));
    }

    @Test
    void isCanonicalFarmId_acceptsOnlyFPlusThreeDigits() {
        assertTrue(BovineController.isCanonicalFarmId("F001"));
        assertTrue(BovineController.isCanonicalFarmId("F123"));
        assertFalse(BovineController.isCanonicalFarmId("FARM#001"));
        assertFalse(BovineController.isCanonicalFarmId(null));
    }

    @Test
    void sanitizeForLog_removesLineBreaksAndTruncates() {
        assertEquals("F001_INFO_falso", BovineController.sanitizeForLog("F001\nINFO falso"));
        assertEquals("FARM#001", BovineController.sanitizeForLog("FARM#001"));
        assertEquals("null", BovineController.sanitizeForLog(null));
        assertEquals(21, BovineController.sanitizeForLog("A".repeat(50)).length());
    }

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
