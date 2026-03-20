package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.MilkingDTO;
import com.cattle.entities.MilkingRecord;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.mapper.MilkingMapperImpl;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.services.MilkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para MilkingProcessor
 * Fase 3 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: 6% → 75%
 * Tests: 10
 */
@Tag("unit")
@Tag("fast")
@Tag("processor")
class MilkingRecordProcessorTest {

    @Mock
    private MilkingService milkingService;

    @Mock
    private MilkingMapperImpl milkingMapper;

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private ProfileLactancyRepository profileLactancyRepository;

    private MilkingProcessor milkingProcessor;

    @BeforeEach
    void setUp() {
        openMocks(this);
        milkingProcessor = new MilkingProcessor(milkingService, milkingMapper, lambdaContext, profileLactancyRepository);
    }

    // ==================== getMilkingData Tests ====================

    @Test
    void getMilkingData_withoutFilter_returnsAllRecords() {
        // Arrange
        Integer bovineId = 1;
        String pk = "BOVINE#1";
        
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        milkingRecords.add(createFarmMilking(bovineId, "2026-01-20", "AM", 20.5));
        milkingRecords.add(createFarmMilking(bovineId, "2026-01-20", "PM", 18.0));

        MilkingDTO dto1 = createMilkingDTO(bovineId, "2026-01-20", "AM", 20.5);
        MilkingDTO dto2 = createMilkingDTO(bovineId, "2026-01-20", "PM", 18.0);

        when(milkingService.getMilkingByPk(pk)).thenReturn(Optional.of(milkingRecords));
        when(milkingMapper.toDTO(any(MilkingRecord.class))).thenReturn(dto1, dto2);

        // Act
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, null);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(milkingService, times(1)).getMilkingByPk(pk);
    }

    @Test
    void getMilkingData_withShiftFilter_returnsFilteredRecords() {
        // Arrange
        Integer bovineId = 1;
        String pk = "BOVINE#1";
        
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        MilkingRecord am = createFarmMilking(bovineId, "2026-01-20", "AM", 20.5);
        MilkingRecord pm = createFarmMilking(bovineId, "2026-01-20", "PM", 18.0);
        milkingRecords.add(am);
        milkingRecords.add(pm);

        MilkingDTO dtoAM = createMilkingDTO(bovineId, "2026-01-20", "AM", 20.5);

        when(milkingService.getMilkingByPk(pk)).thenReturn(Optional.of(milkingRecords));
        when(milkingMapper.toDTO(am)).thenReturn(dtoAM);

        // Act
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, "AM");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("AM", result.get().get(0).getShift());
    }

    @Test
    void getMilkingData_emptyResult_returnsEmpty() {
        // Arrange
        Integer bovineId = 999;
        String pk = "BOVINE#999";
        
        when(milkingService.getMilkingByPk(pk)).thenReturn(Optional.empty());

        // Act
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, null);

        // Assert
        assertTrue(result.isEmpty());
        verify(milkingService, times(1)).getMilkingByPk(pk);
    }

    @Test
    void getMilkingData_filterNoMatches_returnsEmpty() {
        // Arrange
        Integer bovineId = 1;
        String pk = "BOVINE#1";
        
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        milkingRecords.add(createFarmMilking(bovineId, "2026-01-20", "AM", 20.5));

        when(milkingService.getMilkingByPk(pk)).thenReturn(Optional.of(milkingRecords));

        // Act
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, "PM");

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== createMilking Tests ====================

    @Test
    void createMilking_validData_returnsCreatedDTO() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(1, "2026-01-20", "AM", 20.5);
        MilkingRecord entity = createFarmMilking(1, "2026-01-20", "AM", 20.5);
        MilkingRecord savedEntity = createFarmMilking(1, "2026-01-20", "AM", 20.5);
        savedEntity.setPK("BOVINE#1");
        savedEntity.setSK("MILKING#2026-01-20#AM");

        MilkingDTO outputDTO = createMilkingDTO(1, "2026-01-20", "AM", 20.5);

        ProfileLactancy openLactation = ProfileLactancy.builder()
                .pk("BOVINE#1")
                .sk("LACT#01")
                .lactationNumber("1")
                .status("LACTATING")
                .startDate("2025-11-27")
                .build();

        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        when(profileLactancyRepository.findAllLactationsByBovine("BOVINE#1"))
                .thenReturn(Optional.of(List.of(openLactation)));
        when(milkingService.save(any(MilkingRecord.class))).thenReturn(Optional.of(savedEntity));
        when(milkingMapper.toDTO(savedEntity)).thenReturn(outputDTO);

        // Act
        Optional<MilkingDTO> result = milkingProcessor.createMilking(inputDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(20.5, result.get().getLiters());
        verify(milkingMapper, times(1)).toEntity(inputDTO);
        verify(milkingService, times(1)).save(any(MilkingRecord.class));
    }

    @Test
    void createMilking_invalidBovineId_throwsException() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(null, "2026-01-20", "AM", 20.5);
        MilkingRecord entity = createFarmMilking(null, "2026-01-20", "AM", 20.5);

        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("bovineId"));
    }

    @Test
    void createMilking_nullDate_throwsException() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(1, null, "AM", 20.5);
        MilkingRecord entity = createFarmMilking(1, null, "AM", 20.5);

        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("date"));
    }

    @Test
    void createMilking_invalidDateFormat_throwsException() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(1, "20-01-2026", "AM", 20.5);
        MilkingRecord entity = createFarmMilking(1, "20-01-2026", "AM", 20.5);

        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("formato"));
    }

    @Test
    void createMilking_emptyShift_throwsException() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(1, "2026-01-20", "", 20.5);
        MilkingRecord entity = createFarmMilking(1, "2026-01-20", "", 20.5);

        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("shift"));
    }

    @Test
    void createMilking_setsPkSkCorrectly() {
        // Arrange
        MilkingDTO inputDTO = createMilkingDTO(42, "2026-01-20", "PM", 22.0);
        MilkingRecord entity = createFarmMilking(42, "2026-01-20", "PM", 22.0);
        MilkingRecord savedEntity = createFarmMilking(42, "2026-01-20", "PM", 22.0);
        savedEntity.setPK("BOVINE#42");
        savedEntity.setSK("MILKING#2026-01-20#PM");

        MilkingDTO outputDTO = createMilkingDTO(42, "2026-01-20", "PM", 22.0);

        ProfileLactancy openLactation = ProfileLactancy.builder()
                .pk("BOVINE#42")
                .sk("LACT#01")
                .lactationNumber("1")
                .status("LACTATING")
                .startDate("2025-11-27")
                .build();

        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        when(profileLactancyRepository.findAllLactationsByBovine("BOVINE#42"))
                .thenReturn(Optional.of(List.of(openLactation)));
        when(milkingService.save(any(MilkingRecord.class))).thenReturn(Optional.of(savedEntity));
        when(milkingMapper.toDTO(savedEntity)).thenReturn(outputDTO);

        // Act
        Optional<MilkingDTO> result = milkingProcessor.createMilking(inputDTO);

        // Assert
        assertTrue(result.isPresent());
        verify(milkingService).save(argThat(fm -> 
            "BOVINE#42".equals(fm.getPK()) && 
            fm.getSK() != null && 
            fm.getSK().startsWith("MILKING#")
        ));
    }

    // ==================== Helper Methods ====================

    private MilkingRecord createFarmMilking(Integer bovineId, String date, String shift, Double liters) {
        return MilkingRecord.builder()
                .bovineId(bovineId)
                .date(date)
                .shift(shift)
                .liters(liters)
                .status("completo")
                .build();
    }

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
