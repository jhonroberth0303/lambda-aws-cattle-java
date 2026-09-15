package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.entities.bovines.ProfilePregnancy;
import com.cattle.entities.bovines.ProfileReproductive;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.repository.ProfileLifecycleRepository;
import com.cattle.repository.ProfilePregnancyRepository;
import com.cattle.repository.ProfileReproductiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para ExitEventProjector.
 * DT-20260909, hallazgo A: proyección VENTA/MUERTE -> LifecycleStatus + cierre de perfiles activos.
 */
@Tag("unit")
@Tag("fast")
@Tag("services")
@DisplayName("ExitEventProjector Tests")
class ExitEventProjectorTest {

    private static final String PK = "BOVINE#B1";
    // 15:00 UTC = 10:00 America/Bogota (UTC-5): mismo día calendario en ambas zonas,
    // para no acoplar los tests que no versan sobre zona horaria a ese detalle.
    private static final Instant EVENT_AT = Instant.parse("2026-04-10T15:00:00Z");

    @Mock
    private ProfileLifecycleRepository lifecycleRepository;

    @Mock
    private ProfileReproductiveRepository reproductiveRepository;

    @Mock
    private ProfileLactancyRepository lactancyRepository;

    @Mock
    private ProfilePregnancyRepository pregnancyRepository;

    @Mock
    private LambdaContext lambdaContext;

    private ExitEventProjector projector;

    @BeforeEach
    void setUp() {
        openMocks(this);
        projector = new ExitEventProjector(lifecycleRepository, reproductiveRepository,
                lactancyRepository, pregnancyRepository, lambdaContext);
    }

    private ProfileLifecycle lifecycle(LifecycleStatus status, Boolean enabled) {
        ProfileLifecycle lifecycle = ProfileLifecycle.builder().pk(PK).sk("PROFILE#LIFECYCLE").build();
        lifecycle.setStatus(status);
        lifecycle.setEnabled(enabled);
        return lifecycle;
    }

    @Test
    @DisplayName("MUERTE sin lactancia ni preñez actualiza solo el lifecycle")
    void project_muerte_withoutActiveProfiles_updatesOnlyLifecycle() {
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE"))
                .thenReturn(Optional.of(lifecycle(LifecycleStatus.OPEN, true)));
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.empty());

        projector.project("B1", BovineEventType.MUERTE, EVENT_AT);

