package com.cattle.tasks.service;

import com.cattle.config.AppProperties;
import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.entities.bovines.ProfileReproductive;
import com.cattle.enums.profiles.LifecycleStatus;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.repository.BovineEventRepository;
import com.cattle.repository.BovineRepository;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.repository.ProfileLifecycleRepository;
import com.cattle.repository.ProfileReproductiveRepository;
import com.cattle.tasks.entity.ReproductiveTaskItem;
import com.cattle.tasks.entity.ServiceFollowUpItem;
import com.cattle.tasks.planner.ReproductiveTaskPlanner;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import com.cattle.tasks.repository.ReproductiveTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
@DisplayName("ReproductiveTaskSynchronizer Tests")
class ReproductiveTaskSynchronizerTest {

    private static final String PK = "BOVINE#7";
    private static final ReproductiveTaskSettings SETTINGS = ReproductiveTaskSettings.defaults();
    private static final LocalDate TODAY = LocalDate.parse("2026-10-05");

    @Mock private BovineRepository bovineRepository;
    @Mock private ProfileLifecycleRepository lifecycleRepository;
    @Mock private ProfileReproductiveRepository reproductiveRepository;
    @Mock private ProfileLactancyRepository lactancyRepository;
    @Mock private BovineEventRepository bovineEventRepository;
    @Mock private ReproductiveTaskRepository taskRepository;
    @Mock private ReproductiveTaskSettingsProvider settingsProvider;
    @Mock private LambdaContext lambdaContext;

    private ReproductiveTaskSynchronizer synchronizer;
    private final List<BovineEventItem> events = new ArrayList<>();

