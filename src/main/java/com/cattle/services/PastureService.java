package com.cattle.services;

import com.cattle.entities.Pasture;
import com.cattle.events.EntityPatch;
import com.cattle.repository.PastureRepository;
import com.cattle.config.LambdaContext;
import com.cattle.exceptions.ServiceException;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.enums.LogType;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PastureService {
    private final PastureRepository pastureRepository;
    private final LambdaContext lambdaContext;

    public Optional<List<Pasture>> getPastures(String farmId) {

        lambdaContext.logInfo(LogType.SERVICE, "Getting pastures for farmId: " + farmId);

        if (farmId == null || farmId.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo farmId es requerido");
        }

        try {
            lambdaContext.logInfo(LogType.SERVICE, "farm#" + farmId);
            return pastureRepository.findPastures2(farmId);
        } catch (RepositoryException e) {
            String logMsg = String.format("Repository error getting pastures for farmId=%s: %s",
                    farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.SERVICE, logMsg);
            throw new ServiceException("Repository error getting pastures", e);
        } catch (Exception e) {
            String logMsg = String.format("Unexpected error getting pastures for farmId=%s: %s",
                    farmId, e.getMessage()
            );
            lambdaContext.logException(LogType.SERVICE, logMsg);
            throw new ProcessingException("Unexpected error getting pastures", e);
        }
    }

    public void applyPatch(String pk, String sk, EntityPatch patch) {
        if (Strings.isBlank(pk) || pk.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo pk es requerido");
        }
        if (Objects.isNull(patch) || patch.isEmpty()) {
            throw new IllegalArgumentException("El campo patch es requerido y no puede estar vacío");
        }

        try {
            lambdaContext.logInfo(LogType.SERVICE, "Applying patch to pasture with pk: " + pk);
            pastureRepository.applyPatch(pk, sk, patch);
        } catch (RepositoryException e) {
            String logMsg = String.format("Repository error applying patch to pasture with pk=%s: %s",
                    pk, e.getMessage()
            );
            lambdaContext.logException(LogType.SERVICE, logMsg);
            throw new ServiceException("Repository error applying patch to pasture", e);
        } catch (Exception e) {
            String logMsg = String.format("Unexpected error applying patch to pasture with pk=%s: %s",
                    pk, e.getMessage()
            );
            lambdaContext.logException(LogType.SERVICE, logMsg);
            throw new ProcessingException("Unexpected error applying patch to pasture", e);
        }
    }

}
