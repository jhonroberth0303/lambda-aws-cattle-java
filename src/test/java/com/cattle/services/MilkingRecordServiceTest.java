package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.MilkingRecord;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.MilkingRepository;
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
 * Tests unitarios para MilkingService
 * Fase 1.2 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: 22% → 95%
 * Tests: 10
 */
@Tag("unit")
@Tag("fast")
@Tag("services")
class MilkingRecordServiceTest {

    @Mock
    private MilkingRepository milkingRepository;

    @Mock
    private LambdaContext lambdaContext;

    private MilkingService milkingService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        milkingService = new MilkingService(milkingRepository, lambdaContext);
    }

    // ==================== getMilkingByPk Tests ====================

    @Test
    void getMilkingData_validBovineAndDate_returnsMilking() {
        // Arrange
        String pk = "BOVINE#1";
        List<MilkingRecord> milkingRecords = new ArrayList<>();
        
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK(pk)
                .SK("MILKING#2026-01-20#AM")
                .bovineId(1)
                .date("2026-01-20")
                .shift("AM")
                .liters(20.5)
                .status("completo")
                .build();
        
        milkingRecords.add(milkingRecord);
        when(milkingRepository.getMilkingByPk(pk)).thenReturn(Optional.of(milkingRecords));

        // Act
        Optional<List<MilkingRecord>> result = milkingService.getMilkingByPk(pk);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals(20.5, result.get().get(0).getLiters());
        verify(milkingRepository, times(1)).getMilkingByPk(pk);
    }

    @Test
    void getMilkingData_invalidBovine_returnsEmpty() {
        // Arrange
        String pk = "BOVINE#999";
        when(milkingRepository.getMilkingByPk(pk)).thenReturn(Optional.empty());

        // Act
        Optional<List<MilkingRecord>> result = milkingService.getMilkingByPk(pk);

        // Assert
        assertTrue(result.isEmpty());
        verify(milkingRepository, times(1)).getMilkingByPk(pk);
    }

    @Test
    void getMilkingData_noDataForDate_returnsEmpty() {
        // Arrange
        String pk = "BOVINE#1";
        when(milkingRepository.getMilkingByPk(pk)).thenReturn(Optional.of(new ArrayList<>()));

        // Act
        Optional<List<MilkingRecord>> result = milkingService.getMilkingByPk(pk);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
        verify(milkingRepository, times(1)).getMilkingByPk(pk);
    }

    @Test
    void getMilkingByBovineAndLactation_validInput_returnsMilking() {
        Integer bovineId = 1;
        String lactationNumber = "002";
        List<MilkingRecord> milkingRecords = List.of(
                MilkingRecord.builder()
                        .PK("BOVINE#1")
                        .SK("MILKING#2026-01-20#AM")
                        .bovineId(1)
                        .date("2026-01-20")
                        .shift("AM")
                        .liters(20.5)
                        .lactationNumber(2)
                        .build()
        );

        when(milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber))
                .thenReturn(Optional.of(milkingRecords));

        Optional<List<MilkingRecord>> result = milkingService.getMilkingByBovineAndLactation(bovineId, lactationNumber);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals(2, result.get().get(0).getLactationNumber());
        verify(milkingRepository).getMilkingByBovineAndLactation(bovineId, lactationNumber);
    }

    @Test
    void getMilkingByBovineAndLactation_withoutRecords_returnsEmpty() {
        Integer bovineId = 1;
        String lactationNumber = "002";
        when(milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber))
                .thenReturn(Optional.empty());

        Optional<List<MilkingRecord>> result = milkingService.getMilkingByBovineAndLactation(bovineId, lactationNumber);

        assertTrue(result.isEmpty());
        verify(milkingRepository).getMilkingByBovineAndLactation(bovineId, lactationNumber);
    }

    @Test
    void getMilkingByBovineAndLactation_repositoryFailure_throwsServiceException() {
        Integer bovineId = 1;
        String lactationNumber = "002";
        when(milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber))
                .thenThrow(new RepositoryException("GSI query failed"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> milkingService.getMilkingByBovineAndLactation(bovineId, lactationNumber)
        );

        assertTrue(exception.getMessage().contains("milking by bovine and lactation"));
        verify(milkingRepository).getMilkingByBovineAndLactation(bovineId, lactationNumber);
    }

    // ==================== save Tests ====================

    @Test
    void createMilking_validData_createsSuccessfully() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#1")
                .SK("MILKING#2026-01-20#PM")
                .bovineId(1)
                .date("2026-01-20")
                .shift("PM")
                .liters(18.5)
                .status("completo")
                .observations("Normal")
                .build();

        when(milkingRepository.save(any(MilkingRecord.class))).thenReturn(Optional.of(milkingRecord));

        // Act
        Optional<MilkingRecord> result = milkingService.save(milkingRecord);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(18.5, result.get().getLiters());
        assertEquals("PM", result.get().getShift());
        verify(milkingRepository, times(1)).save(milkingRecord);
    }

    @Test
    void createMilking_duplicateEntry_throwsException() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#1")
                .SK("MILKING#2026-01-20#AM")
                .bovineId(1)
                .build();

        when(milkingRepository.save(any(MilkingRecord.class)))
                .thenThrow(new RepositoryException("Duplicate key"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> milkingService.save(milkingRecord));
        verify(milkingRepository, times(1)).save(milkingRecord);
    }

    @Test
    void createMilking_invalidLiters_throwsValidationException() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#1")
                .SK("MILKING#2026-01-20#AM")
                .bovineId(1)
                .liters(-5.0) // Invalid negative value
                .build();

        when(milkingRepository.save(any(MilkingRecord.class)))
                .thenThrow(new RepositoryException("Invalid data"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> milkingService.save(milkingRecord));
    }

    @Test
    void createMilking_setsPkSk_correctly() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#42")
                .SK("MILKING#2026-01-20#AM")
                .bovineId(42)
                .date("2026-01-20")
                .shift("AM")
                .liters(22.0)
                .build();

        when(milkingRepository.save(any(MilkingRecord.class))).thenReturn(Optional.of(milkingRecord));

        // Act
        Optional<MilkingRecord> result = milkingService.save(milkingRecord);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("BOVINE#42", result.get().getPK());
        assertEquals("MILKING#2026-01-20#AM", result.get().getSK());
    }

    @Test
    void createMilking_assignsLactationNumber() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#1")
                .SK("MILKING#2026-01-20#AM")
                .bovineId(1)
                .liters(20.0)
                .build();

        when(milkingRepository.save(any(MilkingRecord.class))).thenReturn(Optional.of(milkingRecord));

        // Act
        Optional<MilkingRecord> result = milkingService.save(milkingRecord);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getBovineId());
    }

    @Test
    void repositoryFailure_throwsServiceException() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#1")
                .SK("MILKING#2026-01-20#AM")
                .build();

        when(milkingRepository.save(any(MilkingRecord.class)))
                .thenThrow(new RepositoryException("Database connection failed"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, 
                () -> milkingService.save(milkingRecord));
        assertTrue(exception.getMessage().contains("Failed to save farmMilking"));
        verify(milkingRepository, times(1)).save(milkingRecord);
    }

    @Test
    void createMilking_nullValues_throwsException() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK(null) // Null PK
                .SK("MILKING#2026-01-20#AM")
                .build();

        when(milkingRepository.save(any(MilkingRecord.class)))
                .thenThrow(new RepositoryException("Null key not allowed"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> milkingService.save(milkingRecord));
    }
}
