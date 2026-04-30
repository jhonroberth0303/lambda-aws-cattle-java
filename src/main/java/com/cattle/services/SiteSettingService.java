package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.SiteSettingItem;
import com.cattle.enums.LogType;
import com.cattle.enums.SiteSettingValueType;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;
    private final LambdaContext lambdaContext;

    public SiteSettingService(SiteSettingRepository siteSettingRepository, LambdaContext lambdaContext) {
        this.siteSettingRepository = siteSettingRepository;
        this.lambdaContext = lambdaContext;
    }

    public Optional<SiteSettingItem> findCurrent(String siteId, String settingKey) {
        try {
            return siteSettingRepository.findCurrent(siteId, settingKey);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to fetch current SiteSetting: " + e.getMessage());
            throw new ServiceException("Failed to fetch current SiteSetting", e);
        }
    }

    public SiteSettingItem upsertNumberSetting(String siteId, String settingKey, Double valueNumber,
                                               String updatedBy, String changeReason) {
        try {
            String now = Instant.now().toString();
            Optional<SiteSettingItem> currentOpt = siteSettingRepository.findCurrent(siteId, settingKey);

            SiteSettingItem current = buildCurrentItem(currentOpt.orElse(null), siteId, settingKey, valueNumber,
                    updatedBy, changeReason, now);

            siteSettingRepository.saveCurrent(current)
                    .orElseThrow(() -> new ServiceException("Failed to persist current SiteSetting"));

            SiteSettingItem historySnapshot = buildHistorySnapshot(current, now);
            siteSettingRepository.saveHistorySnapshot(historySnapshot)
                    .orElseThrow(() -> new ServiceException("Failed to persist SiteSetting history snapshot"));

            return current;
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to upsert SiteSetting: " + e.getMessage());
            throw new ServiceException("Failed to upsert SiteSetting", e);
        }
    }

    private SiteSettingItem buildCurrentItem(SiteSettingItem existing, String siteId, String settingKey,
                                             Double valueNumber, String updatedBy, String changeReason,
                                             String now) {
        return SiteSettingItem.builder()
                .pk(SiteSettingItem.buildPk(siteId))
                .sk(SiteSettingItem.buildCurrentSk(settingKey))
                .gsi1pk(SiteSettingItem.buildGsi1Pk(settingKey))
                .gsi1sk(SiteSettingItem.buildCurrentGsi1Sk(siteId))
                .siteId(siteId)
                .settingKey(settingKey)
                .valueType(SiteSettingValueType.NUMBER.name())
                .valueNumber(valueNumber)
                .valueString(null)
                .valueBoolean(null)
                .valueJson(null)
                .version(existing == null ? 1 : existing.getVersion() + 1)
                .active(true)
                .effectiveFrom(now)
                .effectiveTo(null)
                .createdAt(existing == null ? now : existing.getCreatedAt())
                .updatedAt(now)
                .updatedBy(updatedBy)
                .changeReason(changeReason)
                .build();
    }

    private SiteSettingItem buildHistorySnapshot(SiteSettingItem current, String now) {
        return SiteSettingItem.builder()
                .pk(current.getPk())
                .sk(SiteSettingItem.buildHistorySk(current.getSettingKey(), now))
                .gsi1pk(current.getGsi1pk())
                .gsi1sk(SiteSettingItem.buildHistoryGsi1Sk(current.getSiteId(), now))
                .siteId(current.getSiteId())
                .settingKey(current.getSettingKey())
                .valueType(current.getValueType())
                .valueNumber(current.getValueNumber())
                .valueString(current.getValueString())
                .valueBoolean(current.getValueBoolean())
                .valueJson(current.getValueJson())
                .version(current.getVersion())
                .active(current.getActive())
                .effectiveFrom(now)
                .effectiveTo(null)
                .createdAt(now)
                .updatedAt(now)
                .updatedBy(current.getUpdatedBy())
                .changeReason(current.getChangeReason())
                .build();
    }
}