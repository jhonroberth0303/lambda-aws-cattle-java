package com.cattle.tasks.service;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.enums.LogType;
import com.cattle.repository.BovineRepository;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Mantenimiento diario de la agenda reproductiva (HU-20260929, fase B): sincroniza todas las
 * hembras del hato. En una sola pasada:
 * <ul>
 *   <li>marca VENCIDA lo que pasó su ventana (CA13, CA23);</li>
 *   <li>reconcilia eventos cuya sincronización en línea falló (CA15, RT2);</li>
 *   <li>en la primera corrida, hace la carga inicial del último ciclo (CA18).</li>
 * </ul>
 * Un fallo en un bovino no corta el lote.
 */
@Service
public class ReproductiveTaskMaintenanceService {

    private static final String FEMALE = "female";

    private final BovineRepository bovineRepository;
    private final ReproductiveTaskSynchronizer synchronizer;
    private final ReproductiveTaskSettingsProvider settingsProvider;
    private final LambdaContext lambdaContext;
    /** Finca de contexto del batch (D4/H9): la misma que usa la app para eventos y agenda. */
    private final String farmId;

    public ReproductiveTaskMaintenanceService(BovineRepository bovineRepository,
                                              ReproductiveTaskSynchronizer synchronizer,
                                              ReproductiveTaskSettingsProvider settingsProvider,
                                              LambdaContext lambdaContext,
                                              @Value("${reproductive-tasks.farm-id:F001}") String farmId) {
        this.bovineRepository = bovineRepository;
        this.synchronizer = synchronizer;
        this.settingsProvider = settingsProvider;
        this.lambdaContext = lambdaContext;
        this.farmId = farmId;
    }

    public record DailyResult(int processed, int created, int updated, int skipped, int failed) {
    }

    public DailyResult runDaily(LocalDate today) {
        ReproductiveTaskSettings settings = settingsProvider.current();
        List<BovineIdentityItem> females = bovineRepository.findAllIdentities().stream()
                .filter(identity -> FEMALE.equalsIgnoreCase(identity.getGender()))
                .filter(identity -> identity.getBovineId() != null)
                .toList();

        int created = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        for (BovineIdentityItem identity : females) {
            try {
                ReproductiveTaskSynchronizer.SyncResult result = synchronizer.sync(farmId, identity, settings, today);
                created += result.created();
                updated += result.updated();
                if (result.skippedReason() != null) {
                    skipped++;
                }
            } catch (Exception ex) {
                failed++;
                lambdaContext.logException(LogType.SERVICE, "Fallo al sincronizar tareas reproductivas en el batch. bovineId="
                        + identity.getBovineId(), ex);
            }
        }
        DailyResult result = new DailyResult(females.size(), created, updated, skipped, failed);
        lambdaContext.logInfo(LogType.SERVICE, "Mantenimiento de tareas reproductivas completado. farmId=" + farmId
                + ", today=" + today + ", " + result);
        return result;
    }
}
