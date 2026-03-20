package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.enums.LogType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.BovineRepository;
import com.cattle.repository.CounterRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class BovineService {

    private static final String TABLE_BOVINES = System.getenv("TABLE_BOVINES");
    private static final ZoneId ZONE_ID = ZoneId.of(System.getenv().getOrDefault("APP_TIMEZONE", "America/Bogota"));
    private final BovineRepository bovineRepository;
    private final LambdaContext lambdaContext;
    private  final CounterRepository counterRepository;

    public BovineService(BovineRepository bovineRepository, LambdaContext lambdaContext,
                         CounterRepository counterRepository) {
        this.lambdaContext = lambdaContext;
        this.bovineRepository = bovineRepository;
        this.counterRepository = counterRepository;
    }

    public Optional<List<BovineIdentityItem>> findAll() {
        try {
            return bovineRepository.findAll();
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to fetch all bovines: " + e.getMessage());
            throw new ServiceException("Failed to fetch bovines", e);
        }
    }

    public Optional<BovineIdentityItem> findById(Integer id) {

        try {
            lambdaContext.logInfo(LogType.SERVICE, "Received request to find bovine with ID: " + id);
            return bovineRepository.findById(id);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to fetch bovine by ID: " + e.getMessage());
            throw new ServiceException("Failed to fetch bovine by ID", e);
        }
    }

    public Optional<BovineIdentityItem> save(BovineIdentityItem bovineIdentityItem) {
        try {
            String counter = counterRepository.getNextId(TABLE_BOVINES);
            bovineIdentityItem.setBovineId(Integer.parseInt(counter));
            bovineIdentityItem.setPk("BOVINE#" + counter);
            bovineIdentityItem.setSk("IDENTITY");
            bovineIdentityItem.setGsi1pk("IDENTITY");
            bovineIdentityItem.setGsi1sk("BOVINE#" + counter);
            String localDate = getLocalDate();
            bovineIdentityItem.setCreatedAt(localDate);
            bovineIdentityItem.setUpdatedAt(localDate);
            lambdaContext.logInfo(LogType.SERVICE, "Generated new ID for bovine: " + counter);
            return bovineRepository.save(bovineIdentityItem);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to save bovine: " + e.getMessage());
            throw new ServiceException("Failed to save bovine", e);
        }
    }

    private static String getLocalDate() {
        return ZonedDateTime.now(ZONE_ID).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public Optional<BovineIdentityItem> update(BovineIdentityItem bovineIdentityItem) {
        try {
            bovineRepository.findById(bovineIdentityItem.getBovineId())
                    .orElseThrow(() -> new ServiceException("Bovine not found with ID: " + bovineIdentityItem.getBovineId()));
            bovineIdentityItem.setPk("BOVINE#" + bovineIdentityItem.getBovineId());
            bovineIdentityItem.setSk("IDENTITY");
            bovineIdentityItem.setGsi1pk("IDENTITY");
            bovineIdentityItem.setGsi1sk("BOVINE#" + bovineIdentityItem.getBovineId());
            bovineIdentityItem.setUpdatedAt(getLocalDate());
            lambdaContext.logInfo(LogType.SERVICE, "Updating bovine with ID: " + bovineIdentityItem.getBovineId());
            return bovineRepository.update(bovineIdentityItem);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to update bovine: " + e.getMessage());
            throw new ServiceException("Failed to update bovine", e);
        }
    }

}
