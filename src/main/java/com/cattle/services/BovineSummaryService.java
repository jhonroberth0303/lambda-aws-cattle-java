package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineSummaryDTO;
import com.cattle.entities.bovines.*;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.mapper.BovineSummaryMapper;
import com.cattle.repository.*;
import com.cattle.services.ProductiveStateCalculator.ProductiveStateResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de resúmenes de bovinos.
 * Proporciona operaciones de consulta y regeneración de ítems SUMMARY.
 */
@Service
public class BovineSummaryService {

    private static final ZoneId ZONE_ID = ZoneId.of(System.getenv().getOrDefault("APP_TIMEZONE", "America/Bogota"));
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    public static final String OPEN = "OPEN";

    private final BovineSummaryRepository summaryRepository;
    private final BovineRepository bovineRepository;
    private final ProfileLifecycleRepository lifecycleRepository;
    private final ProfileReproductiveRepository reproductiveRepository;
    private final ProfileLactancyRepository lactancyRepository;
    private final ProfilePregnancyRepository pregnancyRepository;
    private final BovineSummaryMapper mapper;
    private final LambdaContext lambdaContext;
    private final LifecycleRecalculationService lifecycleRecalculationService;
    private final ProductiveStateCalculator productiveStateCalculator;

    public BovineSummaryService(
            BovineSummaryRepository summaryRepository,
            BovineRepository bovineRepository,
            ProfileLifecycleRepository lifecycleRepository,
            ProfileReproductiveRepository reproductiveRepository,
            ProfileLactancyRepository lactancyRepository,
            ProfilePregnancyRepository pregnancyRepository,
            BovineSummaryMapper mapper,
            LambdaContext lambdaContext,
            LifecycleRecalculationService lifecycleRecalculationService,
            ProductiveStateCalculator productiveStateCalculator) {
        this.summaryRepository = summaryRepository;
        this.bovineRepository = bovineRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.reproductiveRepository = reproductiveRepository;
        this.lactancyRepository = lactancyRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.mapper = mapper;
        this.lambdaContext = lambdaContext;
        this.lifecycleRecalculationService = lifecycleRecalculationService;
        this.productiveStateCalculator = productiveStateCalculator;
    }

    /**
     * Obtiene todos los resúmenes de bovinos.
     * Ordena por status: OPEN primero, luego el resto.
     * @return Lista de BovineSummaryDTO ordenada por status
     */
    public List<BovineSummaryDTO> findAll() {
        try {
            List<BovineSummary> summaries = summaryRepository.findAll();
            return summaries.stream()
                    .map(mapper::toDTO)
                    .sorted(Comparator.comparing(
                            s -> !OPEN.equalsIgnoreCase(s.getStatus())))
                    .collect(Collectors.toList());
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to fetch all summaries: " + e.getMessage());
            throw new ServiceException("Failed to fetch summaries", e);
        }
    }

    /**
     * Obtiene el resumen de un bovino específico.
     * @param bovineId ID del bovino
     * @return Optional con BovineSummaryDTO o vacío si no existe
     */
    public Optional<BovineSummaryDTO> findById(Integer bovineId) {
        try {
            lambdaContext.logInfo(LogType.SERVICE, "Finding summary for bovine ID: " + bovineId);
            return summaryRepository.findById(bovineId)
                    .map(mapper::toDTO);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to fetch summary by ID: " + e.getMessage());
            throw new ServiceException("Failed to fetch summary by ID", e);
        }
    }

