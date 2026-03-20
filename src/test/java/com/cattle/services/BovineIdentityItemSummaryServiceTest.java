package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.entities.bovines.*;
import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.Source;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.mapper.BovineSummaryMapper;
import com.cattle.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * Tests unitarios para BovineSummaryService
 * HU-002-pruebas-summary
 * 
 * Cobertura objetivo: >= 80%
 */
@Tag("unit")
@Tag("fast")
@Tag("services")
@DisplayName("BovineSummaryService Tests")
class BovineIdentityItemSummaryServiceTest {

    @Mock
    private BovineSummaryRepository summaryRepository;
    
    @Mock
    private BovineRepository bovineRepository;
    
    @Mock
    private ProfileLifecycleRepository lifecycleRepository;
    
    @Mock
    private ProfileReproductiveRepository reproductiveRepository;
    
    @Mock
    private ProfileLactancyRepository lactancyRepository;
    
    @Mock
    private ProfilePregnancyRepository pregnancyRepository;
    
    @Mock
    private BovineSummaryMapper mapper;
    
    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private LifecycleRecalculationService lifecycleRecalculationService;

    private ProductiveStateCalculator productiveStateCalculator;

    private BovineSummaryService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        productiveStateCalculator = new ProductiveStateCalculator();
        service = new BovineSummaryService(
            summaryRepository,
            bovineRepository,
            lifecycleRepository,
            reproductiveRepository,
            lactancyRepository,
            pregnancyRepository,
            mapper,
            lambdaContext,
            lifecycleRecalculationService,
            productiveStateCalculator
        );
    }

    // ==================== Helper Methods ====================

    private BovineIdentityItem createTestBovine(Integer id) {
        return BovineIdentityItem.builder()
                .pk("BOVINE#" + id)
                .sk("IDENTITY")
                .bovineId(id)
                .name("Test Bovine " + id)
                .gender("FEMALE")
                .breed("Holstein")
                .bornDate("2022-01-15")
                .farmId("FARM#001")
                .build();
    }

    private BovineSummary createTestSummary(Integer id) {
        return BovineSummary.builder()
                .pk("BOVINE#" + id)
                .sk("SUMMARY")
                .gsi1pk("SUMMARY")
                .gsi1sk("BOVINE#" + id)
                .bovineId(id)
                .name("Test Bovine " + id)
                .gender("FEMALE")
                .breed("Holstein")
                .bornDate("2022-01-15")
                .farmId("FARM#001")
                .category("COW")
                .status("ACTIVE")
                .enabled(true)
                .isPregnant(true)
                .pregnancyStatus("ACTIVE")
                .expectedDueDate("2026-05-15")
                .isLactating(false)
                .build();
    }

    private BovineSummaryDTO createTestSummaryDTO(Integer id) {
        return BovineSummaryDTO.builder()
                .bovineId(id)
                .name("Test Bovine " + id)
                .gender("FEMALE")
                .breed("Holstein")
                .bornDate("2022-01-15")
                .farmId("FARM#001")
                .category("COW")
                .status("ACTIVE")
                .enabled(true)
                .isPregnant(true)
                .pregnancyStatus("ACTIVE")
                .expectedDueDate("2026-05-15")
                .isLactating(false)
                .build();
    }

    private ProfileLifecycle createTestLifecycle(String pk) {
        return ProfileLifecycle.builder()
                .pk(pk)
                .sk("PROFILE#LIFECYCLE")
                .status(LifecycleStatus.OPEN)
                .category(BovineCategory.COW)
                .categorySource(Source.AUTO)
                .lifeStage(LifeStage.ADULT)
                .lifeStageSource(Source.AUTO)
                .enabled(true)
                .build();
    }

    private ProfileReproductive createTestReproductive(String pk) {
        return ProfileReproductive.builder()
                .pk(pk)
                .sk("PROFILE#REPRODUCTIVE")
                .currentPregnancyId("PREG#2025-07-06")
                .currentLactationId("LACT#01")
                .build();
    }

    private ProfilePregnancy createTestPregnancy(String pk) {
        return ProfilePregnancy.builder()
                .pk(pk)
                .sk("PREG#2025-07-06")
                .status("ACTIVE")
                .expectedDueDate("2026-05-15")
                .serviceDate("2025-07-06")
                .build();
    }

    private ProfileLactancy createTestLactancy(String pk) {
        return ProfileLactancy.builder()
                .pk(pk)
                .sk("LACT#01")
                .status("ACTIVE")
                .lactationNumber("1")
                .startDate("2025-01-15")
                .build();
    }

    // ==================== findAll Tests ====================

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar lista de DTOs cuando hay datos")
        void findAll_withData_returnsListOfDTOs() {
            // Arrange
            List<BovineSummary> summaries = List.of(
                createTestSummary(167),
                createTestSummary(168)
            );
            when(summaryRepository.findAll()).thenReturn(summaries);
            when(mapper.toDTO(any(BovineSummary.class))).thenAnswer(inv -> {
                BovineSummary s = inv.getArgument(0);
                return createTestSummaryDTO(s.getBovineId());
            });

            // Act
            List<BovineSummaryDTO> result = service.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(167, result.get(0).getBovineId());
            assertEquals(168, result.get(1).getBovineId());
            verify(summaryRepository, times(1)).findAll();
            verify(mapper, times(2)).toDTO(any(BovineSummary.class));
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay datos")
        void findAll_noData_returnsEmptyList() {
            // Arrange
            when(summaryRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<BovineSummaryDTO> result = service.findAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(summaryRepository, times(1)).findAll();
            verify(mapper, never()).toDTO(any());
        }

        @Test
        @DisplayName("Debe propagar excepción cuando el repositorio falla")
        void findAll_repositoryError_throwsServiceException() {
            // Arrange
            when(summaryRepository.findAll()).thenThrow(new RepositoryException("DB Error", null));

            // Act & Assert
            assertThrows(ServiceException.class, () -> service.findAll());
            verify(lambdaContext).logException(any(), anyString());
        }
    }

    // ==================== findById Tests ====================

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Debe retornar DTO cuando existe el summary")
        void findById_exists_returnsDTO() {
            // Arrange
            Integer bovineId = 167;
            BovineSummary summary = createTestSummary(bovineId);
            BovineSummaryDTO dto = createTestSummaryDTO(bovineId);
            
            when(summaryRepository.findById(bovineId)).thenReturn(Optional.of(summary));
            when(mapper.toDTO(summary)).thenReturn(dto);

            // Act
            Optional<BovineSummaryDTO> result = service.findById(bovineId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(bovineId, result.get().getBovineId());
            assertEquals("Test Bovine 167", result.get().getName());
            verify(summaryRepository, times(1)).findById(bovineId);
        }

        @Test
        @DisplayName("Debe retornar vacío cuando no existe el summary")
        void findById_notExists_returnsEmpty() {
            // Arrange
            Integer bovineId = 999;
            when(summaryRepository.findById(bovineId)).thenReturn(Optional.empty());

            // Act
            Optional<BovineSummaryDTO> result = service.findById(bovineId);

            // Assert
            assertTrue(result.isEmpty());
            verify(mapper, never()).toDTO(any());
        }

        @Test
        @DisplayName("Debe propagar excepción cuando el repositorio falla")
        void findById_repositoryError_throwsServiceException() {
            // Arrange
            when(summaryRepository.findById(anyInt())).thenThrow(new RepositoryException("DB Error", null));

            // Act & Assert
            assertThrows(ServiceException.class, () -> service.findById(167));
        }
    }

    // ==================== refreshSummary Tests ====================

    @Nested
    @DisplayName("refreshSummary() Tests")
    class RefreshSummaryTests {

        @Test
        @DisplayName("Debe regenerar summary con todos los perfiles")
        void refreshSummary_allProfiles_regeneratesSuccessfully() {
            // Arrange
            Integer bovineId = 167;
            String pk = "BOVINE#" + bovineId;
            BovineIdentityItem bovineIdentityItem = createTestBovine(bovineId);
            ProfileLifecycle lifecycle = createTestLifecycle(pk);
            ProfileReproductive reproductive = createTestReproductive(pk);
            ProfilePregnancy pregnancy = createTestPregnancy(pk);
            ProfileLactancy lactancy = createTestLactancy(pk);
            BovineSummaryDTO expectedDTO = createTestSummaryDTO(bovineId);

            when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));
            when(lifecycleRepository.findById(pk, "PROFILE#LIFECYCLE")).thenReturn(Optional.of(lifecycle));
            when(reproductiveRepository.findById(pk, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
            when(pregnancyRepository.findById(pk, "PREG#2025-07-06")).thenReturn(Optional.of(pregnancy));
            when(lactancyRepository.findById(pk, "LACT#01")).thenReturn(Optional.of(lactancy));
            when(summaryRepository.save(any(BovineSummary.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toDTO(any(BovineSummary.class))).thenReturn(expectedDTO);
            when(lifecycleRecalculationService.recalculate(any(), any())).thenReturn(
                new LifecycleRecalculationService.RecalculationResult(false, false, LifeStage.ADULT, BovineCategory.COW, null)
            );
            when(lifecycleRecalculationService.applyRecalculation(any(), any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            BovineSummaryDTO result = service.refreshSummary(bovineId);

            // Assert
            assertNotNull(result);
            assertEquals(bovineId, result.getBovineId());
            verify(summaryRepository, times(1)).save(any(BovineSummary.class));
        }

        @Test
        @DisplayName("Debe regenerar summary sin perfil de preñez")
        void refreshSummary_noPregnancy_regeneratesWithNulls() {
            // Arrange
            Integer bovineId = 168;
            String pk = "BOVINE#" + bovineId;
            BovineIdentityItem bovineIdentityItem = createTestBovine(bovineId);
            ProfileLifecycle lifecycle = createTestLifecycle(pk);
            ProfileReproductive reproductive = ProfileReproductive.builder()
                    .pk(pk)
                    .sk("PROFILE#REPRODUCTIVE")
                    .currentPregnancyId(null)
                    .currentLactationId(null)
                    .build();

            when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));
            when(lifecycleRepository.findById(pk, "PROFILE#LIFECYCLE")).thenReturn(Optional.of(lifecycle));
            when(reproductiveRepository.findById(pk, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
            when(summaryRepository.save(any(BovineSummary.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toDTO(any(BovineSummary.class))).thenReturn(createTestSummaryDTO(bovineId));
            when(lifecycleRecalculationService.recalculate(any(), any())).thenReturn(
                new LifecycleRecalculationService.RecalculationResult(false, false, LifeStage.ADULT, BovineCategory.COW, null)
            );
            when(lifecycleRecalculationService.applyRecalculation(any(ProfileLifecycle.class), any())).thenAnswer(inv -> inv.getArgument(0) != null ? inv.getArgument(0) : createTestLifecycle(pk));

            // Act
            BovineSummaryDTO result = service.refreshSummary(bovineId);

            // Assert
            assertNotNull(result);
            verify(pregnancyRepository, never()).findById(anyString(), anyString());
            verify(lactancyRepository, never()).findById(anyString(), anyString());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando bovino no existe")
        void refreshSummary_bovineNotFound_throwsServiceException() {
            // Arrange
            Integer bovineId = 999;
            when(bovineRepository.findById(bovineId)).thenReturn(Optional.empty());

            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, 
                () -> service.refreshSummary(bovineId));
            assertTrue(exception.getMessage().contains("Bovine not found"));
        }
    }

    // ==================== refreshAllSummaries Tests ====================

    @Nested
    @DisplayName("refreshAllSummaries() Tests")
    class RefreshAllSummariesTests {

        @Test
        @DisplayName("Debe regenerar todos los summaries exitosamente")
        void refreshAllSummaries_withBovines_regeneratesAll() {
            // Arrange
            List<BovineIdentityItem> bovineIdentityItems = List.of(
                createTestBovine(167),
                createTestBovine(168),
                createTestBovine(169)
            );
            
            when(bovineRepository.findAll()).thenReturn(Optional.of(bovineIdentityItems));
            when(lifecycleRepository.findById(anyString(), eq("PROFILE#LIFECYCLE")))
                .thenReturn(Optional.of(createTestLifecycle("BOVINE#167")));
            when(reproductiveRepository.findById(anyString(), eq("PROFILE#REPRODUCTIVE")))
                .thenReturn(Optional.empty());
            when(summaryRepository.saveAll(anyList())).thenReturn(3);

            // Act
            int result = service.refreshAllSummaries();

            // Assert
            assertEquals(3, result);
            verify(bovineRepository, times(1)).findAll();
            verify(summaryRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Debe retornar 0 cuando no hay bovinos")
        void refreshAllSummaries_noBovines_returnsZero() {
            // Arrange
            when(bovineRepository.findAll()).thenReturn(Optional.empty());

            // Act
            int result = service.refreshAllSummaries();

            // Assert
            assertEquals(0, result);
            verify(summaryRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Debe continuar con otros bovinos si uno falla")
        void refreshAllSummaries_oneFailure_continuesWithOthers() {
            // Arrange
            List<BovineIdentityItem> bovineIdentityItems = List.of(
                createTestBovine(167),
                createTestBovine(168)
            );
            
            when(bovineRepository.findAll()).thenReturn(Optional.of(bovineIdentityItems));
            when(lifecycleRepository.findById(eq("BOVINE#167"), eq("PROFILE#LIFECYCLE")))
                .thenThrow(new RuntimeException("Simulated error"));
            when(lifecycleRepository.findById(eq("BOVINE#168"), eq("PROFILE#LIFECYCLE")))
                .thenReturn(Optional.of(createTestLifecycle("BOVINE#168")));
            when(reproductiveRepository.findById(anyString(), eq("PROFILE#REPRODUCTIVE")))
                .thenReturn(Optional.empty());
            when(summaryRepository.saveAll(anyList())).thenReturn(1);

            // Act
            int result = service.refreshAllSummaries();

            // Assert
            assertEquals(1, result);
            verify(lambdaContext, atLeastOnce()).logException(any(), anyString());
        }
    }
}
