package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.forms.EventFormSchemasDTO;
import com.cattle.enums.LogType;
import com.cattle.forms.EventFormSchema;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Carga y sirve los esquemas de formulario de eventos desde {@code event-forms.yml}
 * (EP-20260909, Fase 2). Mismo patrón que {@link CatalogService}: la respuesta se
 * construye una vez al crear el bean y es inmutable.
 *
 * <p>Guardia anti-deriva: si un dominio declara {@code enum:} sus claves deben
 * coincidir EXACTAMENTE con las constantes de ese enum Java (ni de más ni de
 * menos); el arranque falla en caso contrario. Los 22 tipos de
 * {@code BovineEventType} están migrados a schema-driven (EP-20260909, Fase 2).
 */
@Service
public class EventFormCatalog {

    private static final String RESOURCE = "event-forms.yml";
    private static final String BOVINE_EVENTS_DOMAIN = "bovine-events";
    private static final Set<String> KNOWN_TYPES = Set.of("text", "number", "textarea", "date");

    private final String version;
    private final Map<String, Map<String, EventFormSchema>> domains;
    private final String hash;
    private final String etag;

    public EventFormCatalog(LambdaContext lambdaContext) {
        Map<String, Object> root = loadYaml();
        this.version = String.valueOf(root.getOrDefault("version", "0"));
        this.domains = parse(root);
        this.hash = hash(this.domains);
        this.etag = "\"" + version + "-" + hash + "\"";
        lambdaContext.logInfo(LogType.SERVICE, "Esquemas de formulario cargados: "
                + describe() + " (etag " + etag + ")");
    }

    /** Esquema de un tipo de evento bovino ya migrado, o vacío. */
    public Optional<EventFormSchema> findBovineEvent(String code) {
        return Optional.ofNullable(domains.getOrDefault(BOVINE_EVENTS_DOMAIN, Map.of()).get(code));
    }

    /** ¿Hay un dominio con ese nombre en {@code event-forms.yml}? */
    public boolean hasDomain(String domain) {
        return domains.containsKey(domain);
    }

    public String getEtag() {
        return etag;
    }

