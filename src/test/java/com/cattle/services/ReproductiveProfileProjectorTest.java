package com.cattle.services;

import com.cattle.config.AppProperties;
import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.entities.bovines.ProfilePregnancy;
import com.cattle.entities.bovines.ProfileReproductive;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.repository.BovineEventRepository;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.repository.ProfilePregnancyRepository;
import com.cattle.repository.ProfileReproductiveRepository;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import com.cattle.tasks.service.PlannerEventMapper;
import com.cattle.tasks.service.ReproductiveTaskSettingsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * HU-20260930: los eventos reproductivos actualizan preñez y lactancia (PE1–PE5).
 */
@Tag("unit")
@Tag("fast")
@DisplayName("ReproductiveProfileProjector Tests")
class ReproductiveProfileProjectorTest {

    private static final String PK = "BOVINE#175";

    @Mock private ProfileReproductiveRepository reproductiveRepository;
    @Mock private ProfilePregnancyRepository pregnancyRepository;
    @Mock private ProfileLactancyRepository lactancyRepository;
    @Mock private BovineEventRepository bovineEventRepository;
    @Mock private ReproductiveTaskSettingsProvider settingsProvider;
    @Mock private LambdaContext lambdaContext;

    private ReproductiveProfileProjector projector;
    private final List<BovineEventItem> history = new ArrayList<>();

