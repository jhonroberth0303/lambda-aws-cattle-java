package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileReproductive;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.LogType;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.repository.ProfileLifecycleRepository;
import com.cattle.repository.ProfilePregnancyRepository;
import com.cattle.repository.ProfileReproductiveRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Proyecta los eventos de salida (VENTA/MUERTE) sobre los perfiles del bovino.
 * <p>
 * DT-20260909, hallazgo A: antes de este proyector, {@code BovineEventProcessor} solo
 * guardaba el evento en el timeline; nada actualizaba {@code ProfileLifecycle} ni cerraba
 * lactancia/preñez activas, por lo que un animal muerto o vendido seguía apareciendo
 * activo en el summary.
 */
@Service
public class ExitEventProjector {

    private static final String LIFECYCLE_SK = "PROFILE#LIFECYCLE";
    private static final String REPRODUCTIVE_SK = "PROFILE#REPRODUCTIVE";
    private static final String CLOSE_STATUS = "CLOSE";
    /** Misma zona operativa que el resto del proyecto (ej. BovineSummaryService.ZONE_ID). */
    private static final ZoneId ZONE_ID = ZoneId.of(System.getenv().getOrDefault("APP_TIMEZONE", "America/Bogota"));

    private static final Map<BovineEventType, LifecycleStatus> EXIT_STATUS = new EnumMap<>(BovineEventType.class);
    static {
        EXIT_STATUS.put(BovineEventType.VENTA, LifecycleStatus.SOLD);
        EXIT_STATUS.put(BovineEventType.MUERTE, LifecycleStatus.DEAD);
    }

    private final ProfileLifecycleRepository lifecycleRepository;
    private final ProfileReproductiveRepository reproductiveRepository;
    private final ProfileLactancyRepository lactancyRepository;
    private final ProfilePregnancyRepository pregnancyRepository;
    private final LambdaContext lambdaContext;

    public ExitEventProjector(ProfileLifecycleRepository lifecycleRepository,
                              ProfileReproductiveRepository reproductiveRepository,
                              ProfileLactancyRepository lactancyRepository,
                              ProfilePregnancyRepository pregnancyRepository,
                              LambdaContext lambdaContext) {
        this.lifecycleRepository = lifecycleRepository;
        this.reproductiveRepository = reproductiveRepository;
        this.lactancyRepository = lactancyRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.lambdaContext = lambdaContext;
    }

    /**
     * Aplica la proyección de salida: actualiza {@code LifecycleStatus}, deshabilita
     * el bovino y cierra la lactancia/preñez activa, si existen.
     *
     * @param bovineId  id del bovino (sin prefijo {@code BOVINE#})
     * @param eventType tipo de evento; si no es VENTA ni MUERTE, no hace nada
     * @param eventAt   fecha efectiva del evento, usada como fecha de cierre
     */
    public void project(String bovineId, BovineEventType eventType, Instant eventAt) {
        LifecycleStatus targetStatus = EXIT_STATUS.get(eventType);
        if (targetStatus == null) {
            return;
        }

        String pk = "BOVINE#" + bovineId;
        projectLifecycle(pk, targetStatus);
        closeActiveProfiles(pk, eventAt);

        lambdaContext.logInfo(LogType.SERVICE, "Proyección de salida aplicada. bovineId=" + bovineId
                + ", eventType=" + eventType + ", status=" + targetStatus);
    }

    private void projectLifecycle(String pk, LifecycleStatus targetStatus) {
        lifecycleRepository.findById(pk, LIFECYCLE_SK).ifPresent(lifecycle -> {
            lifecycle.setStatus(targetStatus);
            lifecycle.setEnabled(false);
            lifecycleRepository.save(lifecycle);
        });
    }

    private void closeActiveProfiles(String pk, Instant eventAt) {
        Optional<ProfileReproductive> reproductiveOpt = reproductiveRepository.findById(pk, REPRODUCTIVE_SK);
        if (reproductiveOpt.isEmpty()) {
            return;
        }
        ProfileReproductive reproductive = reproductiveOpt.get();
        closeLactancy(pk, reproductive.getCurrentLactationId(), eventAt);
        closePregnancy(pk, reproductive.getCurrentPregnancyId());
    }

    private void closeLactancy(String pk, String lactationId, Instant eventAt) {
        if (lactationId == null || lactationId.isBlank()) {
            return;
        }
        lactancyRepository.findById(pk, lactationId).ifPresent(lactancy -> {
            if (CLOSE_STATUS.equalsIgnoreCase(lactancy.getStatus())) {
                return;
            }
            lactancy.setStatus(CLOSE_STATUS);
            lactancy.setEndDate(eventAt.atZone(ZONE_ID).toLocalDate().toString());
            lactancyRepository.save(lactancy);
        });
    }

    private void closePregnancy(String pk, String pregnancyId) {
        if (pregnancyId == null || pregnancyId.isBlank()) {
            return;
        }
        pregnancyRepository.findById(pk, pregnancyId).ifPresent(pregnancy -> {
            if (CLOSE_STATUS.equalsIgnoreCase(pregnancy.getStatus())) {
                return;
            }
            pregnancy.setStatus(CLOSE_STATUS);
            pregnancyRepository.save(pregnancy);
        });
    }
}