    /**
     * Regenera el resumen de un bovino específico.
     * Consulta todos los perfiles y construye el ítem SUMMARY.
     * @param bovineId ID del bovino
     * @return BovineSummaryDTO regenerado
     */
    public BovineSummaryDTO refreshSummary(Integer bovineId) {
        try {
            lambdaContext.logInfo(LogType.SERVICE, "Refreshing summary for bovine ID: " + bovineId);

            // Obtener bovino (identidad)
            Optional<BovineIdentityItem> bovineOpt = bovineRepository.findById(bovineId);
            if (bovineOpt.isEmpty()) {
                throw new ServiceException("Bovine not found with ID: " + bovineId);
            }
            BovineIdentityItem bovineIdentityItem = bovineOpt.get();

            // Construir summary
            BovineSummary summary = buildSummary(bovineIdentityItem);

            // Guardar y retornar
            BovineSummary saved = summaryRepository.save(summary);
            return mapper.toDTO(saved);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to refresh summary: " + e.getMessage());
            throw new ServiceException("Failed to refresh summary", e);
        }
    }

    /**
     * Regenera todos los resúmenes de bovinos (batch).
     * @return Número de resúmenes regenerados
     */
    public int refreshAllSummaries() {
        try {
            lambdaContext.logInfo(LogType.SERVICE, "Starting batch refresh of all summaries");

            Optional<List<BovineIdentityItem>> bovinesOpt = bovineRepository.findAll();
            if (bovinesOpt.isEmpty() || bovinesOpt.get().isEmpty()) {
                lambdaContext.logInfo(LogType.SERVICE, "No bovines found for refresh");
                return 0;
            }

            List<BovineIdentityItem> bovineIdentityItems = bovinesOpt.get();
            List<BovineSummary> summaries = new ArrayList<>();

            for (BovineIdentityItem bovineIdentityItem : bovineIdentityItems) {
                try {
                    BovineSummary summary = buildSummary(bovineIdentityItem);
                    summaries.add(summary);
                } catch (Exception e) {
                    lambdaContext.logException(LogType.SERVICE, 
                        "Failed to build summary for bovine: " + bovineIdentityItem.getBovineId() + " - " + e.getMessage());
                }
            }

            int saved = summaryRepository.saveAll(summaries);
            lambdaContext.logInfo(LogType.SERVICE, "Batch refresh completed: " + saved + " summaries updated");
            return saved;
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to refresh all summaries: " + e.getMessage());
            throw new ServiceException("Failed to refresh all summaries", e);
        }
    }