    @BeforeEach
    void setUp() {
        openMocks(this);
        PlannerEventMapper mapper = new PlannerEventMapper(new ObjectMapper(), lambdaContext, new AppProperties());
        projector = new ReproductiveProfileProjector(reproductiveRepository, pregnancyRepository, lactancyRepository,
                bovineEventRepository, mapper, settingsProvider, lambdaContext, "001");
        when(settingsProvider.current()).thenReturn(ReproductiveTaskSettings.defaults());
        when(bovineEventRepository.findAllByBovine("175")).thenReturn(history);
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.empty());
        when(lactancyRepository.findAllLactationsByBovine(PK)).thenReturn(Optional.empty());
    }

    /** Fecha elegida en el formulario (medianoche UTC, RT1 de la HU-20260929). */
    private BovineEventItem event(String id, String type, String date, String payloadJson) {
        BovineEventItem item = new BovineEventItem();
        item.setEventId(id);
        item.setEventType(type);
        item.setEventAt(Instant.parse(date + "T00:00:00Z"));
        item.setCreatedAt(date + "T12:00:00Z");
        item.setPayloadJson(payloadJson);
        history.add(item);
        return item;
    }

    private void reproductive(String pregnancyId, String lactationId) {
        ProfileReproductive r = ProfileReproductive.builder().pk(PK).sk("PROFILE#REPRODUCTIVE").build();
        r.setCurrentPregnancyId(pregnancyId);
        r.setCurrentLactationId(lactationId);
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(r));
    }

    private void pregnancy(String sk, String status) {
        ProfilePregnancy p = ProfilePregnancy.builder().pk(PK).sk(sk).build();
        p.setStatus(status);
        when(pregnancyRepository.findById(PK, sk)).thenReturn(Optional.of(p));
    }

    private ProfileLactancy lactation(String sk, String number, String status, String start) {
        ProfileLactancy l = ProfileLactancy.builder().pk(PK).sk(sk).build();
        l.setLactationNumber(number);
        l.setStatus(status);
        l.setStartDate(start);
        when(lactancyRepository.findById(PK, sk)).thenReturn(Optional.of(l));
        return l;
    }

    @Test
    @DisplayName("CA1: diagnóstico positivo abre preñez activa con servicio, parto esperado y método")
    void positiveDiagnosis_opensPregnancy() {
        event("S1", "INSEMINACION", "2026-10-01", "{}");
        BovineEventItem diagnosis = event("D1", "DIAGNOSTICO_PRENEZ", "2026-11-10",
                "{\"result\":\"PRENIADA\",\"method\":\"PALPA\"}");

        projector.project("175", diagnosis);

        ArgumentCaptor<ProfilePregnancy> saved = ArgumentCaptor.forClass(ProfilePregnancy.class);
        verify(pregnancyRepository).save(saved.capture());
        ProfilePregnancy p = saved.getValue();
        assertThat(p.getSk()).isEqualTo("PREG#2026-10-01");
        assertThat(p.getStatus()).isEqualTo("ACTIVE");
        assertThat(p.getServiceDate()).isEqualTo("2026-10-01");
        assertThat(p.getExpectedDueDate()).isEqualTo("2027-07-07");
        assertThat(p.getConfirmationMethod()).isEqualTo("palpation");
        assertThat(p.getGsi1pk()).isEqualTo("PREG#ACTIVE");

        ArgumentCaptor<ProfileReproductive> repro = ArgumentCaptor.forClass(ProfileReproductive.class);
        verify(reproductiveRepository).save(repro.capture());
        assertThat(repro.getValue().getCurrentPregnancyId()).isEqualTo("PREG#2026-10-01");
        assertThat(repro.getValue().getSk()).isEqualTo("PROFILE#REPRODUCTIVE");
        assertThat(repro.getValue().getGsi1pk()).isEqualTo("PROFILE");
    }

    @Test
    @DisplayName("P1/CA10: sin servicio usa la estimación; sin estimación abre preñez sin fechas")
    void positiveDiagnosis_withoutService() {
        BovineEventItem withEstimate = event("D1", "DIAGNOSTICO_PRENEZ", "2026-11-10",
                "{\"result\":\"PRENIADA\",\"gestationDays\":90}");
        projector.project("175", withEstimate);

        ArgumentCaptor<ProfilePregnancy> saved = ArgumentCaptor.forClass(ProfilePregnancy.class);
        verify(pregnancyRepository).save(saved.capture());
        assertThat(saved.getValue().getServiceDate()).isEqualTo("2026-08-12");
        assertThat(saved.getValue().getExpectedDueDate()).isEqualTo("2027-05-18");

        history.clear();
        BovineEventItem noEstimate = event("D2", "DIAGNOSTICO_PRENEZ", "2026-11-10", "{\"result\":\"PRENIADA\"}");
        projector.project("175", noEstimate);
        verify(pregnancyRepository, times(2)).save(saved.capture());
        ProfilePregnancy bare = saved.getValue();
        assertThat(bare.getSk()).isEqualTo("PREG#2026-11-10");
        assertThat(bare.getServiceDate()).isNull();
        assertThat(bare.getExpectedDueDate()).isNull();
        assertThat(bare.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("CA5: reconfirmación no abre otra preñez; solo actualiza el método")
    void reconfirmation_updatesMethodOnly() {
        reproductive("PREG#2026-10-01", null);
        pregnancy("PREG#2026-10-01", "ACTIVE");
        BovineEventItem diagnosis = event("D2", "DIAGNOSTICO_PRENEZ", "2026-12-20",
                "{\"result\":\"PRENIADA\",\"method\":\"ECOGRAFIA\"}");

        projector.project("175", diagnosis);

        ArgumentCaptor<ProfilePregnancy> saved = ArgumentCaptor.forClass(ProfilePregnancy.class);
        verify(pregnancyRepository).save(saved.capture());
        assertThat(saved.getValue().getSk()).isEqualTo("PREG#2026-10-01");
        assertThat(saved.getValue().getConfirmationMethod()).isEqualTo("ultrasound");
        verify(reproductiveRepository, never()).save(any());
    }

    @Test
    @DisplayName("CA6: VACIA y ABORTO cierran la preñez activa sin fecha de parto")
    void emptyDiagnosisAndAbortion_closePregnancy() {
        reproductive("PREG#2026-10-01", null);
        pregnancy("PREG#2026-10-01", "ACTIVE");

        projector.project("175", event("D2", "DIAGNOSTICO_PRENEZ", "2026-12-01", "{\"result\":\"VACIA\"}"));

        ArgumentCaptor<ProfilePregnancy> saved = ArgumentCaptor.forClass(ProfilePregnancy.class);
        verify(pregnancyRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo("CLOSE");
        assertThat(saved.getValue().getCalvingDate()).isNull();
        assertThat(saved.getValue().getGsi1pk()).isEqualTo("PREG#CLOSE");

        pregnancy("PREG#2026-10-01", "ACTIVE");
        projector.project("175", event("A1", "ABORTO", "2026-12-05", "{}"));
        verify(pregnancyRepository, times(2)).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo("CLOSE");
    }

    @Test
    @DisplayName("CA2 / PE9: parto cierra preñez y lactancia seca, y abre la n.º 3 con el formato que lee el ordeño")
    void calving_opensNextLactation() {
        reproductive("PREG#2026-10-01", "LACT#002");
        pregnancy("PREG#2026-10-01", "ACTIVE");
        ProfileLactancy dry = lactation("LACT#002", "2", "DRY", "2026-04-11");
        ProfileLactancy first = lactation("LACT#001", "1", "CLOSED", "2025-01-25");
        when(lactancyRepository.findAllLactationsByBovine(PK)).thenReturn(Optional.of(List.of(first, dry)));

        projector.project("175", event("P1", "PARTO", "2027-07-05", "{\"calfGender\":\"HEMBRA\",\"birthType\":\"NORMAL\"}"));

        ArgumentCaptor<ProfilePregnancy> preg = ArgumentCaptor.forClass(ProfilePregnancy.class);
        verify(pregnancyRepository).save(preg.capture());
        assertThat(preg.getValue().getStatus()).isEqualTo("CLOSE");
        assertThat(preg.getValue().getCalvingDate()).isEqualTo("2027-07-05");

        ArgumentCaptor<ProfileLactancy> lact = ArgumentCaptor.forClass(ProfileLactancy.class);
        verify(lactancyRepository, times(2)).save(lact.capture());
        ProfileLactancy closed = lact.getAllValues().get(0);
        assertThat(closed.getSk()).isEqualTo("LACT#002");
        assertThat(closed.getStatus()).isEqualTo("CLOSED");
        assertThat(closed.getEndDate()).isEqualTo("2027-07-05");

        ProfileLactancy opened = lact.getAllValues().get(1);
        assertThat(opened.getSk()).isEqualTo("LACT#003");
        assertThat(opened.getLactationNumber()).isEqualTo("3");
        assertThat(opened.getStatus()).isEqualTo("LACTATING");
        assertThat(opened.getStartDate()).isEqualTo("2027-07-05");
        assertThat(opened.getEndDate()).isNull();
        // Mismo formato que LACT#002 de la vaca 167 en producción: es lo que consulta el ordeño.
        assertThat(opened.getGsi1pk()).isEqualTo("LACT#FARM#001");
        assertThat(opened.getGsi1sk()).isEqualTo("2027-07-05#LACTATING#BOVINE#175#LACT#003");

        ArgumentCaptor<ProfileReproductive> repro = ArgumentCaptor.forClass(ProfileReproductive.class);
        verify(reproductiveRepository).save(repro.capture());
        assertThat(repro.getValue().getCurrentLactationId()).isEqualTo("LACT#003");
    }

    @Test
    @DisplayName("CA9: primer parto abre la lactancia n.º 1 y crea el perfil reproductivo")
    void firstCalving_opensFirstLactation() {
        projector.project("175", event("P1", "PARTO", "2027-07-05", "{\"calfGender\":\"MACHO\",\"birthType\":\"NORMAL\"}"));

        ArgumentCaptor<ProfileLactancy> lact = ArgumentCaptor.forClass(ProfileLactancy.class);
        verify(lactancyRepository).save(lact.capture());
        assertThat(lact.getValue().getSk()).isEqualTo("LACT#001");
        assertThat(lact.getValue().getLactationNumber()).isEqualTo("1");
        verify(pregnancyRepository, never()).save(any());
    }

    @Test
    @DisplayName("CA3: secado deja la lactancia seca con su fecha, sin cerrarla")
    void dryOff_setsDry() {
        reproductive(null, "LACT#002");
        lactation("LACT#002", "2", "LACTATING", "2026-04-11");

        projector.project("175", event("SEC", "SECADO", "2027-05-08", "{}"));

        ArgumentCaptor<ProfileLactancy> lact = ArgumentCaptor.forClass(ProfileLactancy.class);
        verify(lactancyRepository).save(lact.capture());
        assertThat(lact.getValue().getStatus()).isEqualTo("DRY");
        assertThat(lact.getValue().getDryDate()).isEqualTo("2027-05-08");
        assertThat(lact.getValue().getEndDate()).isNull();
    }

    @Test
    @DisplayName("RT2: eventos fuera de orden no corrompen la lactancia vigente")
    void outOfOrderEvents_areIgnored() {
        reproductive(null, "LACT#002");
        lactation("LACT#002", "2", "LACTATING", "2026-04-11");

        projector.project("175", event("SEC", "SECADO", "2026-03-01", "{}"));
        projector.project("175", event("P0", "PARTO", "2026-04-11", "{\"calfGender\":\"MACHO\",\"birthType\":\"NORMAL\"}"));

        verify(lactancyRepository, never()).save(any());
        verify(reproductiveRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eventos que no afectan perfiles no hacen nada")
    void unrelatedEvent_doesNothing() {
        projector.project("175", event("X", "PESAJE", "2026-10-01", "{\"weightKg\":320}"));

        verify(reproductiveRepository, never()).findById(any(), any());
        verify(pregnancyRepository, never()).save(any());
        verify(lactancyRepository, never()).save(any());
    }
}