    /** Respuesta de {@code GET /catalogs/{domain}/schema}, o {@code null} si no existe. */
    public EventFormSchemasDTO getDomainSchemas(String domain) {
        Map<String, EventFormSchema> schemas = domains.get(domain);
        if (schemas == null) {
            return null;
        }
        return EventFormSchemasDTO.builder()
                .version(version)
                .hash(hash)
                .domain(domain)
                .schemas(schemas)
                .build();
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
            if (root == null || root.get("domains") == null) {
                throw new IllegalStateException(RESOURCE + " no define 'domains'");
            }
            return root;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer " + RESOURCE + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, EventFormSchema>> parse(Map<String, Object> root) {
        Map<String, Object> domainsRaw = (Map<String, Object>) root.get("domains");
        Map<String, Map<String, EventFormSchema>> parsed = new LinkedHashMap<>();

        for (Map.Entry<String, Object> domainEntry : domainsRaw.entrySet()) {
            String domainName = domainEntry.getKey();
            Map<String, Object> spec = (Map<String, Object>) domainEntry.getValue();
            String enumClass = (String) spec.get("enum");

            Map<String, EventFormSchema> schemas = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : spec.entrySet()) {
                if ("enum".equals(e.getKey())) {
                    continue;
                }
                String code = e.getKey();
                schemas.put(code, toSchema(domainName, code, (Map<String, Object>) e.getValue()));
            }
            if (schemas.isEmpty()) {
                throw new IllegalStateException("El dominio '" + domainName + "' de " + RESOURCE + " no tiene esquemas");
            }

            if (enumClass != null) {
                validateAgainstEnum(domainName, enumClass, schemas.keySet());
            }
            parsed.put(domainName, Collections.unmodifiableMap(schemas));
        }
        return Collections.unmodifiableMap(parsed);
    }

    @SuppressWarnings("unchecked")
    private EventFormSchema toSchema(String domain, String code, Map<String, Object> raw) {
        if (raw == null) {
            throw new IllegalStateException("El esquema '" + code + "' de " + domain + " está vacío");
        }
        List<Map<String, Object>> fieldsRaw = (List<Map<String, Object>>) raw.get("fields");
        if (fieldsRaw == null || fieldsRaw.isEmpty()) {
            throw new IllegalStateException("El esquema '" + code + "' no tiene 'fields'");
        }

        List<EventFormSchema.Field> fields = new ArrayList<>(fieldsRaw.size());
        Set<String> names = new LinkedHashSet<>();
        for (Map<String, Object> fieldRaw : fieldsRaw) {
            EventFormSchema.Field field = toField(code, fieldRaw);
            if (!names.add(field.getName())) {
                throw new IllegalStateException("El esquema '" + code + "' repite el campo '" + field.getName() + "'");
            }
            fields.add(field);
        }

        List<String> requireOneOf = (List<String>) raw.get("requireOneOf");
        if (requireOneOf != null) {
            for (String name : requireOneOf) {
                if (!names.contains(name)) {
                    throw new IllegalStateException(
                            "El esquema '" + code + "' referencia en requireOneOf un campo inexistente: " + name);
                }
            }
        }

        boolean appendNotes = !Boolean.FALSE.equals(raw.get("appendNotes"));
        return new EventFormSchema(code, (String) raw.get("title"), (String) raw.get("submitLabel"),
                appendNotes, requireOneOf, fields);
    }

    @SuppressWarnings("unchecked")
    private EventFormSchema.Field toField(String code, Map<String, Object> raw) {
        String name = (String) raw.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("El esquema '" + code + "' tiene un campo sin 'name'");
        }
        String label = (String) raw.get("label");
        if (label == null || label.isBlank()) {
            throw new IllegalStateException("El campo '" + name + "' del esquema '" + code + "' no tiene 'label'");
        }
        String rawType = String.valueOf(raw.getOrDefault("type", "text")).trim().toLowerCase();
        if (!KNOWN_TYPES.contains(rawType)) {
            throw new IllegalStateException("El campo '" + name + "' del esquema '" + code + "' tiene un 'type' inválido: " + rawType);
        }

        List<EventFormSchema.Option> options = null;
        List<Map<String, Object>> optionsRaw = (List<Map<String, Object>>) raw.get("options");
        if (optionsRaw != null) {
            options = new ArrayList<>(optionsRaw.size());
            for (Map<String, Object> o : optionsRaw) {
                String value = String.valueOf(o.get("value"));
                String optLabel = (String) o.getOrDefault("label", value);
                options.add(new EventFormSchema.Option(value, optLabel));
            }
        }

        return new EventFormSchema.Field(
                name,
                label,
                EventFormSchema.Field.Type.valueOf(rawType.toUpperCase()),
                Boolean.TRUE.equals(raw.get("required")),
                asDouble(raw.get("min")),
                asDouble(raw.get("max")),
                asDouble(raw.get("step")),
                asInteger(raw.get("maxLength")),
                (String) raw.get("placeholder"),
                (String) raw.get("hint"),
                (String) raw.get("minField"),
                Boolean.TRUE.equals(raw.get("calvingEstimate")),
                options);
    }

    private void validateAgainstEnum(String domain, String enumClassName, Set<String> codes) {
        Class<?> enumClass;
        try {
            enumClass = Class.forName(enumClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "El dominio '" + domain + "' de " + RESOURCE + " referencia un enum inexistente: " + enumClassName, e);
        }
        if (!enumClass.isEnum()) {
            throw new IllegalStateException("El dominio '" + domain + "' referencia una clase que no es enum: " + enumClassName);
        }
        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .map(c -> ((Enum<?>) c).name())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> unknown = new TreeSet<>(codes);
        unknown.removeAll(enumNames);
        if (!unknown.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "El dominio '%s' de %s tiene esquemas para códigos que no existen en el enum %s: %s",
                    domain, RESOURCE, enumClassName, unknown));
        }
        Set<String> missing = new TreeSet<>(enumNames);
        missing.removeAll(codes);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "El dominio '%s' de %s no tiene esquema para todos los valores del enum %s — faltan: %s",
                    domain, RESOURCE, enumClassName, missing));
        }
    }

    private String describe() {
        return domains.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().keySet())
                .collect(Collectors.joining(", "));
    }

    // ------------------------------------------------------------------------
    // Hash de contenido (determinista)
    // ------------------------------------------------------------------------

    private String hash(Map<String, Map<String, EventFormSchema>> content) {
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                    .build();
            byte[] canonical = mapper.writeValueAsBytes(content);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de esquemas de formulario: " + e.getMessage(), e);
        }
    }

    private static Double asDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private static Integer asInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }
}
