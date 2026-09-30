package com.cattle.tasks.service;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.repository.BovineRepository;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
@DisplayName("ReproductiveTaskMaintenanceService Tests")
class ReproductiveTaskMaintenanceServiceTest {

    private static final LocalDate TODAY = LocalDate.parse("2026-09-30");
    private static final ReproductiveTaskSettings SETTINGS = ReproductiveTaskSettings.defaults();

    @Mock private BovineRepository bovineRepository;
    @Mock private ReproductiveTaskSynchronizer synchronizer;
    @Mock private ReproductiveTaskSettingsProvider settingsProvider;
    @Mock private LambdaContext lambdaContext;

    private ReproductiveTaskMaintenanceService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new ReproductiveTaskMaintenanceService(bovineRepository, synchronizer, settingsProvider,
                lambdaContext, "F001");
        when(settingsProvider.current()).thenReturn(SETTINGS);
    }

    private static BovineIdentityItem bovine(int id, String gender) {
        return BovineIdentityItem.builder().bovineId(id).gender(gender).farmId("FARM#001").build();
    }

    private static ReproductiveTaskSynchronizer.SyncResult result(int created, int updated) {
        return new ReproductiveTaskSynchronizer.SyncResult(created, updated, null);
    }

    @Test
    @DisplayName("Sincroniza solo hembras (sin importar mayúsculas) con la finca configurada y suma resultados")
    void runDaily_syncsFemalesWithConfiguredFarm() {
        BovineIdentityItem upper = bovine(167, "FEMALE");
        BovineIdentityItem lower = bovine(174, "female");
        when(bovineRepository.findAllIdentities()).thenReturn(List.of(upper, lower, bovine(173, "MALE")));
        when(synchronizer.sync("F001", upper, SETTINGS, TODAY)).thenReturn(result(2, 0));
        when(synchronizer.sync("F001", lower, SETTINGS, TODAY)).thenReturn(result(0, 1));

        ReproductiveTaskMaintenanceService.DailyResult daily = service.runDaily(TODAY);

        assertThat(daily.processed()).isEqualTo(2);
        assertThat(daily.created()).isEqualTo(2);
        assertThat(daily.updated()).isEqualTo(1);
        assertThat(daily.failed()).isZero();
        verify(synchronizer, never()).sync(any(), argThat((BovineIdentityItem b) -> b.getBovineId() == 173), any(), any());
    }

    @Test
    @DisplayName("CA15: el fallo de un bovino no corta el lote")
    void runDaily_failureIsIsolatedPerBovine() {
        BovineIdentityItem failing = bovine(167, "FEMALE");
        BovineIdentityItem ok = bovine(169, "FEMALE");
        when(bovineRepository.findAllIdentities()).thenReturn(List.of(failing, ok));
        when(synchronizer.sync("F001", failing, SETTINGS, TODAY)).thenThrow(new IllegalStateException("boom"));
        when(synchronizer.sync("F001", ok, SETTINGS, TODAY)).thenReturn(result(1, 0));

        ReproductiveTaskMaintenanceService.DailyResult daily = service.runDaily(TODAY);

        assertThat(daily.failed()).isEqualTo(1);
        assertThat(daily.created()).isEqualTo(1);
        verify(lambdaContext).logException(any(), org.mockito.ArgumentMatchers.contains("bovineId=167"),
                any(IllegalStateException.class));
    }

    @Test
    @DisplayName("Los omitidos por el sincronizador se cuentan aparte; los parámetros se cargan una vez por lote")
    void runDaily_countsSkippedAndLoadsSettingsOnce() {
        BovineIdentityItem a = bovine(167, "FEMALE");
        BovineIdentityItem b = bovine(169, "FEMALE");
        when(bovineRepository.findAllIdentities()).thenReturn(List.of(a, b));
        when(synchronizer.sync(eq("F001"), any(BovineIdentityItem.class), any(), any()))
                .thenReturn(new ReproductiveTaskSynchronizer.SyncResult(0, 0, "no es hembra"));

        assertThat(service.runDaily(TODAY).skipped()).isEqualTo(2);
        verify(settingsProvider).current();
        verify(lambdaContext).logInfo(any(), anyString());
    }
}
