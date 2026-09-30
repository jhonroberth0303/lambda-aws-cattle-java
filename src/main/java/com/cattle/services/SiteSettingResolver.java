package com.cattle.services;

import com.cattle.entities.SiteSettingItem;
import com.cattle.enums.SiteSettingValueType;
import com.cattle.services.SiteSettingsCatalog.Definition;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve el valor vigente de las settings de un sitio: el almacenado en
 * {@code SiteSettingItem} o, si no hay, el {@code default} de {@code site-settings.yml}.
 * Punto único para leer settings desde la lógica de negocio (no solo desde el endpoint).
 */
@Service
public class SiteSettingResolver {

    private final SiteSettingsCatalog catalog;
    private final SiteSettingService siteSettingService;

    public SiteSettingResolver(SiteSettingsCatalog catalog, SiteSettingService siteSettingService) {
        this.catalog = catalog;
        this.siteSettingService = siteSettingService;
    }

    /** Valores vigentes de todas las claves del catálogo para el sitio. */
    public Map<String, Object> resolveAll(String siteId) {
        Map<String, SiteSettingItem> stored = siteSettingService.findAllCurrent(siteId).stream()
                .filter(it -> it.getSettingKey() != null)
                .collect(Collectors.toMap(SiteSettingItem::getSettingKey, Function.identity(), (a, b) -> a));

        Map<String, Object> values = new HashMap<>();
        for (Definition def : catalog.list()) {
            SiteSettingItem item = stored.get(def.getKey());
            Object value = item != null ? extractValue(item) : null;
            values.put(def.getKey(), value != null ? value : def.getDefaultValue());
        }
        return values;
    }

    /** Extrae el valor tipado de un ítem almacenado según su {@code valueType}. */
    public static Object extractValue(SiteSettingItem item) {
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
}
