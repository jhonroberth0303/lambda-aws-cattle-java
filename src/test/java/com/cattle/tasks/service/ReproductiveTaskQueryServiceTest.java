package com.cattle.tasks.service;

import com.cattle.config.AppProperties;
import com.cattle.tasks.dto.BovineTasksDTO;
import com.cattle.tasks.dto.ReproductiveTaskDTO;
import com.cattle.tasks.dto.ServiceFollowUpDTO;
import com.cattle.tasks.entity.ReproductiveTaskItem;
import com.cattle.tasks.entity.ServiceFollowUpItem;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import com.cattle.tasks.repository.ReproductiveTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
@DisplayName("ReproductiveTaskQueryService Tests")
class ReproductiveTaskQueryServiceTest {

    @Mock private ReproductiveTaskRepository repository;
    @Mock private ReproductiveTaskSettingsProvider settingsProvider;

    private ReproductiveTaskQueryService service;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new ReproductiveTaskQueryService(repository, settingsProvider, new AppProperties());
        today = service.today();
        when(settingsProvider.current()).thenReturn(ReproductiveTaskSettings.defaults());
    }

    private static ReproductiveTaskItem task(String id, String farmId, String status, LocalDate due) {
        ReproductiveTaskItem item = new ReproductiveTaskItem();
        item.setTaskId(id);
        item.setBovineId("7");
        item.setFarmId(farmId);
        item.setStatus(status);
        item.setDueDate(due.toString());
        item.setWindowEnd(due.toString());
        return item;
    }

    private static ServiceFollowUpItem followUp(String bovineId, LocalDate serviceDate, int serviceNumber) {
        ServiceFollowUpItem item = new ServiceFollowUpItem();
        item.setBovineId(bovineId);
        item.setFarmId("F001");
        item.setLastServiceDate(serviceDate.toString());
        item.setServiceNumber(serviceNumber);
        item.setOpen(true);
        return item;
    }

    @Test
    @DisplayName("CA4: sin 'to' la agenda consulta hasta hoy + 6 días")
    void farmAgenda_defaultsToOneWeek() {
        when(repository.findOpenTasksByFarm(eq("F001"), any())).thenReturn(List.of());

        service.farmAgenda("F001", null);

        verify(repository).findOpenTasksByFarm("F001", today.plusDays(6));
    }

    @Test
    @DisplayName("CA4: una pendiente cuya ventana ya pasó se muestra vencida sin esperar al batch")
    void farmAgenda_marksOverdueOnRead() {
        when(repository.findOpenTasksByFarm(eq("F001"), any())).thenReturn(List.of(
                task("B", "F001", "PENDIENTE", today.plusDays(2)),
                task("A", "F001", "PENDIENTE", today.minusDays(1))));

        List<ReproductiveTaskDTO> agenda = service.farmAgenda("F001", null);

        assertThat(agenda).extracting(ReproductiveTaskDTO::getTaskId).containsExactly("A", "B");
        assertThat(agenda.get(0).isOverdue()).isTrue();
        assertThat(agenda.get(0).getStatus()).isEqualTo("VENCIDA");
        assertThat(agenda.get(1).isOverdue()).isFalse();
    }

    @Test
    @DisplayName("Tareas del bovino: solo de la finca pedida, abiertas primero, y su semáforo")
    void bovineTasks_filtersFarmAndOrdersOpenFirst() {
        when(repository.findTasksByBovine("7")).thenReturn(List.of(
                task("DONE", "F001", "HECHA", today.minusDays(30)),
                task("OPEN", "F001", "PENDIENTE", today.plusDays(10)),
                task("OTHER", "F999", "PENDIENTE", today)));
        when(repository.findFollowUp("7")).thenReturn(Optional.of(followUp("7", today.minusDays(45), 1)));

        BovineTasksDTO dto = service.bovineTasks("F001", "7");

        assertThat(dto.getTasks()).extracting(ReproductiveTaskDTO::getTaskId).containsExactly("OPEN", "DONE");
        assertThat(dto.getFollowUp().getColor()).isEqualTo("ORANGE");
        assertThat(dto.getFollowUp().getDaysSinceService()).isEqualTo(45);
    }

    @Test
    @DisplayName("CA20/CA21: semáforo ordenado de rojo a verde y por días; la repetidora cuenta como roja")
    void serviceFollowUps_areSortedRedFirst() {
        when(repository.findOpenFollowUps("F001")).thenReturn(List.of(
                followUp("green", today.minusDays(10), 1),
                followUp("red", today.minusDays(70), 1),
                followUp("orange", today.minusDays(45), 1),
                followUp("repeat", today, 3)));

        List<ServiceFollowUpDTO> result = service.serviceFollowUps("F001");

        assertThat(result).extracting(ServiceFollowUpDTO::getBovineId)
                .containsExactly("red", "repeat", "orange", "green");
        assertThat(result.get(1).isRepeatBreeder()).isTrue();
    }
}
