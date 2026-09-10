package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.enums.SiteSettingValueType;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catálogo de claves de configuración de negocio por sitio (categoría C de la
 * épica EP-20260909, Fase 3). Se carga desde {@code site-settings.yml} al crear
 * el bean; si una definición es inconsistente (clave duplicada, tipo inválido o
 * {@code default} incoherente con el tipo) el arranque falla.
 *
 * <p>El valor vigente vive en {@code SiteSettingItem}; este catálogo aporta el
 * tipo, la etiqueta, el grupo, los límites y el valor por defecto.
 */
@Service
public class SiteSettingsCatalog {

    private static final String RESOURCE = "site-settings.yml";

    private final String version;
    private final Map<String, Definition> definitions;

    public SiteSettingsCatalog(LambdaContext lambdaContext) {
        Map<String, Object> root = loadYaml();
        this.version = String.valueOf(root.getOrDefault("version", "0"));
        this.definitions = parse(root);
        lambdaContext.logInfo(LogType.SERVICE,
                "Configuración de negocio cargada: " + definitions.keySet() + " (version " + version + ")");
    }

    /** Definiciones en el orden declarado en el YAML. */
    public List<Definition> list() {
        return new ArrayList<>(definitions.values());
    }

    public Optional<Definition> find(String key) {
        return Optional.ofNullable(definitions.get(key));
    }

    public boolean contains(String key) {
        return definitions.containsKey(key);
    }

    public String getVersion() {
        return version;
    }

    // ------------------------------------------------------------------------
    // Carga
    // ------------------------------------------------------------------------

    private Map<String, Object> loadYaml() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso " + RESOURCE);
            }
            Map<String, Object> root = new Yaml().load(in);
            if (root == null || root.get("settings") == null) {
                throw new IllegalStateException(RESOURCE + " no define 'settings'");
            }
            return root;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer " + RESOURCE + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Definition> parse(Map<String, Object> root) {
        List<Map<String, Object>> raw = (List<Map<String, Object>>) root.get("settings");
        if (raw.isEmpty()) {
            throw new IllegalStateException(RESOURCE + " no tiene entradas en 'settings'");
        }

        Map<String, Definition> parsed = new LinkedHashMap<>();
        for (Map<String, Object> entry : raw) {
            String key = (String) entry.get("key");
            if (key == null || key.isBlank()) {
                throw new IllegalStateException("Hay una entrada de settings sin 'key'");
            }
            if (parsed.containsKey(key)) {
                throw new IllegalStateException("Clave de setting duplicada: " + key);
            }

            SiteSettingValueType type = parseType(key, entry.get("type"));
            String label = (String) entry.get("label");
            if (label == null || label.isBlank()) {
                throw new IllegalStateException("La clave '" + key + "' no tiene 'label'");
            }
            Object defaultValue = coerce(key, type, entry.get("default"));
            Double min = asDouble(entry.get("min"));
            Double max = asDouble(entry.get("max"));

            parsed.put(key, new Definition(key, type, defaultValue, label,
                    (String) entry.getOrDefault("group", "general"), min, max));
        }
        return Collections.unmodifiableMap(parsed);
    }

    private SiteSettingValueType parseType(String key, Object rawType) {
        if (rawType == null) {
            throw new IllegalStateException("La clave '" + key + "' no tiene 'type'");
        }
        try {
            return SiteSettingValueType.valueOf(String.valueOf(rawType).trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("La clave '" + key + "' tiene un 'type' inválido: " + rawType);
        }
    }

    /** Convierte y valida el valor contra el tipo declarado. */
    public static Object coerce(String key, SiteSettingValueType type, Object value) {
        if (value == null) {
            throw new IllegalStateException("La clave '" + key + "' no tiene valor por defecto");
        }
        switch (type) {
            case NUMBER:
                if (value instanceof Number n) {
                    return n.doubleValue();
                }
                try {
                    return Double.parseDouble(String.valueOf(value));
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("La clave '" + key + "' espera un número y recibió: " + value);
                }
            case BOOLEAN:
                if (value instanceof Boolean b) {
                    return b;
                }
                String s = String.valueOf(value).trim().toLowerCase();
                if (s.equals("true") || s.equals("false")) {
                    return Boolean.parseBoolean(s);
                }
                throw new IllegalStateException("La clave '" + key + "' espera un booleano y recibió: " + value);
            case STRING:
                return String.valueOf(value);
            case JSON:
            default:
                return value; // se serializa aguas abajo
        }
    }

    private static Double asDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    /** Definición inmutable de una clave de configuración de negocio. */
    public static final class Definition {
        private final String key;
        private final SiteSettingValueType type;
        private final Object defaultValue;
        private final String label;
        private final String group;
        private final Double min;
        private final Double max;

        public Definition(String key, SiteSettingValueType type, Object defaultValue, String label,
                          String group, Double min, Double max) {
            this.key = key;
            this.type = type;
            this.defaultValue = defaultValue;
            this.label = label;
            this.group = group;
            this.min = min;
            this.max = max;
        }

        public String getKey() {
            return key;
        }

        public SiteSettingValueType getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String getLabel() {
            return label;
        }

        public String getGroup() {
            return group;
        }

        public Double getMin() {
            return min;
        }

        public Double getMax() {
            return max;
        }
    }
}
