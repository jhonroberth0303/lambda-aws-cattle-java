package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineDTO;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.ServiceException;
import com.cattle.mapper.BovinesMapperImpl;
import com.cattle.services.BovineService;
import com.cattle.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BovinesProcessor
 * Fase 3 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: 6% → 80%
 * Tests: 8
 */
@Tag("unit")
@Tag("fast")
@Tag("processor")
class BovineIdentityItemProcessorTest {

    @Mock
    private BovineService bovineService;

    @Mock
    private BovinesMapperImpl bovinesMapper;

    @Mock
    private LambdaContext lambdaContext;

    private BovineProcessor bovineProcessor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        bovineProcessor = new BovineProcessor(bovineService, bovinesMapper, lambdaContext);
    }

    // ==================== findAll Tests ====================

    @Test
    void findAll_withBovines_returnsDTOList() {
        // Arrange
        List<BovineIdentityItem> bovineIdentityItems = TestDataBuilder.createBovineList("farm-001", 3);
        BovineDTO dto1 = BovineDTO.builder().bovineId(1).name("Bovine-1").build();
        BovineDTO dto2 = BovineDTO.builder().bovineId(2).name("Bovine-2").build();
        BovineDTO dto3 = BovineDTO.builder().bovineId(3).name("Bovine-3").build();

        when(bovineService.findAll()).thenReturn(Optional.of(bovineIdentityItems));
        when(bovinesMapper.toDTO(any(BovineIdentityItem.class)))
                .thenReturn(dto1, dto2, dto3);

        // Act
        List<BovineDTO> result = bovineProcessor.findAll();

        // Assert
        assertEquals(3, result.size());
        verify(bovineService, times(1)).findAll();
        verify(bovinesMapper, times(3)).toDTO(any(BovineIdentityItem.class));
    }

    @Test
    void findAll_emptyList_returnsEmptyList() {
        // Arrange
        when(bovineService.findAll()).thenReturn(Optional.empty());

        // Act
        List<BovineDTO> result = bovineProcessor.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(bovineService, times(1)).findAll();
    }

    @Test
    void findAll_serviceException_throwsProcessingException() {
        // Arrange
        when(bovineService.findAll()).thenThrow(new ServiceException("Service error"));

        // Act & Assert
        assertThrows(ProcessingException.class, () -> bovineProcessor.findAll());
        verify(bovineService, times(1)).findAll();
    }

    // ==================== findById Tests ====================

    @Test
    void findById_existingId_returnsDTO() {
        // Arrange
        Integer id = 123;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(id);
        BovineDTO dto = BovineDTO.builder().bovineId(id).name("Test Cow").build();

        when(bovineService.findById(id)).thenReturn(Optional.of(bovineIdentityItem));
        when(bovinesMapper.toDTO(bovineIdentityItem)).thenReturn(dto);

        // Act
        Optional<BovineDTO> result = bovineProcessor.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getBovineId());
        verify(bovineService, times(1)).findById(id);
        verify(bovinesMapper, times(1)).toDTO(bovineIdentityItem);
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        // Arrange
        Integer id = 999;
        when(bovineService.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<BovineDTO> result = bovineProcessor.findById(id);

        // Assert
        assertTrue(result.isEmpty());
        verify(bovineService, times(1)).findById(id);
        verify(bovinesMapper, never()).toDTO(any());
    }

    @Test
    void findById_serviceException_throwsProcessingException() {
        // Arrange
        Integer id = 123;
        when(bovineService.findById(id)).thenThrow(new ServiceException("Service error"));

        // Act & Assert
        assertThrows(ProcessingException.class, () -> bovineProcessor.findById(id));
        verify(bovineService, times(1)).findById(id);
    }

    // ==================== save Tests ====================

    @Test
    void save_validDTO_returnsSavedDTO() {
        // Arrange
        BovineDTO inputDTO = BovineDTO.builder()
                .name("New Cow")
                .gender("female")
                .breed("Holstein")
                .build();

        BovineIdentityItem entity = TestDataBuilder.createBovine("farm-001", "cow");
        BovineIdentityItem savedEntity = TestDataBuilder.createBovine("farm-001", "cow");
        savedEntity.setBovineId(100);

        BovineDTO outputDTO = BovineDTO.builder()
                .bovineId(100)
                .name("New Cow")
                .build();

        when(bovinesMapper.toEntity(inputDTO)).thenReturn(entity);
        when(bovineService.save(entity)).thenReturn(Optional.of(savedEntity));
        when(bovinesMapper.toDTO(savedEntity)).thenReturn(outputDTO);

        // Act
        Optional<BovineDTO> result = bovineProcessor.save(inputDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getBovineId());
        verify(bovinesMapper, times(1)).toEntity(inputDTO);
        verify(bovineService, times(1)).save(entity);
        verify(bovinesMapper, times(1)).toDTO(savedEntity);
    }

    @Test
    void save_serviceException_throwsProcessingException() {
        // Arrange
        BovineDTO inputDTO = BovineDTO.builder().name("Test").build();
        BovineIdentityItem entity = TestDataBuilder.createBovine("farm-001", "cow");

        when(bovinesMapper.toEntity(inputDTO)).thenReturn(entity);
        when(bovineService.save(entity)).thenThrow(new ServiceException("Save error"));

        // Act & Assert
        assertThrows(ProcessingException.class, () -> bovineProcessor.save(inputDTO));
        verify(bovineService, times(1)).save(entity);
    }

    // ==================== update Tests ====================

    @Test
    void update_validDTO_returnsUpdatedDTO() {
        // Arrange
        BovineDTO inputDTO = BovineDTO.builder()
                .bovineId(123)
                .name("Updated Cow")
                .build();

        BovineIdentityItem entity = TestDataBuilder.createBovine("farm-001", "cow");
        entity.setBovineId(123);

        BovineIdentityItem updatedEntity = TestDataBuilder.createBovine("farm-001", "cow");
        updatedEntity.setBovineId(123);
        updatedEntity.setName("Updated Cow");

        BovineDTO outputDTO = BovineDTO.builder()
                .bovineId(123)
                .name("Updated Cow")
                .build();

        when(bovinesMapper.toEntity(inputDTO)).thenReturn(entity);
        when(bovineService.update(entity)).thenReturn(Optional.of(updatedEntity));
        when(bovinesMapper.toDTO(updatedEntity)).thenReturn(outputDTO);

        // Act
        Optional<BovineDTO> result = bovineProcessor.update(inputDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(123, result.get().getBovineId());
        assertEquals("Updated Cow", result.get().getName());
        verify(bovinesMapper, times(1)).toEntity(inputDTO);
        verify(bovineService, times(1)).update(entity);
        verify(bovinesMapper, times(1)).toDTO(updatedEntity);
    }

    @Test
    void update_serviceException_throwsProcessingException() {
        // Arrange
        BovineDTO inputDTO = BovineDTO.builder().bovineId(123).name("Test").build();
        BovineIdentityItem entity = TestDataBuilder.createBovine("farm-001", "cow");

        when(bovinesMapper.toEntity(inputDTO)).thenReturn(entity);
        when(bovineService.update(entity)).thenThrow(new ServiceException("Update error"));

        // Act & Assert
        assertThrows(ProcessingException.class, () -> bovineProcessor.update(inputDTO));
        verify(bovineService, times(1)).update(entity);
    }
}
