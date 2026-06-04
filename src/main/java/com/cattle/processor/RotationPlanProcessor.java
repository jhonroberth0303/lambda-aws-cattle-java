package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import com.cattle.enums.LogType;
import com.cattle.enums.PastureStatus;
import com.cattle.events.EntityPatch;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.ServiceException;
import com.cattle.services.PastureService;
import com.cattle.services.PlanService;
import com.cattle.utils.EtaCalculator;
import com.cattle.utils.PastureStatusEngine;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cattle.events.PatchApplier.applyLocal;

@Service
public class RotationPlanProcessor {

    private final PastureService pastureService;
    private final PlanService planService;
    private final PastureStatusEngine pastureStatusEngine;
    private final LambdaContext lambdaContext;

    public RotationPlanProcessor(
            PastureService pastureService,
            PlanService planService,
            PastureStatusEngine pastureStatusEngine,
            LambdaContext lambdaContext
    ) {
        this.pastureService = pastureService;
        this.planService = planService;
        this.pastureStatusEngine = pastureStatusEngine;
        this.lambdaContext = lambdaContext;
    }

    /**
     * Obtiene la lista de RotationSemaphoreItemDTO para un farmId dado.
     */
    public Optional<List<RotationSemaphoreItemDTO>> getRotationSemaphoreItems(String farmId) {
        try {
            lambdaContext.logInfo(LogType.PROCESSOR, "Obteniendo semáforo de rotación para farmId: " + farmId);
            List<Pasture> pastures = pastureService.getPastures(farmId).orElse(Collections.emptyList());
            pastures.stream().forEach(p -> lambdaContext.logInfo(LogType.PROCESSOR, "Pasture found: " + p.getId() + ", species: " + p.getSpecies()));
            List<Plan> plans = planService.getPlans(farmId).orElse(Collections.emptyList());
            plans.stream().forEach(plan -> lambdaContext.logInfo(LogType.PROCESSOR, "Plan found: " + plan.getPk() + ", species: " + plan.getSpecies()));

            if (pastures.isEmpty()) {
                lambdaContext.logInfo(LogType.PROCESSOR, "No se encontraron pasturas para farmId: " + farmId);
                return Optional.empty();
            }

            List<RotationSemaphoreItemDTO> items = pastures.stream()
                    .map(pasture -> {
                        Plan plan = findPlanForSpecies(plans, pasture.getSpecies());
                        int eta = EtaCalculator.etaOpenDays(pasture, plan);
                        EntityPatch aut = pastureStatusEngine.autoUpdateStatusTickByHoldUntil(pasture, plan);
                        if (!aut.isEmpty()) {
                            pastureService.applyPatch(pasture.getPk(), aut);
                            applyLocal(pasture, aut);
                        }
                        PastureStatus status = pastureStatusEngine.deriveEffectiveStatus(pasture, eta);
                        pasture.setStatus(status.name());
                        return buildRotationSemaphoreItemDTO(pasture, plan);
                    })
                    .sorted((a, b) -> {
                        Integer etaA = a.getEtaOpenDays() != null ? a.getEtaOpenDays() : Integer.MAX_VALUE;
                        Integer etaB = b.getEtaOpenDays() != null ? b.getEtaOpenDays() : Integer.MAX_VALUE;
                        return etaA.compareTo(etaB);
                    })
                    .collect(Collectors.toList());

            lambdaContext.logInfo(LogType.PROCESSOR, "Success building rotation semaphore farmId: " + farmId);
            return Optional.of(items);

        } catch (ServiceException e) {
            String logMsg = String.format(
                    "Failed to build rotation semaphore for farmId=%s: %s",
                    farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.PROCESSOR, logMsg);
            throw new ProcessingException("Failed to build rotation semaphore", e);
        } catch (Exception e) {
            String logMsg = String.format(
                    "Unexpected error building rotation semaphore for farmId=%s: %s",
                    farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.PROCESSOR, logMsg);
            throw new ProcessingException("Unexpected error building rotation semaphore", e);
        }
    }

    private Plan findPlanForSpecies(List<Plan> plans, String species) {
        lambdaContext.logInfo(LogType.PROCESSOR, "Finding plan for species: " + species);
        return plans.stream()
                .filter(plan -> species != null && species.contains(plan.getSpecies()))
                .findFirst()
                .orElse(null);
    }

    private RotationSemaphoreItemDTO buildRotationSemaphoreItemDTO(Pasture pasture, Plan plan) {
        RotationSemaphoreItemDTO dto = new RotationSemaphoreItemDTO();
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        Integer etaOpenDays = EtaCalculator.etaOpenDays(pasture, plan);

        dto.setId(pasture.getId());
        dto.setFarmId(pasture.getFarmId());
        dto.setPastureId(pasture.getId());
        dto.setName(pasture.getName());
        dto.setSpecies(pasture.getSpecies());
        dto.setStatus(pasture.getStatus());
        dto.setSubstatus(pasture.getSubstatus());
        dto.setBlocked(pasture.getGsi2pk() != null && pasture.getGsi2pk().contains("true"));
        dto.setBlockReason(pasture.getBlockReason());
        dto.setEtaOpenDays(etaOpenDays);
        dto.setReadyAt(etaOpenDays != null ? now.plusDays(etaOpenDays.longValue()).toString() : null);
        dto.setHoldUntil(pasture.getHoldUntil());
        dto.setLastUseAt(pasture.getLastUseAt());
        dto.setCurrentHeightCm(pasture.getCurrentHeightCm());
        dto.setAreaHa(pasture.getAreaHa());
        dto.setNotes(pasture.getNotes());

        if (plan != null && plan.getRules() != null) {
            dto.setDaysRest(plan.getRules().getRestDaysMin());
            dto.setEntryHeightCm(plan.getRules().getEntryHeightCm());
            dto.setExitResidualCm(plan.getRules().getExitResidualCm());
            dto.setRestDaysMin(plan.getRules().getRestDaysMin());
            dto.setCutIntervalDays(plan.getRules().getCutIntervalDays());
            dto.setWarn(plan.getNotes());
        }
        return dto;
    }
}
