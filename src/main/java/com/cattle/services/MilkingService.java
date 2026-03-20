package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.MilkingRecord;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.MilkingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MilkingService {

    private final MilkingRepository milkingRepository;
    private final LambdaContext lambdaContext;

    public MilkingService(MilkingRepository milkingRepository, LambdaContext lambdaContext) {
        this.lambdaContext = lambdaContext;
        this.milkingRepository = milkingRepository;
    }

    public Optional<MilkingRecord> save(MilkingRecord milkingRecord) {
        try {
            return milkingRepository.save(milkingRecord);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to save farmMilking: " + e.getMessage());
            throw new ServiceException("Failed to save farmMilking", e);
        }
    }

    public Optional<List<MilkingRecord>> getMilkingByPk(String pk) {

        try {
            return milkingRepository.getMilkingByPk(pk);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to fetch farmMilking by ID: " + e.getMessage());
            throw new ServiceException("Failed to fetch farmMilking by ID", e);
        }
    }

    public Optional<List<MilkingRecord>> getMilkingByBovineAndLactation(Integer bovineId, String lactationNumber) {
        try {
            return milkingRepository.getMilkingByBovineAndLactation(bovineId, lactationNumber);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to fetch milking by bovine and lactation: " + e.getMessage());
            throw new ServiceException("Failed to fetch milking by bovine and lactation", e);
        }
    }

}
