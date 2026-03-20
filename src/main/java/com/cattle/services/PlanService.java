package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.Plan;
import com.cattle.enums.LogType;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final LambdaContext lambdaContext;

    public Optional<List<Plan>> getPlans(String farmId) {

        if (farmId == null || farmId.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo farmId es requerido");
        }

        try {
            lambdaContext.logInfo(LogType.SERVICE, "farm#" + farmId);
            return planRepository.findPlans(farmId);
        } catch (RepositoryException e) {
            String logMsg = String.format("Repository error getting plans for farmId=%s: %s",
                    farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.SERVICE, logMsg);
            throw new ServiceException("Repository error getting plans", e);
        } catch (Exception e) {
            String logMsg = String.format("Unexpected error getting plans for farmId=%s: %s",
                    farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.SERVICE, logMsg);
            throw new ProcessingException("Unexpected error getting plans", e);
        }
    }
}
