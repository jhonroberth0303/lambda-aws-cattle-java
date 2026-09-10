package com.cattle.processor;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.settings.SiteSettingDTO;
import com.cattle.dtos.settings.SiteSettingUpdateRequestDTO;
import com.cattle.dtos.settings.SiteSettingsResponseDTO;
import com.cattle.entities.SiteSettingItem;
import com.cattle.enums.LogType;
import com.cattle.enums.SiteSettingValueType;
import com.cattle.exceptions.NotFoundException;
import com.cattle.services.SiteSettingService;
import com.cattle.services.SiteSettingsCatalog;
import com.cattle.services.SiteSettingsCatalog.Definition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orquesta la configuración de negocio por sitio: mezcla el catálogo
 * ({@code site-settings.yml}) con los valores almacenados en
 * {@code SiteSettingItem}. EP-20260909, Fase 3.
 */
@Component
public class SiteSettingsProcessor {

    private static final String DEFAULT_CHANGE_REASON = "Actualizacion manual desde endpoint settings";
    private static final String SOURCE_STORED = "STORED";
    private static final String SOURCE_DEFAULT = "DEFAULT";

    private final SiteSettingsCatalog catalog;
    private final SiteSettingService siteSettingService;
    private final LambdaContext lambdaContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SiteSettingsProcessor(SiteSettingsCatalog catalog, SiteSettingService siteSettingService,
                                 LambdaContext lambdaContext) {
        this.catalog = catalog;
        this.siteSettingService = siteSettingService;
        this.lambdaContext = lambdaContext;
    }

    public SiteSettingsResponseDTO getAll(String siteId) {
        validateSiteId(siteId);
        lambdaContext.logInfo(LogType.PROCESSOR, "Listing site settings for site: " + siteId);

        Map<String, SiteSettingItem> stored = siteSettingService.findAllCurrent(siteId).stream()
                .filter(it -> it.getSettingKey() != null)
                .collect(Collectors.toMap(SiteSettingItem::getSettingKey, Function.identity(), (a, b) -> a));

        List<SiteSettingDTO> settings = new ArrayList<>();
        for (Definition def : catalog.list()) {
            settings.add(toDTO(siteId, def, stored.get(def.getKey())));
        }

        return SiteSettingsResponseDTO.builder()
                .siteId(siteId)
                .catalogVersion(catalog.getVersion())
                .settings(settings)
                .build();
    }

    public SiteSettingDTO getByKey(String siteId, String key) {
        validateSiteId(siteId);
        Definition def = requireDefinition(key);
        SiteSettingItem stored = siteSettingService.findCurrent(siteId, key).orElse(null);
        return toDTO(siteId, def, stored);
    }

    public SiteSettingDTO updateByKey(String siteId, String key, SiteSettingUpdateRequestDTO request) {
        validateSiteId(siteId);
        Definition def = requireDefinition(key);
        if (request == null || request.getValue() == null) {
            throw new IllegalArgumentException("El valor 'value' es obligatorio");
        }

        Object normalized = normalizeAndValidate(def, request.getValue());
        String changeReason = request.getChangeReason() != null && !request.getChangeReason().isBlank()
                ? request.getChangeReason()
                : DEFAULT_CHANGE_REASON;

        lambdaContext.logInfo(LogType.PROCESSOR, "Updating site setting " + key + " for site: " + siteId);
        SiteSettingItem item = siteSettingService.upsertSetting(
                siteId, key, def.getType(), normalized, request.getUpdatedBy(), changeReason);

        return toDTO(siteId, def, item);
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private Definition requireDefinition(String key) {
        return catalog.find(key)
                .orElseThrow(() -> new NotFoundException("Clave de configuración desconocida: " + key));
    }

    private Object normalizeAndValidate(Definition def, Object rawValue) {
        SiteSettingValueType type = def.getType();
        switch (type) {
            case NUMBER: {
                double number = toNumber(def.getKey(), rawValue);
                if (def.getMin() != null && number < def.getMin()) {
                    throw new IllegalArgumentException(
                            "El valor de " + def.getKey() + " no puede ser menor que " + def.getMin());
                }
                if (def.getMax() != null && number > def.getMax()) {
                    throw new IllegalArgumentException(
                            "El valor de " + def.getKey() + " no puede ser mayor que " + def.getMax());
                }
                return number;
            }
            case BOOLEAN:
                if (rawValue instanceof Boolean b) {
                    return b;
                }
                throw new IllegalArgumentException("El valor de " + def.getKey() + " debe ser booleano");
            case STRING:
                return String.valueOf(rawValue);
            case JSON:
            default:
                return toJsonString(def.getKey(), rawValue);
        }
    }

    private double toNumber(String key, Object rawValue) {
        if (rawValue instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(rawValue));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El valor de " + key + " debe ser numérico");
        }
    }

    private String toJsonString(String key, Object rawValue) {
        if (rawValue instanceof String s) {
            return s;
        }
        try {
            return objectMapper.writeValueAsString(rawValue);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("El valor de " + key + " no es un JSON válido");
        }
    }

    private SiteSettingDTO toDTO(String siteId, Definition def, SiteSettingItem stored) {
        Object value = stored != null ? extractValue(stored) : def.getDefaultValue();
        return SiteSettingDTO.builder()
                .siteId(siteId)
                .key(def.getKey())
                .valueType(def.getType().name())
                .value(value)
                .defaultValue(def.getDefaultValue())
                .source(stored != null ? SOURCE_STORED : SOURCE_DEFAULT)
                .label(def.getLabel())
                .group(def.getGroup())
                .min(def.getMin())
                .max(def.getMax())
                .version(stored != null ? stored.getVersion() : null)
                .updatedAt(stored != null ? stored.getUpdatedAt() : null)
                .updatedBy(stored != null ? stored.getUpdatedBy() : null)
                .build();
    }

    private Object extractValue(SiteSettingItem item) {
        SiteSettingValueType type;
        try {
            type = SiteSettingValueType.valueOf(String.valueOf(item.getValueType()).toUpperCase());
        } catch (IllegalArgumentException e) {
            type = SiteSettingValueType.STRING;
        }
        switch (type) {
            case NUMBER:
                return item.getValueNumber();
            case BOOLEAN:
                return item.getValueBoolean();
            case JSON:
                return item.getValueJson();
            case STRING:
            default:
                return item.getValueString();
        }
    }

    private void validateSiteId(String siteId) {
        if (siteId == null || siteId.isBlank()) {
            throw new IllegalArgumentException("El siteId es obligatorio");
        }
    }
}
