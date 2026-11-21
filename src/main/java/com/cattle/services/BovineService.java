package com.cattle.services;

import com.cattle.config.AppProperties;
import com.cattle.config.LambdaContext;
import com.cattle.config.TablesConfig;
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

    private final BovineRepository bovineRepository;
    private final LambdaContext lambdaContext;
    private  final CounterRepository counterRepository;
    private final String tableBovines;
    private final ZoneId zoneId;

    public BovineService(BovineRepository bovineRepository, LambdaContext lambdaContext,
                         CounterRepository counterRepository, TablesConfig tablesConfig, AppProperties appProperties) {
        this.lambdaContext = lambdaContext;
        this.bovineRepository = bovineRepository;
        this.counterRepository = counterRepository;
        this.tableBovines = tablesConfig.getBovines();
        this.zoneId = ZoneId.of(appProperties.getTimezone());
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
            String counter = counterRepository.getNextId(tableBovines);
            bovineIdentityItem.setBovineId(Integer.parseInt(counter));
            bovineIdentityItem.setPk("BOVINE#" + counter);
            bovineIdentityItem.setSk("IDENTITY");
            bovineIdentityItem.setGsi1pk("IDENTITY");
            bovineIdentityItem.setGsi1sk("BOVINE#" + counter);
            bovineIdentityItem.setGender(normalizeGender(bovineIdentityItem.getGender()));
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

    private String getLocalDate() {
        return ZonedDateTime.now(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Edita la identidad haciendo merge sobre el ítem guardado: solo se sobrescriben los campos que
     * llegan no nulos. Antes se reemplazaba el ítem completo con el payload parcial del formulario y se
     * perdían farmId, origin, createdAt, breedComposition y los padres (HU-20261120, RF3/RF4).
     * Los textos opcionales se vacían enviando "" (ver mergeOptionalText); null o ausente = conservar.
     */
    public Optional<BovineIdentityItem> update(BovineIdentityItem changes) {
        try {
            BovineIdentityItem current = bovineRepository.findById(changes.getBovineId())
                    .orElseThrow(() -> new ServiceException("Bovine not found with ID: " + changes.getBovineId()));
            BovineIdentityItem merged = mergeIdentity(current, changes);
            merged.setPk("BOVINE#" + current.getBovineId());
            merged.setSk("IDENTITY");
            merged.setGsi1pk("IDENTITY");
            merged.setGsi1sk("BOVINE#" + current.getBovineId());
            merged.setUpdatedAt(getLocalDate());
            lambdaContext.logInfo(LogType.SERVICE, "Updating bovine with ID: " + current.getBovineId());
            return bovineRepository.update(merged);
        } catch (RepositoryException e) {
            lambdaContext.logException(LogType.SERVICE, "Failed to update bovine: " + e.getMessage());
            throw new ServiceException("Failed to update bovine", e);
        }
    }

    /** Copia del ítem actual con los campos no nulos de {@code changes}; createdAt nunca cambia (RF4). */
    static BovineIdentityItem mergeIdentity(BovineIdentityItem current, BovineIdentityItem changes) {
        BovineIdentityItem merged = BovineIdentityItem.builder()
                .bovineId(current.getBovineId())
                .farmId(firstNonNull(changes.getFarmId(), current.getFarmId()))
                .name(firstNonNull(changes.getName(), current.getName()))
                .gender(normalizeGender(firstNonNull(changes.getGender(), current.getGender())))
                .breed(mergeOptionalText(changes.getBreed(), current.getBreed()))
                .breedComposition(firstNonNull(changes.getBreedComposition(), current.getBreedComposition()))
                .bornDate(firstNonNull(changes.getBornDate(), current.getBornDate()))
                .color(mergeOptionalText(changes.getColor(), current.getColor()))
                .origin(firstNonNull(changes.getOrigin(), current.getOrigin()))
                .fatherId(mergeOptionalText(changes.getFatherId(), current.getFatherId()))
                .fatherNameSnapshot(mergeOptionalText(changes.getFatherNameSnapshot(), current.getFatherNameSnapshot()))
                .motherId(mergeOptionalText(changes.getMotherId(), current.getMotherId()))
                .motherNameSnapshot(mergeOptionalText(changes.getMotherNameSnapshot(), current.getMotherNameSnapshot()))
                .breedType(mergeOptionalText(changes.getBreedType(), current.getBreedType()))
                .build();
        merged.setCreatedAt(current.getCreatedAt());
        return merged;
    }

    /** RF6: el género se guarda siempre en mayúsculas (FEMALE / MALE). */
    static String normalizeGender(String gender) {
        return gender == null ? null : gender.trim().toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Texto opcional en el merge (revisión H3): null = no se envió (conserva); "" o solo espacios = vaciar
     * (queda null, el atributo no se escribe); otro valor = reemplaza.
     */
    static String mergeOptionalText(String sent, String stored) {
        if (sent == null) {
            return stored;
        }
        return sent.isBlank() ? null : sent;
    }

    private static <T> T firstNonNull(T preferred, T fallback) {
        return preferred != null ? preferred : fallback;
    }

}