    /**
     * Construye un BovineSummary a partir de un Bovine y sus perfiles relacionados.
     */
    private BovineSummary buildSummary(BovineIdentityItem bovineIdentityItem) {
        String pk = bovineIdentityItem.getPk();
        Integer bovineId = bovineIdentityItem.getBovineId();
        Optional<ProfileLifecycle> lifecycleOpt = lifecycleRepository.findById(pk, "PROFILE#LIFECYCLE");
        Optional<ProfileReproductive> reproductiveOpt = reproductiveRepository.findById(pk, "PROFILE#REPRODUCTIVE");

        // Datos de preñez activa
        Boolean isPregnant = false;
        String pregnancyStatus = null;
        String expectedDueDate = null;
        String calvingDate = null;

        if (reproductiveOpt.isPresent()) {
            ProfileReproductive reproductive = reproductiveOpt.get();
            String currentPregnancyId = reproductive.getCurrentPregnancyId();
            if (currentPregnancyId != null && !currentPregnancyId.isEmpty()) {
                Optional<ProfilePregnancy> pregnancyOpt = pregnancyRepository.findById(pk, currentPregnancyId);
                if (pregnancyOpt.isPresent()) {
                    ProfilePregnancy pregnancy = pregnancyOpt.get();
                    isPregnant = "ACTIVE".equalsIgnoreCase(pregnancy.getStatus());
                    pregnancyStatus = pregnancy.getStatus();
                    expectedDueDate = pregnancy.getExpectedDueDate();
                    // Solo usar calvingDate si la preñez NO está activa (ya parió)
                    // Una preñez ACTIVE no puede tener calvingDate válido
                    if (!Boolean.TRUE.equals(isPregnant)) {
                        calvingDate = pregnancy.getCalvingDate();
                    }
                }
            }
        }

        // Datos de lactancia activa
        Boolean isLactating = false;
        String lactationStatus = null;
        String lactationNumber = null;
        String lactationStartDate = null;

        if (reproductiveOpt.isPresent()) {
            ProfileReproductive reproductive = reproductiveOpt.get();
            String currentLactationId = reproductive.getCurrentLactationId();
            if (currentLactationId != null && !currentLactationId.isEmpty()) {
                Optional<ProfileLactancy> lactancyOpt = lactancyRepository.findById(pk, currentLactationId);
                if (lactancyOpt.isPresent()) {
                    ProfileLactancy lactancy = lactancyOpt.get();
                    isLactating = "LACTATING".equalsIgnoreCase(lactancy.getStatus());
                    lactationStatus = lactancy.getStatus();
                    lactationNumber = lactancy.getLactationNumber();
                    lactationStartDate = lactancy.getStartDate();
                }
            }
        }

        // Datos de lifecycle
        String category = null;
        String status = null;
        String lifeStage = null;
        Boolean enabled = null;
        if (lifecycleOpt.isPresent()) {
            ProfileLifecycle lifecycle = lifecycleOpt.get();
            // Recalcular y actualizar
            LifecycleRecalculationService.RecalculationResult result = lifecycleRecalculationService.recalculate(bovineIdentityItem, lifecycle);
            ProfileLifecycle updatedLifecycle = lifecycleRecalculationService.applyRecalculation(lifecycle, result);
            if (updatedLifecycle == null) {
                updatedLifecycle = lifecycle;
            }
            lifecycleRepository.save(updatedLifecycle); // Persistir cambios
            category = updatedLifecycle.getCategory() != null ? updatedLifecycle.getCategory().name() : null;
            status = updatedLifecycle.getStatus() != null ? updatedLifecycle.getStatus().name() : null;
            lifeStage = updatedLifecycle.getLifeStage() != null ? updatedLifecycle.getLifeStage().name() : null;
            enabled = updatedLifecycle.getEnabled();
        }

        // Calcular estados productivos y alertas
        LocalDate today = LocalDate.now(ZONE_ID);
        ProductiveStateResult stateResult = productiveStateCalculator.calculate(
            isPregnant,
            pregnancyStatus,
            expectedDueDate,
            calvingDate,
            lactationStatus,
            lactationStartDate,
            today
        );

        // Timestamp actual
        String updatedAt = ZonedDateTime.now(ZONE_ID).format(ISO_FORMATTER);

        return BovineSummary.builder()
                .pk(pk)
                .sk("SUMMARY")
                .gsi1pk("SUMMARY")
                .gsi1sk(pk)
                .bovineId(bovineId)
                .name(bovineIdentityItem.getName())
                .gender(bovineIdentityItem.getGender())
                .breed(bovineIdentityItem.getBreed())
                .bornDate(bovineIdentityItem.getBornDate())
                .farmId(bovineIdentityItem.getFarmId())
                .category(category)
                .status(status)
                .lifeStage(lifeStage)
                .enabled(enabled)
                .currentLactationId(reproductiveOpt.map(ProfileReproductive::getCurrentLactationId).orElse(null))
                .currentPregnancyId(reproductiveOpt.map(ProfileReproductive::getCurrentPregnancyId).orElse(null))
                .isPregnant(isPregnant)
                .pregnancyStatus(pregnancyStatus)
                .expectedDueDate(expectedDueDate)
                .calvingDate(calvingDate)
                .isLactating(isLactating)
                .lactationStatus(lactationStatus)
                .lactationNumber(lactationNumber)
                .lactationStartDate(lactationStartDate)
                // Calculated states
                .reproductiveState(stateResult.reproductiveState().name())
                .productiveState(stateResult.productiveState().name())
                .alerts(stateResult.alertsAsStrings())
                .daysUntilDue(stateResult.daysUntilDue())
                .daysInLactation(stateResult.daysInLactation())
                .daysSinceCalving(stateResult.daysSinceCalving())
                .updatedAt(updatedAt)
                .build();
    }
}