        ArgumentCaptor<ProfileLifecycle> captor = ArgumentCaptor.forClass(ProfileLifecycle.class);
        verify(lifecycleRepository).save(captor.capture());
        assertEquals(LifecycleStatus.DEAD, captor.getValue().getStatus());
        assertFalse(captor.getValue().getEnabled());
        verify(lactancyRepository, never()).save(any());
        verify(pregnancyRepository, never()).save(any());
    }

    @Test
    @DisplayName("VENTA con lactancia LACTATING activa la cierra con endDate = eventAt")
    void project_venta_withActiveLactancy_closesLactancy() {
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE"))
                .thenReturn(Optional.of(lifecycle(LifecycleStatus.OPEN, true)));
        ProfileReproductive reproductive = ProfileReproductive.builder()
                .pk(PK).sk("PROFILE#REPRODUCTIVE").currentLactationId("LACT#01").build();
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
        ProfileLactancy lactancy = ProfileLactancy.builder().pk(PK).sk("LACT#01").status("LACTATING").build();
        when(lactancyRepository.findById(PK, "LACT#01")).thenReturn(Optional.of(lactancy));
        when(pregnancyRepository.findById(any(), any())).thenReturn(Optional.empty());

        projector.project("B1", BovineEventType.VENTA, EVENT_AT);

        ArgumentCaptor<ProfileLifecycle> lifecycleCaptor = ArgumentCaptor.forClass(ProfileLifecycle.class);
        verify(lifecycleRepository).save(lifecycleCaptor.capture());
        assertEquals(LifecycleStatus.SOLD, lifecycleCaptor.getValue().getStatus());

        ArgumentCaptor<ProfileLactancy> lactancyCaptor = ArgumentCaptor.forClass(ProfileLactancy.class);
        verify(lactancyRepository).save(lactancyCaptor.capture());
        assertEquals("CLOSE", lactancyCaptor.getValue().getStatus());
        assertEquals("2026-04-10", lactancyCaptor.getValue().getEndDate());
    }

    @Test
    @DisplayName("MUERTE con preñez ACTIVE la cierra como CLOSE")
    void project_muerte_withActivePregnancy_closesPregnancy() {
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE"))
                .thenReturn(Optional.of(lifecycle(LifecycleStatus.OPEN, true)));
        ProfileReproductive reproductive = ProfileReproductive.builder()
                .pk(PK).sk("PROFILE#REPRODUCTIVE").currentPregnancyId("PREG#2026-01-01").build();
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
        when(lactancyRepository.findById(any(), any())).thenReturn(Optional.empty());
        ProfilePregnancy pregnancy = ProfilePregnancy.builder().pk(PK).sk("PREG#2026-01-01").status("ACTIVE").build();
        when(pregnancyRepository.findById(PK, "PREG#2026-01-01")).thenReturn(Optional.of(pregnancy));

        projector.project("B1", BovineEventType.MUERTE, EVENT_AT);

        ArgumentCaptor<ProfilePregnancy> captor = ArgumentCaptor.forClass(ProfilePregnancy.class);
        verify(pregnancyRepository).save(captor.capture());
        assertEquals("CLOSE", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Lactancia ya cerrada no se vuelve a guardar (idempotencia)")
    void project_alreadyClosedLactancy_isNotOverwritten() {
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE"))
                .thenReturn(Optional.of(lifecycle(LifecycleStatus.OPEN, true)));
        ProfileReproductive reproductive = ProfileReproductive.builder()
                .pk(PK).sk("PROFILE#REPRODUCTIVE").currentLactationId("LACT#01").build();
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
        ProfileLactancy lactancy = ProfileLactancy.builder().pk(PK).sk("LACT#01").status("CLOSE")
                .endDate("2025-12-01").build();
        when(lactancyRepository.findById(PK, "LACT#01")).thenReturn(Optional.of(lactancy));
        when(pregnancyRepository.findById(any(), any())).thenReturn(Optional.empty());

        projector.project("B1", BovineEventType.MUERTE, EVENT_AT);

        verify(lactancyRepository, never()).save(any());
    }

    @Test
    @DisplayName("currentLactationId/currentPregnancyId colgados (perfil inexistente) no lanzan excepción")
    void project_danglingReproductiveReferences_doesNotThrow() {
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE"))
                .thenReturn(Optional.of(lifecycle(LifecycleStatus.OPEN, true)));
        ProfileReproductive reproductive = ProfileReproductive.builder()
                .pk(PK).sk("PROFILE#REPRODUCTIVE")
                .currentLactationId("LACT#99")
                .currentPregnancyId("PREG#99")
                .build();
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
        when(lactancyRepository.findById(PK, "LACT#99")).thenReturn(Optional.empty());
        when(pregnancyRepository.findById(PK, "PREG#99")).thenReturn(Optional.empty());

        projector.project("B1", BovineEventType.MUERTE, EVENT_AT);

        verify(lactancyRepository, never()).save(any());
        verify(pregnancyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bovino sin perfil de lifecycle (dato legacy) no lanza excepción")
    void project_withoutLifecycleProfile_doesNotThrow() {
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE")).thenReturn(Optional.empty());
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.empty());

        projector.project("B1", BovineEventType.MUERTE, EVENT_AT);

        verify(lifecycleRepository, never()).save(any());
    }

    @Test
    @DisplayName("endDate usa la zona operativa (America/Bogota), no UTC — hallazgo de revisión #2")
    void project_closeLactancy_usesOperationalTimezoneNotUtc() {
        // 2026-04-11T02:00:00Z = 2026-04-10T21:00:00 hora Bogotá (UTC-5): mismo instante,
        // día calendario distinto según la zona usada para derivar endDate.
        Instant nearMidnightUtc = Instant.parse("2026-04-11T02:00:00Z");
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE"))
                .thenReturn(Optional.of(lifecycle(LifecycleStatus.OPEN, true)));
        ProfileReproductive reproductive = ProfileReproductive.builder()
                .pk(PK).sk("PROFILE#REPRODUCTIVE").currentLactationId("LACT#01").build();
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
        ProfileLactancy lactancy = ProfileLactancy.builder().pk(PK).sk("LACT#01").status("LACTATING").build();
        when(lactancyRepository.findById(PK, "LACT#01")).thenReturn(Optional.of(lactancy));
        when(pregnancyRepository.findById(any(), any())).thenReturn(Optional.empty());

        projector.project("B1", BovineEventType.MUERTE, nearMidnightUtc);

        ArgumentCaptor<ProfileLactancy> captor = ArgumentCaptor.forClass(ProfileLactancy.class);
        verify(lactancyRepository).save(captor.capture());
        assertEquals("2026-04-10", captor.getValue().getEndDate());
    }

    @Test
    @DisplayName("Evento que no es de salida no hace nada")
    void project_nonExitEvent_doesNothing() {
        projector.project("B1", BovineEventType.PESAJE, EVENT_AT);

        verify(lifecycleRepository, never()).findById(any(), any());
        verify(reproductiveRepository, never()).findById(any(), any());
    }
}
