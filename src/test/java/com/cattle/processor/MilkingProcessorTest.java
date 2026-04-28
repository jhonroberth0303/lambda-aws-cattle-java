package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.CowWithLactationsDTO;
import com.cattle.dtos.LactationSummaryDTO;
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

@Tag("unit")
@Tag("fast")
@Tag("processor")
class MilkingProcessorTest {

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
        Integer bovineId = 1;
        String pk = "BOVINE#1";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        milkingRecords.add(createFarmMilking(bovineId, "2026-01-20", "AM", 20.5));
        milkingRecords.add(createFarmMilking(bovineId, "2026-01-20", "PM", 18.0));
        MilkingDTO dto1 = createMilkingDTO(bovineId, "2026-01-20", "AM", 20.5);
        MilkingDTO dto2 = createMilkingDTO(bovineId, "2026-01-20", "PM", 18.0);
        when(milkingService.getMilkingByPk(pk)).thenReturn(Optional.of(milkingRecords));
        when(milkingMapper.toDTO(any(MilkingRecord.class))).thenReturn(dto1, dto2);
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, null);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(milkingService, times(1)).getMilkingByPk(pk);
    }

    @Test
    void getMilkingData_withShiftFilter_returnsFilteredRecords() {
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
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, "AM");
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("AM", result.get().get(0).getShift());
    }

    @Test
    void getMilkingData_emptyResult_returnsEmpty() {
        Integer bovineId = 999;
        String pk = "BOVINE#999";
        when(milkingService.getMilkingByPk(pk)).thenReturn(Optional.empty());
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, null);
        assertTrue(result.isEmpty());
        verify(milkingService, times(1)).getMilkingByPk(pk);
    }

    @Test
    void getMilkingData_filterNoMatches_returnsEmpty() {
        Integer bovineId = 1;
        String pk = "BOVINE#1";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        milkingRecords.add(createFarmMilking(bovineId, "2026-01-20", "AM", 20.5));
        when(milkingService.getMilkingByPk(pk)).thenReturn(Optional.of(milkingRecords));
        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingData(bovineId, "PM");
        assertTrue(result.isEmpty());
    }

    // ==================== createMilking Tests ====================

    @Test
    void createMilking_validData_returnsCreatedDTO() {
        MilkingDTO inputDTO = createMilkingDTO(1, "2026-01-20", "AM", 20.5);
        MilkingRecord entity = createFarmMilking(1, "2026-01-20", "AM", 20.5);
        MilkingRecord savedEntity = createFarmMilking(1, "2026-01-20", "AM", 20.5);
        savedEntity.setPK("BOVINE#1");
        savedEntity.setSK("MILKING#2026-01-20#AM");
        MilkingDTO outputDTO = createMilkingDTO(1, "2026-01-20", "AM", 20.5);
        ProfileLactancy openLactation = ProfileLactancy.builder()
                .pk("BOVINE#1")
                .sk("LACT#001")
                .lactationNumber("1")
                .status("LACTATING")
                .startDate("2025-11-27")
                .build();
        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        when(profileLactancyRepository.findAllLactationsByBovine("BOVINE#1"))
                .thenReturn(Optional.of(List.of(openLactation)));
        when(milkingService.save(any(MilkingRecord.class))).thenReturn(Optional.of(savedEntity));
        when(milkingMapper.toDTO(savedEntity)).thenReturn(outputDTO);
        Optional<MilkingDTO> result = milkingProcessor.createMilking(inputDTO);
        assertTrue(result.isPresent());
        assertEquals(20.5, result.get().getLiters());
        verify(milkingMapper, times(1)).toEntity(inputDTO);
        verify(milkingService, times(1)).save(any(MilkingRecord.class));
    }

    @Test
    void createMilking_invalidBovineId_throwsException() {
        MilkingDTO inputDTO = createMilkingDTO(null, "2026-01-20", "AM", 20.5);
        MilkingRecord entity = createFarmMilking(null, "2026-01-20", "AM", 20.5);
        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("bovineId"));
    }

    @Test
    void createMilking_nullDate_throwsException() {
        MilkingDTO inputDTO = createMilkingDTO(1, null, "AM", 20.5);
        MilkingRecord entity = createFarmMilking(1, null, "AM", 20.5);
        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("date"));
    }

    @Test
    void createMilking_invalidDateFormat_throwsException() {
        MilkingDTO inputDTO = createMilkingDTO(1, "20-01-2026", "AM", 20.5);
        MilkingRecord entity = createFarmMilking(1, "20-01-2026", "AM", 20.5);
        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("formato"));
    }

    @Test
    void createMilking_emptyShift_throwsException() {
        MilkingDTO inputDTO = createMilkingDTO(1, "2026-01-20", "", 20.5);
        MilkingRecord entity = createFarmMilking(1, "2026-01-20", "", 20.5);
        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.createMilking(inputDTO));
        assertTrue(exception.getMessage().contains("shift"));
    }

    @Test
    void createMilking_setsPkSkCorrectly() {
        MilkingDTO inputDTO = createMilkingDTO(42, "2026-01-20", "PM", 22.0);
        MilkingRecord entity = createFarmMilking(42, "2026-01-20", "PM", 22.0);
        MilkingRecord savedEntity = createFarmMilking(42, "2026-01-20", "PM", 22.0);
        savedEntity.setPK("BOVINE#42");
        savedEntity.setSK("MILKING#2026-01-20#PM");
        MilkingDTO outputDTO = createMilkingDTO(42, "2026-01-20", "PM", 22.0);
        ProfileLactancy openLactation = ProfileLactancy.builder()
                .pk("BOVINE#42")
                .sk("LACT#001")
                .lactationNumber("1")
                .status("LACTATING")
                .startDate("2025-11-27")
                .build();
        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        when(profileLactancyRepository.findAllLactationsByBovine("BOVINE#42"))
                .thenReturn(Optional.of(List.of(openLactation)));
        when(milkingService.save(any(MilkingRecord.class))).thenReturn(Optional.of(savedEntity));
        when(milkingMapper.toDTO(savedEntity)).thenReturn(outputDTO);
        Optional<MilkingDTO> result = milkingProcessor.createMilking(inputDTO);
        assertTrue(result.isPresent());
        verify(milkingService).save(argThat(fm -> 
            "BOVINE#42".equals(fm.getPK()) && 
            fm.getSK() != null && 
            fm.getSK().startsWith("MILKING#")
        ));
    }

    @Test
    void createMilking_formatsGsi2pkWithThreeDigits() {
        MilkingDTO inputDTO = createMilkingDTO(167, "2026-04-25", "AM", 3.0);
        MilkingRecord entity = createFarmMilking(167, "2026-04-25", "AM", 3.0);
        MilkingRecord savedEntity = createFarmMilking(167, "2026-04-25", "AM", 3.0);
        MilkingDTO outputDTO = createMilkingDTO(167, "2026-04-25", "AM", 3.0);
        ProfileLactancy openLactation = ProfileLactancy.builder()
                .pk("BOVINE#167")
                .sk("LACT#002")
                .lactationNumber("2")
                .status("LACTATING")
                .startDate("2026-01-01")
                .build();
        when(milkingMapper.toEntity(inputDTO)).thenReturn(entity);
        when(profileLactancyRepository.findAllLactationsByBovine("BOVINE#167"))
                .thenReturn(Optional.of(List.of(openLactation)));
        when(milkingService.save(any(MilkingRecord.class))).thenReturn(Optional.of(savedEntity));
        when(milkingMapper.toDTO(savedEntity)).thenReturn(outputDTO);

        Optional<MilkingDTO> result = milkingProcessor.createMilking(inputDTO);

        assertTrue(result.isPresent());
        verify(milkingService).save(argThat(fm ->
                "BOVINE#167#LACT#002".equals(fm.getGsi2pk())
                        && "2026-04-25#AM".equals(fm.getGsi2sk())
                        && Integer.valueOf(2).equals(fm.getLactationNumber())
        ));
    }

        @Test
        void getCowsWithLactations_groupsByBovineAndSortsLactations() {
        String siteId = "FARM#001";
        ProfileLactancy lactation2 = ProfileLactancy.builder()
            .pk("BOVINE#172")
            .sk("LACT#002")
            .lactationNumber("2")
            .status("LACTATING")
            .startDate("2026-01-10")
            .build();
        ProfileLactancy lactation1 = ProfileLactancy.builder()
            .pk("BOVINE#172")
            .sk("LACT#001")
            .lactationNumber("1")
            .status("CLOSED")
            .startDate("2025-01-10")
            .endDate("2025-10-10")
            .build();
        ProfileLactancy otherBovine = ProfileLactancy.builder()
            .pk("BOVINE#200")
            .sk("LACT#001")
            .lactationNumber("1")
            .status("LACTATING")
            .startDate("2026-02-01")
            .build();

        when(profileLactancyRepository.findAllLactations(siteId))
            .thenReturn(Optional.of(List.of(lactation2, lactation1, otherBovine)));

        Optional<List<CowWithLactationsDTO>> result = milkingProcessor.getCowsWithLactations(siteId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());

        CowWithLactationsDTO cow172 = result.get().stream()
            .filter(cow -> Integer.valueOf(172).equals(cow.getBovineId()))
            .findFirst()
            .orElseThrow();

        assertEquals(2, cow172.getLactations().size());
        assertEquals("001", cow172.getLactations().get(0).getLactationNumber());
        assertEquals("002", cow172.getLactations().get(1).getLactationNumber());
        verify(profileLactancyRepository).findAllLactations(siteId);
        }

        @Test
        void getCowsWithLactations_withoutRecords_returnsEmpty() {
        String siteId = "FARM#001";
        when(profileLactancyRepository.findAllLactations(siteId)).thenReturn(Optional.empty());

        Optional<List<CowWithLactationsDTO>> result = milkingProcessor.getCowsWithLactations(siteId);

        assertTrue(result.isEmpty());
        verify(profileLactancyRepository).findAllLactations(siteId);
        }

        @Test
        void getCowsWithLactations_ignoresInvalidPkEntries() {
        String siteId = "FARM#001";
        ProfileLactancy invalidPk = ProfileLactancy.builder()
            .pk("INVALID#172")
            .sk("LACT#001")
            .status("LACTATING")
            .build();

        when(profileLactancyRepository.findAllLactations(siteId)).thenReturn(Optional.of(List.of(invalidPk)));

        Optional<List<CowWithLactationsDTO>> result = milkingProcessor.getCowsWithLactations(siteId);

        assertTrue(result.isEmpty());
        verify(profileLactancyRepository).findAllLactations(siteId);
        }

    @Test
    void getMilkingByLactation_normalizesInputToThreeDigits() {
        Integer bovineId = 167;
        MilkingRecord record = createFarmMilking(bovineId, "2026-04-25", "AM", 3.0);
        MilkingDTO dto = createMilkingDTO(bovineId, "2026-04-25", "AM", 3.0);
        when(milkingService.getMilkingByBovineAndLactation(bovineId, "002"))
                .thenReturn(Optional.of(List.of(record)));
        when(milkingMapper.toDTO(record)).thenReturn(dto);

        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingByLactation(bovineId, "02", null);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        verify(milkingService).getMilkingByBovineAndLactation(bovineId, "002");
    }

    @Test
    void getMilkingByLactation_invalidLactationNumber_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> milkingProcessor.getMilkingByLactation(167, "LACT#02", null));

        assertTrue(exception.getMessage().contains("lactancia"));
        verify(milkingService, never()).getMilkingByBovineAndLactation(anyInt(), anyString());
    }

    @Test
    void getMilkingByLactation_withShiftFilter_returnsOnlyMatchingRecords() {
        Integer bovineId = 167;
        MilkingRecord amRecord = createFarmMilking(bovineId, "2026-04-25", "AM", 3.0);
        MilkingRecord pmRecord = createFarmMilking(bovineId, "2026-04-25", "PM", 2.5);
        MilkingDTO amDto = createMilkingDTO(bovineId, "2026-04-25", "AM", 3.0);

        when(milkingService.getMilkingByBovineAndLactation(bovineId, "002"))
                .thenReturn(Optional.of(List.of(amRecord, pmRecord)));
        when(milkingMapper.toDTO(amRecord)).thenReturn(amDto);

        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingByLactation(bovineId, "2", "AM");

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("AM", result.get().get(0).getShift());
        verify(milkingService).getMilkingByBovineAndLactation(bovineId, "002");
        verify(milkingMapper, never()).toDTO(pmRecord);
    }

    @Test
    void getMilkingByLactation_whenFilterRemovesAllRecords_returnsEmpty() {
        Integer bovineId = 167;
        MilkingRecord amRecord = createFarmMilking(bovineId, "2026-04-25", "AM", 3.0);

        when(milkingService.getMilkingByBovineAndLactation(bovineId, "002"))
                .thenReturn(Optional.of(List.of(amRecord)));

        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingByLactation(bovineId, "2", "PM");

        assertTrue(result.isEmpty());
        verify(milkingMapper, never()).toDTO(any(MilkingRecord.class));
    }

    @Test
    void getMilkingByLactation_whenServiceReturnsEmpty_returnsEmpty() {
        Integer bovineId = 167;

        when(milkingService.getMilkingByBovineAndLactation(bovineId, "002"))
                .thenReturn(Optional.empty());

        Optional<List<MilkingDTO>> result = milkingProcessor.getMilkingByLactation(bovineId, "2", null);

        assertTrue(result.isEmpty());
        verify(milkingService).getMilkingByBovineAndLactation(bovineId, "002");
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

