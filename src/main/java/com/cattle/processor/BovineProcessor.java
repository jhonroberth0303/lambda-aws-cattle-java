package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineDTO;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.enums.LogType;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.ServiceException;
import com.cattle.mapper.BovinesMapperImpl;
import com.cattle.services.BovineService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BovineProcessor {

    private final BovineService bovineService;
    private final BovinesMapperImpl bovinesMapperImpl;
    private final LambdaContext lambdaContext;

    public BovineProcessor(BovineService bovineService, BovinesMapperImpl bovinesMapperImpl,
                           LambdaContext lambdaContext) {
        this.bovineService = bovineService;
        this.bovinesMapperImpl = bovinesMapperImpl;
        this.lambdaContext = lambdaContext;
    }

    public List<BovineDTO> findAll() {

        try {
            Optional<List<BovineIdentityItem>> bovinesList = bovineService.findAll();
            if (bovinesList.isEmpty()) {
                lambdaContext.logInfo(LogType.PROCESSOR, "No bovines found");
                return List.of();
            }
            lambdaContext.logInfo(LogType.PROCESSOR, "Bovines were found: " + bovinesList.get().size());
            return bovinesList.get().stream().map(bovinesMapperImpl::toDTO).toList();

        } catch (ServiceException e) {
            lambdaContext.logException(LogType.PROCESSOR, "Failed to fetch all bovines: " + e.getMessage());
            throw new ProcessingException("Failed to fetch all bovines:", e);
        }
    }

    public Optional<BovineDTO> findById(Integer id) {
        try {
            lambdaContext.logInfo(LogType.PROCESSOR, "Received request to find bovine with ID: " + id);
            Optional<BovineIdentityItem> bovine = bovineService.findById(id);
            if (bovine.isPresent()) {
                lambdaContext.logInfo(LogType.PROCESSOR, "Bovine was found with ID: " + id);
                return bovine.map(bovinesMapperImpl::toDTO);
            } else {
                lambdaContext.logInfo(LogType.PROCESSOR, "No bovine found with ID: " + id);
                return Optional.empty();
            }
        } catch (ServiceException e) {
            lambdaContext.logException(LogType.PROCESSOR, "Failed to fetch bovine by ID: " + e.getMessage());
            throw new ProcessingException("Failed finding by id", e);
        }
    }

    public Optional<BovineDTO> save(BovineDTO bovineDTO) {
        try {
            BovineIdentityItem bovineIdentityItem = bovinesMapperImpl.toEntity(bovineDTO);
            Optional<BovineIdentityItem> savedBovine = bovineService.save(bovineIdentityItem);
            return savedBovine.map(bovinesMapperImpl::toDTO);
        } catch (ServiceException e) {
            lambdaContext.logException(LogType.PROCESSOR, "Failed to save bovine: " + e.getMessage());
            throw new ProcessingException("Failed to save bovine", e);
        }
    }

    public Optional<BovineDTO> update(BovineDTO bovineDTO) {
        try {
            BovineIdentityItem bovineIdentityItem = bovinesMapperImpl.toEntity(bovineDTO);
            lambdaContext.logInfo(LogType.PROCESSOR, "Updating bovine with ID: " + bovineIdentityItem.getBovineId());
            Optional<BovineIdentityItem> updatedBovine = bovineService.update(bovineIdentityItem);
            lambdaContext.logInfo(LogType.PROCESSOR, "Bovine updated with ID: " + bovineIdentityItem.getBovineId());
            return updatedBovine.map(bovinesMapperImpl::toDTO);
        } catch (ServiceException e) {
            lambdaContext.logException(LogType.PROCESSOR, "Failed to update bovine: " + e.getMessage());
            throw new ProcessingException("Failed to update bovine", e);
        }
    }

}