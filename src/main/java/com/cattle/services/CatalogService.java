package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.catalog.CatalogDomainDTO;
import com.cattle.dtos.catalog.CatalogEntryDTO;
import com.cattle.dtos.catalog.CatalogResponseDTO;
import com.cattle.enums.LogType;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Carga y sirve los catálogos de dominio desde {@code catalog.yml}.
 *
 * <p>Épica EP-20260909, Fase 1. La respuesta es inmutable y se construye una sola
 * vez al crear el bean. Si algún dominio marcado con {@code enum:} diverge del
 * enum Java correspondiente, el arranque falla (guardia anti-deriva del backend).
 */
@Service
public class CatalogService {

    private static final String CATALOG_RESOURCE = "catalog.yml";

    private final CatalogResponseDTO catalog;
    private final String etag;

    public CatalogService(LambdaContext lambdaContext) {
        this.catalog = load();
        this.etag = "\"" + catalog.getVersion() + "-" + catalog.getHash() + "\"";
        lambdaContext.logInfo(LogType.SERVICE,
                "Catálogos cargados: " + catalog.getDomains().keySet() + " (etag " + etag + ")");
    }

    /** Respuesta completa de {@code GET /catalogs}. */
    public CatalogResponseDTO getCatalog() {
        return catalog;
    }

    /** ETag del contenido (entre comillas, listo para la cabecera HTTP). */
    public String getEtag() {
        return etag;
    }

    /** Un dominio concreto, o {@code null} si no existe. */
    public CatalogDomainDTO getDomain(String domain) {
        List<CatalogEntryDTO> entries = catalog.getDomains().get(domain);
        if (entries == null) {
            return null;
        }
        return CatalogDomainDTO.builder()
                .version(catalog.getVersion())
                .hash(catalog.getHash())
                .domain(domain)
                .entries(entries)
                .build();
    }

    // ------------------------------------------------------------------------
    // Carga
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private CatalogResponseDTO load() {
        Map<String, Object> root;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CATALOG_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso " + CATALOG_RESOURCE);
            }
            root = new Yaml().load(in);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer " + CATALOG_RESOURCE + ": " + e.getMessage(), e);
        }

        if (root == null || root.get("domains") == null) {
            throw new IllegalStateException(CATALOG_RESOURCE + " no define 'domains'");
        }

        String version = String.valueOf(root.getOrDefault("version", "0"));
        Map<String, Object> domainsRaw = (Map<String, Object>) root.get("domains");

        Map<String, List<CatalogEntryDTO>> domains = new LinkedHashMap<>();
        for (Map.Entry<String, Object> domainEntry : domainsRaw.entrySet()) {
            String domainName = domainEntry.getKey();
            Map<String, Object> spec = (Map<String, Object>) domainEntry.getValue();
            List<Map<String, Object>> entriesRaw = (List<Map<String, Object>>) spec.get("entries");
            if (entriesRaw == null || entriesRaw.isEmpty()) {
                throw new IllegalStateException("El dominio '" + domainName + "' no tiene 'entries'");
            }

            List<CatalogEntryDTO> entries = new ArrayList<>(entriesRaw.size());
            for (int i = 0; i < entriesRaw.size(); i++) {
                entries.add(toEntry(domainName, entriesRaw.get(i), i));
            }

            String enumClass = (String) spec.get("enum");
            if (enumClass != null) {
                validateAgainstEnum(domainName, enumClass, entries);
            }

            domains.put(domainName, Collections.unmodifiableList(entries));
        }

        return CatalogResponseDTO.builder()
                .version(version)
                .hash(hash(domains))
                .domains(Collections.unmodifiableMap(domains))
                .build();
    }

    private CatalogEntryDTO toEntry(String domain, Map<String, Object> raw, int order) {
        String code = (String) raw.get("code");
        if (code == null || code.isBlank()) {
            throw new IllegalStateException("Entrada sin 'code' en el dominio '" + domain + "' (posición " + order + ")");
        }
        String label = (String) raw.get("label");
        if (label == null || label.isBlank()) {
            throw new IllegalStateException("El código '" + code + "' del dominio '" + domain + "' no tiene 'label'");
        }
        return CatalogEntryDTO.builder()
                .code(code)
                .label(label)
                .order(order)
                .group((String) raw.get("group"))
                .dotClass((String) raw.get("dotClass"))
                .sortOrder(asInteger(raw.get("sortOrder")))
                .icon((String) raw.get("icon"))
                .tone((String) raw.get("tone"))
                .build();
    }

    private Integer asInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private void validateAgainstEnum(String domain, String enumClassName, List<CatalogEntryDTO> entries) {
        Class<?> enumClass;
        try {
            enumClass = Class.forName(enumClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "El dominio '" + domain + "' referencia un enum inexistente: " + enumClassName, e);
        }
        if (!enumClass.isEnum()) {
            throw new IllegalStateException("El dominio '" + domain + "' referencia una clase que no es enum: " + enumClassName);
        }

        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .map(c -> ((Enum<?>) c).name())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> ymlCodes = entries.stream()
                .map(CatalogEntryDTO::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (ymlCodes.size() != entries.size()) {
            throw new IllegalStateException("El dominio '" + domain + "' tiene códigos duplicados");
        }
        if (!enumNames.equals(ymlCodes)) {
            Set<String> missingInYml = new TreeSet<>(enumNames);
            missingInYml.removeAll(ymlCodes);
            Set<String> unknownInYml = new TreeSet<>(ymlCodes);
            unknownInYml.removeAll(enumNames);
            throw new IllegalStateException(String.format(
                    "El dominio '%s' de %s diverge del enum %s — faltan en catalog.yml: %s; sobran en catalog.yml: %s",
                    domain, CATALOG_RESOURCE, enumClassName, missingInYml, unknownInYml));
        }
    }

    // ------------------------------------------------------------------------
    // Hash de contenido (determinista, independiente del orden de mapas/campos)
    // ------------------------------------------------------------------------

    private String hash(Map<String, List<CatalogEntryDTO>> domains) {
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                    .build();
            byte[] canonical = mapper.writeValueAsBytes(domains);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de catálogos: " + e.getMessage(), e);
        }
    }
}
