package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureDTO;
import com.cattle.dtos.RotationSemaphoreItemDTO;
import com.cattle.entities.Pasture;
import com.cattle.services.PastureService;
import com.cattle.exceptions.ServiceException;
import com.cattle.exceptions.ProcessingException;
import com.cattle.enums.LogType;
import com.cattle.mapper.PasturesMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PastureProcessor {
    private final PastureService pastureService;
    private final LambdaContext lambdaContext;
    private final PasturesMapper pasturesMapper;

    public PastureProcessor(PastureService pastureService, LambdaContext lambdaContext, PasturesMapper pasturesMapper) {
        this.pastureService = pastureService;
        this.lambdaContext = lambdaContext;
        this.pasturesMapper = pasturesMapper;
    }

    public Optional<List<RotationSemaphoreItemDTO>> listPastures(String farmId) {

        try {
            lambdaContext.logInfo(LogType.PROCESSOR, "farm#" + farmId);
            Optional<List<Pasture>> entitiesOpt = pastureService.getPastures(farmId);
            if (entitiesOpt.isPresent()) {
                List<PastureDTO> dtos = pasturesMapper.toDTOList(entitiesOpt.get());
                lambdaContext.logInfo(LogType.PROCESSOR, "success");
                //return Optional.of(dtos);
                return Optional.of(new ArrayList<RotationSemaphoreItemDTO>());
            } else {
                return Optional.empty();
            }
        } catch (ServiceException e) {
            String logMsg = String.format("Failed to list pastures for farmId=%s: %s",
                    farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.PROCESSOR, logMsg);
            throw new ProcessingException("Failed to list pastures", e);
        } catch (Exception e) {
            String logMsg = String.format("Unexpected error listing pastures for farmId=%s: %s",
                farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.PROCESSOR, logMsg);
            throw new ProcessingException("Unexpected error listing pastures", e);
        }
    }
}
