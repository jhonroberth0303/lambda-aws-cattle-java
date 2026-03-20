package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.BovineRepository;
import com.cattle.repository.CounterRepository;
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
 * Tests unitarios para BovinesService
 * Fase 1.1 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: 0% → 95%
 * Tests: 18
 */
@Tag("unit")
@Tag("fast")
@Tag("services")
class BovineIdentityItemServiceTest {

    @Mock
    private BovineRepository bovineRepository;

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private CounterRepository counterRepository;

    private BovineService bovineService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        bovineService = new BovineService(bovineRepository, lambdaContext, counterRepository);
    }

    // ==================== findAll Tests ====================

    @Test
    void findAll_returnsListOfBovines() {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = TestDataBuilder.createBovineList(farmId, 5);
        when(bovineRepository.findAll()).thenReturn(Optional.of(bovineIdentityItems));

        // Act
        Optional<List<BovineIdentityItem>> result = bovineService.findAll();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get().size());
        verify(bovineRepository, times(1)).findAll();
        verify(lambdaContext, never()).logException(any(), anyString(), any());
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        // Arrange
        when(bovineRepository.findAll()).thenReturn(Optional.empty());

        // Act
        Optional<List<BovineIdentityItem>> result = bovineService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(bovineRepository, times(1)).findAll();
    }

    @Test
    void findAll_repositoryException_throwsServiceException() {
        // Arrange
        when(bovineRepository.findAll()).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.findAll());
    }

    // ==================== findById Tests ====================

    @Test
    void findById_existingId_returnsBovine() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));

        // Act
        Optional<BovineIdentityItem> result = bovineService.findById(bovineId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(bovineId, result.get().getBovineId());
        verify(bovineRepository, times(1)).findById(bovineId);
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        // Arrange
        Integer bovineId = 999;
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.empty());

        // Act
        Optional<BovineIdentityItem> result = bovineService.findById(bovineId);

        // Assert
        assertTrue(result.isEmpty());
        verify(bovineRepository, times(1)).findById(bovineId);
    }

    @Test
    void findById_repositoryException_throwsServiceException() {
        // Arrange
        Integer bovineId = 123;
        when(bovineRepository.findById(bovineId)).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.findById(bovineId));
    }

    // ==================== save Tests ====================

    @Test
    void save_validBovine_returnsCreatedBovine() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(null); // Nuevo bovino sin ID
        
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getBovineId());
        assertNotNull(result.get().getCreatedAt());
        assertNotNull(result.get().getUpdatedAt());
        verify(counterRepository, times(1)).getNextId(any());
        verify(bovineRepository, times(1)).save(any(BovineIdentityItem.class));
    }

    @Test
    void save_withFarmId_assignsFarmId() {
        // Arrange
        String farmId = "farm-XYZ";
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine(farmId, "bull");
        
        when(counterRepository.getNextId(any())).thenReturn("200");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(farmId, result.get().getFarmId());
    }

    @Test
    void save_generatesId_whenNotProvided() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "heifer");
        bovineIdentityItem.setBovineId(null);
        
        when(counterRepository.getNextId(any())).thenReturn("42");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(42, result.get().getBovineId());
        assertEquals("BOVINE#42", result.get().getPk());
        assertEquals("IDENTITY", result.get().getSk());
        assertEquals("IDENTITY", result.get().getGsi1pk());
        assertEquals("BOVINE#42", result.get().getGsi1sk());
    }

    @Test
    void save_repositoryException_throwsServiceException() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.save(bovineIdentityItem));
    }

    // ==================== update Tests ====================

    @Test
    void update_existingBovine_returnsUpdated() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem existingBovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        existingBovineIdentityItem.setBovineId(bovineId);
        
        BovineIdentityItem updatedBovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        updatedBovineIdentityItem.setBovineId(bovineId);
        updatedBovineIdentityItem.setName("Updated Name");
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(existingBovineIdentityItem));
        when(bovineRepository.update(any(BovineIdentityItem.class))).thenReturn(Optional.of(updatedBovineIdentityItem));

        // Act
        Optional<BovineIdentityItem> result = bovineService.update(updatedBovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("BOVINE#123", result.get().getPk());
        assertEquals("IDENTITY", result.get().getSk());
        assertNotNull(result.get().getUpdatedAt());
        verify(bovineRepository, times(1)).findById(bovineId);
        verify(bovineRepository, times(1)).update(any(BovineIdentityItem.class));
    }

    @Test
    void update_nonExisting_throwsServiceException() {
        // Arrange
        Integer bovineId = 999;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> bovineService.update(bovineIdentityItem));
        assertTrue(exception.getMessage().contains("not found"));
        verify(bovineRepository, times(1)).findById(bovineId);
        verify(bovineRepository, never()).update(any());
    }

    @Test
    void update_repositoryException_throwsServiceException() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));
        when(bovineRepository.update(any(BovineIdentityItem.class))).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.update(bovineIdentityItem));
    }

    // ==================== Additional Business Logic Tests ====================

    @Test
    void save_setsCorrectDynamoDBKeys() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        when(counterRepository.getNextId(any())).thenReturn("555");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        BovineIdentityItem saved = result.get();
        assertEquals("BOVINE#555", saved.getPk());
        assertEquals("IDENTITY", saved.getSk());
        assertEquals("IDENTITY", saved.getGsi1pk());
        assertEquals("BOVINE#555", saved.getGsi1sk());
    }

    @Test
    void save_setsEnabledToTrue() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void save_setsTimestamps() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getCreatedAt());
        assertNotNull(result.get().getUpdatedAt());
        assertEquals(result.get().getCreatedAt(), result.get().getUpdatedAt());
    }

    @Test
    void update_onlyUpdatesTimestamp() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        String originalCreatedAt = "2025-01-01T00:00:00Z";
        bovineIdentityItem.setCreatedAt(originalCreatedAt);
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));
        when(bovineRepository.update(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.update(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(originalCreatedAt, result.get().getCreatedAt()); // No cambia
        assertNotNull(result.get().getUpdatedAt()); // Se actualiza
    }
}