    @BeforeEach
    void setUp() {
        openMocks(this);
        synchronizer = new ReproductiveTaskSynchronizer(bovineRepository, lifecycleRepository, reproductiveRepository,
                lactancyRepository, bovineEventRepository, taskRepository, new ReproductiveTaskPlanner(),
                settingsProvider, new ObjectMapper(), lambdaContext, new AppProperties());
        when(bovineRepository.findById(7)).thenReturn(Optional.of(identity("female", "F001")));
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE")).thenReturn(Optional.empty());
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.empty());
        when(bovineEventRepository.findAllByBovine("7")).thenReturn(events);
        when(taskRepository.findTasksByBovine("7")).thenReturn(List.of());
        when(taskRepository.findFollowUp("7")).thenReturn(Optional.empty());
        when(taskRepository.createIfAbsent(any())).thenReturn(true);
        when(taskRepository.saveIfStatus(any(), any())).thenReturn(true);
    }

    private static BovineIdentityItem identity(String gender, String farmId) {
        BovineIdentityItem identity = BovineIdentityItem.builder().bovineId(7).gender(gender).farmId(farmId).build();
        return identity;
    }

    private void event(String id, String type, String isoDate, String payloadJson) {
        BovineEventItem item = new BovineEventItem();
        item.setEventId(id);
        item.setEventType(type);
        // Fecha elegida en el formulario: resolveEventAt la guarda como medianoche UTC.
        item.setEventAt(Instant.parse(isoDate + "T00:00:00Z"));
        item.setCreatedAt(isoDate + "T12:00:00Z");
        item.setPayloadJson(payloadJson);
        events.add(item);
    }

    private static ReproductiveTaskItem stored(String sk, String status, String dueDate) {
        ReproductiveTaskItem item = new ReproductiveTaskItem();
        item.setPk(PK);
        item.setSk(sk);
        item.setFarmId("F001");
        item.setBovineId("7");
        item.setRuleCode(sk.split("#")[1]);
        item.setStatus(status);
        item.setDueDate(dueDate);
        item.applyOpenIndex(true);
        return item;
    }

    @Test
    @DisplayName("CA1: crea el diagnóstico en el índice de abiertas y guarda el semáforo abierto")
    void service_createsTaskAndFollowUp() {
        event("S1", "INSEMINACION", "2026-10-01", "{}");

        ReproductiveTaskSynchronizer.SyncResult result = synchronizer.sync("F001", "7", SETTINGS, TODAY);

        assertThat(result.created()).isEqualTo(1);
        ArgumentCaptor<ReproductiveTaskItem> task = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository).createIfAbsent(task.capture());
        assertThat(task.getValue().getSk()).isEqualTo("TASK#R1_DIAGNOSIS#S1");
        assertThat(task.getValue().getDueDate()).isEqualTo("2026-11-10"); // RT1: fecha explícita sin desfase
        assertThat(task.getValue().getStatus()).isEqualTo("PENDIENTE");
        assertThat(task.getValue().getGsi1pk()).isEqualTo("FARM#F001#OPEN");
        assertThat(task.getValue().getGsi1sk()).isEqualTo("2026-11-10#BOVINE#7#R1_DIAGNOSIS");

        ArgumentCaptor<ServiceFollowUpItem> followUp = ArgumentCaptor.forClass(ServiceFollowUpItem.class);
        verify(taskRepository).saveFollowUp(followUp.capture());
        assertThat(followUp.getValue().getOpen()).isTrue();
        assertThat(followUp.getValue().getServiceNumber()).isEqualTo(1);
        assertThat(followUp.getValue().getGsi1pk()).isEqualTo("FARMFOLLOWUP#F001#OPEN");
    }

    @Test
    @DisplayName("CA15: sincronizar de nuevo sin cambios no crea ni actualiza nada")
    void resync_isIdempotent() {
        event("S1", "INSEMINACION", "2026-10-01", "{}");
        when(taskRepository.findTasksByBovine("7"))
                .thenReturn(List.of(stored("TASK#R1_DIAGNOSIS#S1", "PENDIENTE", "2026-11-10")));
        ServiceFollowUpItem followUp = new ServiceFollowUpItem();
        followUp.setLastServiceEventId("S1");
        followUp.setServiceNumber(1);
        followUp.setOpen(true);
        when(taskRepository.findFollowUp("7")).thenReturn(Optional.of(followUp));

        ReproductiveTaskSynchronizer.SyncResult result = synchronizer.sync("F001", "7", SETTINGS, TODAY);

        assertThat(result.created()).isZero();
        assertThat(result.updated()).isZero();
        verify(taskRepository, never()).createIfAbsent(any());
        verify(taskRepository, never()).saveIfStatus(any(), any());
        verify(taskRepository, never()).saveFollowUp(any());
    }

    @Test
    @DisplayName("CA2: el diagnóstico positivo cierra la tarea guardada y la retira del índice de abiertas")
    void positiveDiagnosis_closesStoredTask() {
        event("S1", "INSEMINACION", "2026-10-01", "{}");
        event("D1", "DIAGNOSTICO_PRENEZ", "2026-11-10", "{\"result\":\"PRENIADA\"}");
        when(taskRepository.findTasksByBovine("7"))
                .thenReturn(List.of(stored("TASK#R1_DIAGNOSIS#S1", "PENDIENTE", "2026-11-10")));

        ReproductiveTaskSynchronizer.SyncResult result =
                synchronizer.sync("F001", "7", SETTINGS, LocalDate.parse("2026-11-10"));

        ArgumentCaptor<ReproductiveTaskItem> saved = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository).saveIfStatus(saved.capture(), eq("PENDIENTE"));
        assertThat(saved.getValue().getStatus()).isEqualTo("HECHA");
        assertThat(saved.getValue().getClosedByEventId()).isEqualTo("D1");
        assertThat(saved.getValue().getGsi1pk()).isNull();
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.created()).isEqualTo(2); // parto esperado y preparto (vaca sin lactancia)
    }

    @Test
    @DisplayName("G3: una tarea CANCELADA no vuelve a PENDIENTE")
    void finalStatus_isNotReopened() {
        event("S1", "INSEMINACION", "2026-10-01", "{}");
        when(taskRepository.findTasksByBovine("7"))
                .thenReturn(List.of(stored("TASK#R1_DIAGNOSIS#S1", "CANCELADA", "2026-11-10")));

        synchronizer.sync("F001", "7", SETTINGS, TODAY);

        verify(taskRepository, never()).saveIfStatus(any(), any());
    }

    @Test
    @DisplayName("CA12: al cambiar de estado se conserva el vencimiento guardado aunque cambien los parámetros")
    void transition_keepsStoredDueDate() {
        event("S1", "MONTA", "2026-10-01", "{}");
        event("D1", "DIAGNOSTICO_PRENEZ", "2026-11-10", "{\"result\":\"VACIA\"}");
        when(taskRepository.findTasksByBovine("7"))
                .thenReturn(List.of(stored("TASK#R1_DIAGNOSIS#S1", "PENDIENTE", "2026-11-08")));

        synchronizer.sync("F001", "7", SETTINGS, LocalDate.parse("2026-11-10"));

        ArgumentCaptor<ReproductiveTaskItem> saved = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository).saveIfStatus(saved.capture(), eq("PENDIENTE"));
        assertThat(saved.getValue().getDueDate()).isEqualTo("2026-11-08");
    }

    @Test
    @DisplayName("CA11: un macho se omite sin tocar la agenda")
    void male_isSkipped() {
        when(bovineRepository.findById(7)).thenReturn(Optional.of(identity("male", "F001")));

        ReproductiveTaskSynchronizer.SyncResult result = synchronizer.sync("F001", "7", SETTINGS, TODAY);

        assertThat(result.skippedReason()).isEqualTo("no es hembra");
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("RT5: sin finca de contexto se omite")
    void withoutFarm_isSkipped() {
        when(bovineRepository.findById(7)).thenReturn(Optional.of(identity("female", null)));

        ReproductiveTaskSynchronizer.SyncResult result = synchronizer.sync(null, "7", SETTINGS, TODAY);

        assertThat(result.skippedReason()).isEqualTo("sin finca de contexto");
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("H9: la tarea usa la finca de contexto (F001), no el farmId de la identidad (FARM#001 en datos reales)")
    void taskFarm_isContextFarmNotIdentityFarm() {
        when(bovineRepository.findById(7)).thenReturn(Optional.of(identity("FEMALE", "FARM#001")));
        event("S1", "INSEMINACION", "2026-10-01", "{}");

        synchronizer.sync("F001", "7", SETTINGS, TODAY);

        ArgumentCaptor<ReproductiveTaskItem> task = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository).createIfAbsent(task.capture());
        assertThat(task.getValue().getFarmId()).isEqualTo("F001");
        assertThat(task.getValue().getGsi1pk()).isEqualTo("FARM#F001#OPEN");
        ArgumentCaptor<ServiceFollowUpItem> followUp = ArgumentCaptor.forClass(ServiceFollowUpItem.class);
        verify(taskRepository).saveFollowUp(followUp.capture());
        assertThat(followUp.getValue().getGsi1pk()).isEqualTo("FARMFOLLOWUP#F001#OPEN");
    }

    @Test
    @DisplayName("H9: una identidad sin farmId (dato legacy) igual se sincroniza con la finca de contexto")
    void identityWithoutFarm_usesContextFarm() {
        when(bovineRepository.findById(7)).thenReturn(Optional.of(identity("female", null)));
        event("S1", "MONTA", "2026-10-01", "{}");

        assertThat(synchronizer.sync("F001", "7", SETTINGS, TODAY).created()).isEqualTo(1);
    }

    @Test
    @DisplayName("G4: bovino inactivo no crea tareas y cancela las abiertas")
    void inactive_cancelsAndDoesNotCreate() {
        ProfileLifecycle lifecycle = ProfileLifecycle.builder().pk(PK).sk("PROFILE#LIFECYCLE").build();
        lifecycle.setStatus(LifecycleStatus.SOLD);
        lifecycle.setEnabled(false);
        when(lifecycleRepository.findById(PK, "PROFILE#LIFECYCLE")).thenReturn(Optional.of(lifecycle));
        event("S1", "MONTA", "2026-10-01", "{}");
        event("S2", "MONTA", "2026-10-22", "{}");
        when(taskRepository.findTasksByBovine("7"))
                .thenReturn(List.of(stored("TASK#R1_DIAGNOSIS#S2", "PENDIENTE", "2026-12-01")));

        synchronizer.sync("F001", "7", SETTINGS, LocalDate.parse("2026-10-25"));

        verify(taskRepository, never()).createIfAbsent(any());
        ArgumentCaptor<ReproductiveTaskItem> saved = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository).saveIfStatus(saved.capture(), eq("PENDIENTE"));
        assertThat(saved.getValue().getStatus()).isEqualTo("CANCELADA");
    }

    @Test
    @DisplayName("CA18: las tareas de ciclos anteriores no se crean en la carga inicial")
    void previousCycleTasks_areNotCreated() {
        event("S1", "MONTA", "2025-06-01", "{}");
        event("D1", "DIAGNOSTICO_PRENEZ", "2025-07-11", "{\"result\":\"PRENIADA\"}");
        event("P1", "PARTO", "2026-03-06", "{}");

        synchronizer.sync("F001", "7", SETTINGS, LocalDate.parse("2026-03-10"));

        ArgumentCaptor<ReproductiveTaskItem> created = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository, times(2)).createIfAbsent(created.capture());
        assertThat(created.getAllValues()).extracting(ReproductiveTaskItem::getRuleCode)
                .containsExactlyInAnyOrder("R5_POSTPARTUM_CHECK", "R5_SERVE");
    }

    @Test
    @DisplayName("Carrera: si la tarea ya existe al crear, no se cuenta como creada")
    void createRace_isNotCounted() {
        event("S1", "MONTA", "2026-10-01", "{}");
        when(taskRepository.createIfAbsent(any())).thenReturn(false);

        assertThat(synchronizer.sync("F001", "7", SETTINGS, TODAY).created()).isZero();
    }

    @Test
    @DisplayName("P4: con lactancia activa el diagnóstico positivo también programa SECAR")
    void lactatingCow_getsDryOff() {
        ProfileReproductive reproductive = ProfileReproductive.builder()
                .pk(PK).sk("PROFILE#REPRODUCTIVE").currentLactationId("LACT#01").build();
        when(reproductiveRepository.findById(PK, "PROFILE#REPRODUCTIVE")).thenReturn(Optional.of(reproductive));
        when(lactancyRepository.findById(PK, "LACT#01"))
                .thenReturn(Optional.of(ProfileLactancy.builder().pk(PK).sk("LACT#01").status("LACTATING").build()));
        event("S1", "MONTA", "2026-10-01", "{}");
        event("D1", "DIAGNOSTICO_PRENEZ", "2026-11-10", "{\"result\":\"PRENIADA\"}");

        synchronizer.sync("F001", "7", SETTINGS, LocalDate.parse("2026-11-10"));

        ArgumentCaptor<ReproductiveTaskItem> created = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository, times(4)).createIfAbsent(created.capture());
        assertThat(created.getAllValues()).extracting(ReproductiveTaskItem::getRuleCode).contains("R2_DRY_OFF");
    }

    @Test
    @DisplayName("H3: si otra sincronización cambió la tarea después de leerla, la transición se descarta")
    void concurrentChange_discardsTransition() {
        event("S1", "INSEMINACION", "2026-10-01", "{}");
        event("D1", "DIAGNOSTICO_PRENEZ", "2026-11-10", "{\"result\":\"PRENIADA\"}");
        when(taskRepository.findTasksByBovine("7"))
                .thenReturn(List.of(stored("TASK#R1_DIAGNOSIS#S1", "PENDIENTE", "2026-11-10")));
        when(taskRepository.saveIfStatus(any(), eq("PENDIENTE"))).thenReturn(false);

        ReproductiveTaskSynchronizer.SyncResult result =
                synchronizer.sync("F001", "7", SETTINGS, LocalDate.parse("2026-11-10"));

        assertThat(result.updated()).isZero();
        verify(lambdaContext).logInfo(any(), org.mockito.ArgumentMatchers.contains("concurrencia"));
    }

    @Test
    @DisplayName("H7 / G3: una tarea VENCIDA guardada pasa a HECHA marcada como tardía")
    void overdueStored_becomesLateCompletion() {
        event("P1", "PARTO", "2027-07-05", "{\"calfGender\":\"HEMBRA\",\"birthType\":\"NORMAL\"}");
        event("CP", "CONTROL_POSPARTO", "2027-07-30", "{\"uterineStatus\":\"NORMAL\"}");
        ReproductiveTaskItem overdue = stored("TASK#R5_POSTPARTUM_CHECK#P1", "VENCIDA", "2027-07-26");
        when(taskRepository.findTasksByBovine("7")).thenReturn(List.of(overdue,
                stored("TASK#R5_SERVE#P1", "PENDIENTE", "2027-09-03")));

        synchronizer.sync("F001", "7", SETTINGS, LocalDate.parse("2027-07-30"));

        ArgumentCaptor<ReproductiveTaskItem> saved = ArgumentCaptor.forClass(ReproductiveTaskItem.class);
        verify(taskRepository).saveIfStatus(saved.capture(), eq("VENCIDA"));
        assertThat(saved.getValue().getStatus()).isEqualTo("HECHA");
        assertThat(saved.getValue().getLateCompletion()).isTrue();
        assertThat(saved.getValue().getClosedByEventId()).isEqualTo("CP");
        assertThat(saved.getValue().getGsi1pk()).isNull();
    }

    @Test
    @DisplayName("H7: un id de bovino no numérico (legacy) se omite sin tocar la agenda")
    void nonNumericId_isSkipped() {
        ReproductiveTaskSynchronizer.SyncResult result = synchronizer.sync("F001", "B-9", SETTINGS, TODAY);

        assertThat(result.skippedReason()).isEqualTo("bovino sin identidad");
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("Sin parámetros explícitos usa el proveedor de settings")
    void defaultSync_usesSettingsProvider() {
        when(settingsProvider.current()).thenReturn(SETTINGS);
        event("S1", "MONTA", "2026-10-01", "{}");

        synchronizer.sync("F001", "7");

        verify(settingsProvider).current();
        verify(taskRepository).createIfAbsent(any());
        verify(lambdaContext, never()).logException(any(), anyString(), any());
    }
}
