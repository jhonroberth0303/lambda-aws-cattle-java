package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.CowWithLactationsDTO;
import com.cattle.dtos.LactationSummaryDTO;
import com.cattle.dtos.MilkingDTO;
import com.cattle.entities.MilkingRecord;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.entities.bovines.ProfileReproductive;
import com.cattle.enums.LogType;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.mapper.MilkingMapperImpl;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.repository.ProfileLifecycleRepository;
import com.cattle.repository.ProfileReproductiveRepository;
import com.cattle.services.MilkingService;
import io.jsonwebtoken.lang.Strings;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MilkingProcessor {

    private static final String PK_PREFIX = "BOVINE#";
    private static final String SK_PREFIX = "MILKING#";
    private static final String LACT_PREFIX = "LACT#";
    private static final String PROFILE_LIFECYCLE_SK = "PROFILE#LIFECYCLE";
    private static final String PROFILE_REPRODUCTIVE_SK = "PROFILE#REPRODUCTIVE";
    private static final String LACTATING_STATUS = "LACTATING";
    public static final String HASH_TAG = "#";

    private final MilkingService milkingService;
    private final MilkingMapperImpl milkingMapperImpl;
    private final LambdaContext lambdaContext;
    private final ProfileLactancyRepository profileLactancyRepository;
    private final ProfileLifecycleRepository profileLifecycleRepository;
    private final ProfileReproductiveRepository profileReproductiveRepository;

    public MilkingProcessor(MilkingService milkingService, MilkingMapperImpl milkingMapperImpl,
                            LambdaContext lambdaContext,
                            ProfileLactancyRepository profileLactancyRepository,
                            ProfileLifecycleRepository profileLifecycleRepository,
                            ProfileReproductiveRepository profileReproductiveRepository) {
        this.milkingService = milkingService;
        this.milkingMapperImpl = milkingMapperImpl;
        this.lambdaContext = lambdaContext;
        this.profileLactancyRepository = profileLactancyRepository;
        this.profileLifecycleRepository = profileLifecycleRepository;
        this.profileReproductiveRepository = profileReproductiveRepository;
    }

    public Optional<List<MilkingDTO>> getMilkingData(Integer idBovine, String shift) {
        String pk = PK_PREFIX + idBovine;
        return milkingService.getMilkingByPk(pk)
                .map(milkings -> milkings.stream()
                        .filter(m -> shift == null || shift.isEmpty() || shift.equalsIgnoreCase(m.getShift()))
                        .map(milkingMapperImpl::toDTO)
                        .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty());
    }

    public Optional<MilkingDTO> createMilking(MilkingDTO milkingDTO) {
        MilkingRecord entity = milkingMapperImpl.toEntity(milkingDTO);
        setPkSkAndLactation(entity);
        Optional<MilkingRecord> saved = milkingService.save(entity);
        return saved.map(milkingMapperImpl::toDTO);
    }

    private void setPkSkAndLactation(MilkingRecord entity) {
        Integer bovineId = entity.getBovineId();
        String date = entity.getDate();
        String shift = entity.getShift();

        if (bovineId == null || bovineId <= 0) {
            throw new IllegalArgumentException("El campo bovineId es obligatorio y debe ser mayor a 0.");
        }
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("El campo date es obligatorio.");
        }
        if (shift == null || shift.isEmpty()) {
            throw new IllegalArgumentException("El campo shift es obligatorio.");
        }

        try {
            date = java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE).toString();
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("El campo date debe tener el formato YYYY-MM-DD.");
        }

        entity.setPK(PK_PREFIX + bovineId);
        entity.setSK(SK_PREFIX + date + HASH_TAG + shift);
        entity.setCreatedAt(Instant.now().toString());

        assignLactationToMilking(entity, bovineId);
    }

    private void assignLactationToMilking(MilkingRecord entity, Integer bovineId) {
        String pk = PK_PREFIX + bovineId;
        ProfileLifecycle lifecycle = profileLifecycleRepository.findById(pk, PROFILE_LIFECYCLE_SK)
                .orElseThrow(() -> new IllegalArgumentException("El bovino " + bovineId + " no tiene perfil lifecycle registrado"));
        validateOperationalLifecycle(lifecycle, bovineId);

        ProfileReproductive reproductive = profileReproductiveRepository.findById(pk, PROFILE_REPRODUCTIVE_SK)
                .orElseThrow(() -> new IllegalArgumentException("El bovino " + bovineId + " no tiene perfil reproductivo registrado"));

        String currentLactationId = reproductive.getCurrentLactationId();
        if (currentLactationId == null || currentLactationId.isBlank()) {
            throw new IllegalArgumentException("El bovino " + bovineId + " no tiene una lactancia activa referenciada");
        }

        ProfileLactancy lactation = profileLactancyRepository.findById(pk, currentLactationId)
                .orElseThrow(() -> new IllegalArgumentException("El bovino " + bovineId + " no tiene una lactancia vigente válida"));
        validateLactationForMilking(lactation, bovineId);
        Integer lactNum = parseLactationNumber(lactation.getLactationNumber());

        if (lactNum == null) {
            throw new IllegalArgumentException("El número de lactancia del bovino " + bovineId + " es inválido");
        }

        entity.setLactationNumber(lactNum);
        String lactNumStr = String.format("%03d", lactNum);
        entity.setGsi2pk(PK_PREFIX + bovineId + HASH_TAG + LACT_PREFIX + lactNumStr);
        entity.setGsi2sk(entity.getDate() + HASH_TAG + entity.getShift());
    }

    public Optional<List<CowWithLactationsDTO>> getCowsWithLactations(String siteId) {
        lambdaContext.logInfo(LogType.PROCESSOR, "Fetching operational milking cows");
        Optional<List<ProfileLactancy>> allLactationsOpt = profileLactancyRepository.findAllLactations(siteId);

        if (allLactationsOpt.isEmpty()) {
            lambdaContext.logInfo(LogType.PROCESSOR, "No lactations found for operational milking view");
            return Optional.empty();
        }

        Map<Integer, List<ProfileLactancy>> lactationsByBovine = groupLactationsByBovine(allLactationsOpt.get());
        List<CowWithLactationsDTO> result = new ArrayList<>();

        for (Map.Entry<Integer, List<ProfileLactancy>> entry : lactationsByBovine.entrySet()) {
            Integer bovineId = entry.getKey();
            String pk = PK_PREFIX + bovineId;

            Optional<ProfileLifecycle> lifecycleOpt = profileLifecycleRepository.findById(pk, PROFILE_LIFECYCLE_SK);
            if (lifecycleOpt.isEmpty() || !isOperationalLifecycle(lifecycleOpt.get())) {
                continue;
            }

            Optional<ProfileReproductive> reproductiveOpt = profileReproductiveRepository.findById(pk, PROFILE_REPRODUCTIVE_SK);
            if (reproductiveOpt.isEmpty()) {
                continue;
            }

            String currentLactationId = reproductiveOpt.get().getCurrentLactationId();
            if (currentLactationId == null || currentLactationId.isBlank()) {
                continue;
            }

            Optional<ProfileLactancy> lactationOpt = entry.getValue().stream()
                    .filter(lactation -> currentLactationId.equals(lactation.getSk()))
                    .findFirst()
                    .or(() -> profileLactancyRepository.findById(pk, currentLactationId));

            if (lactationOpt.isEmpty() || !isValidCurrentLactation(lactationOpt.get())) {
                continue;
            }

            result.add(CowWithLactationsDTO.builder()
                    .bovineId(bovineId)
                    .lactations(List.of(toLactationSummary(lactationOpt.get())))
                    .build());
        }

        lambdaContext.logInfo(LogType.PROCESSOR, "Operational milking cows found: " + result.size());
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    public Optional<List<CowWithLactationsDTO>> getCowsWithLactationsHistory(String siteId) {

        lambdaContext.logInfo(LogType.PROCESSOR, "Fetching cows with lactations");
        Optional<List<ProfileLactancy>> allLactationsOpt = profileLactancyRepository.findAllLactations(siteId);

        if (allLactationsOpt.isEmpty()) {
            lambdaContext.logInfo(LogType.PROCESSOR, "No lactations found");
            return Optional.empty();
        }

        List<ProfileLactancy> allLactations = allLactationsOpt.get();

    Map<Integer, List<ProfileLactancy>> lactationsByBovine = groupLactationsByBovine(allLactations);

        lambdaContext.logInfo(LogType.PROCESSOR, "Found " + lactationsByBovine.size() + " bovines with lactations");

        List<CowWithLactationsDTO> result = new ArrayList<>();

        for (Map.Entry<Integer, List<ProfileLactancy>> entry : lactationsByBovine.entrySet()) {
            Integer bovineId = entry.getKey();
            List<ProfileLactancy> bovineLactations = entry.getValue();

            List<LactationSummaryDTO> lactations = bovineLactations.stream()
                    .map(this::toLactationSummary)
                    .sorted(Comparator.comparing(LactationSummaryDTO::getLactationNumber))
                    .collect(Collectors.toList());

            CowWithLactationsDTO cow = CowWithLactationsDTO.builder()
                    .bovineId(bovineId)
                    .lactations(lactations)
                    .build();

            result.add(cow);
        }

        lambdaContext.logInfo(LogType.PROCESSOR, "Total cows with lactations found: " + result.size());
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    public Optional<List<MilkingDTO>> getMilkingByLactation(Integer bovineId, String lactationNumber, String shift) {
        return milkingService.getMilkingByBovineAndLactation(bovineId, formatLactationNumber(lactationNumber))
                .map(records -> records.stream()
                        .filter(r -> shift == null || shift.isEmpty() || shift.equalsIgnoreCase(r.getShift()))
                        .map(milkingMapperImpl::toDTO)
                        .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty());
    }

    private Integer extractBovineIdFromPk(String pk) {
        if (pk == null || !pk.startsWith(PK_PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(pk.substring(PK_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<Integer, List<ProfileLactancy>> groupLactationsByBovine(List<ProfileLactancy> lactations) {
        return lactations.stream()
                .filter(lact -> extractBovineIdFromPk(lact.getPk()) != null)
                .collect(Collectors.groupingBy(lact -> extractBovineIdFromPk(lact.getPk())));
    }

    private LactationSummaryDTO toLactationSummary(ProfileLactancy lactancy) {
        String lactNumber = lactancy.getSk() != null && lactancy.getSk().startsWith(LACT_PREFIX)
                ? lactancy.getSk().substring(LACT_PREFIX.length())
                : Strings.EMPTY;

        return LactationSummaryDTO.builder()
                .lactationNumber(lactNumber)
                .startDate(lactancy.getStartDate())
                .endDate(lactancy.getEndDate())
                .status(lactancy.getStatus())
                .build();
    }

    private void validateOperationalLifecycle(ProfileLifecycle lifecycle, Integer bovineId) {
        if (!isOperationalLifecycle(lifecycle)) {
            throw new IllegalArgumentException("El bovino " + bovineId + " no está habilitado para el flujo operativo de ordeño");
        }
    }

    private boolean isOperationalLifecycle(ProfileLifecycle lifecycle) {
        return lifecycle.getStatus() == LifecycleStatus.OPEN && Boolean.TRUE.equals(lifecycle.getEnabled());
    }

    private void validateLactationForMilking(ProfileLactancy lactation, Integer bovineId) {
        if (!isValidCurrentLactation(lactation)) {
            throw new IllegalArgumentException("El bovino " + bovineId + " no tiene una lactancia activa válida para ordeño");
        }
    }

    private boolean isValidCurrentLactation(ProfileLactancy lactation) {
        return LACTATING_STATUS.equalsIgnoreCase(lactation.getStatus()) && lactation.getEndDate() == null;
    }

    private Integer parseLactationNumber(String lactationNumber) {
        if (lactationNumber == null) {
            return null;
        }
        try {
            return Integer.parseInt(lactationNumber);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatLactationNumber(String lactationNumber) {
        Integer parsedLactationNumber = parseLactationNumber(lactationNumber);
        if (parsedLactationNumber == null) {
            throw new IllegalArgumentException("El número de lactancia es inválido");
        }
        return String.format("%03d", parsedLactationNumber);
    }

}
