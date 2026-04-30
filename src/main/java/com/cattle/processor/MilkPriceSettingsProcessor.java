package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.MilkPriceSettingDTO;
import com.cattle.dtos.MilkPriceSettingUpdateRequestDTO;
import com.cattle.entities.SiteSettingItem;
import com.cattle.enums.LogType;
import com.cattle.services.SiteSettingService;
import org.springframework.stereotype.Component;

@Component
public class MilkPriceSettingsProcessor {

    public static final String MILK_PRICE_PER_LITER = "MILK_PRICE_PER_LITER";
    private static final String DEFAULT_CHANGE_REASON = "Actualizacion manual desde endpoint milk-price";

    private final SiteSettingService siteSettingService;
    private final LambdaContext lambdaContext;

    public MilkPriceSettingsProcessor(SiteSettingService siteSettingService, LambdaContext lambdaContext) {
        this.siteSettingService = siteSettingService;
        this.lambdaContext = lambdaContext;
    }

    public MilkPriceSettingDTO getMilkPrice(String siteId) {
        validateSiteId(siteId);
        lambdaContext.logInfo(LogType.PROCESSOR, "Fetching milk price setting for site: " + siteId);
        return siteSettingService.findCurrent(siteId, MILK_PRICE_PER_LITER)
                .map(this::toMilkPriceSettingDTO)
                .orElseGet(() -> MilkPriceSettingDTO.builder()
                        .siteId(siteId)
                        .milkPricePerLiter(0.0)
                        .updatedAt(null)
                        .updatedBy(null)
                        .build());
    }

    public MilkPriceSettingDTO updateMilkPrice(String siteId, MilkPriceSettingUpdateRequestDTO request) {
        validateSiteId(siteId);
        validateRequest(request);
        lambdaContext.logInfo(LogType.PROCESSOR, "Updating milk price setting for site: " + siteId);

        SiteSettingItem item = siteSettingService.upsertNumberSetting(
                siteId,
                MILK_PRICE_PER_LITER,
                request.getMilkPricePerLiter(),
                request.getUpdatedBy(),
                DEFAULT_CHANGE_REASON
        );

        return toMilkPriceSettingDTO(item);
    }

    private MilkPriceSettingDTO toMilkPriceSettingDTO(SiteSettingItem item) {
        return MilkPriceSettingDTO.builder()
                .siteId(item.getSiteId())
                .milkPricePerLiter(item.getValueNumber())
                .updatedAt(item.getUpdatedAt())
                .updatedBy(item.getUpdatedBy())
                .build();
    }

    private void validateSiteId(String siteId) {
        if (siteId == null || siteId.isBlank()) {
            throw new IllegalArgumentException("El siteId es obligatorio");
        }
    }

    private void validateRequest(MilkPriceSettingUpdateRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("El payload de actualización es obligatorio");
        }
        if (request.getMilkPricePerLiter() == null) {
            throw new IllegalArgumentException("El valor milkPricePerLiter es obligatorio");
        }
        if (request.getMilkPricePerLiter() < 0) {
            throw new IllegalArgumentException("El valor milkPricePerLiter no puede ser negativo");
        }
    }
}