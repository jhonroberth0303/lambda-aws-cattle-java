package com.cattle.notifications.producer;

import com.cattle.config.LambdaContext;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.ScheduleWindow;
import com.cattle.tasks.dto.ReproductiveTaskDTO;
import com.cattle.tasks.service.ReproductiveTaskQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
@DisplayName("ReproductiveTasksDigestProducer Tests")
class ReproductiveTasksDigestProducerTest {

    private static final ZonedDateTime NOW =
            ZonedDateTime.of(2026, 10, 7, 9, 0, 0, 0, ZoneId.of("America/Bogota"));
    private static final LocalDate TODAY = NOW.toLocalDate();

    @Mock private ReproductiveTaskQueryService queryService;
    @Mock private LambdaContext lambdaContext;

    private ReproductiveTasksDigestProducer producer;

    @BeforeEach
    void setUp() {
        openMocks(this);
        producer = new ReproductiveTasksDigestProducer(queryService, new ObjectMapper(), lambdaContext, "F001");
    }

    private static ReproductiveTaskDTO task(boolean overdue) {
        return ReproductiveTaskDTO.builder().taskId("t").overdue(overdue).build();
    }

    @Test
    @DisplayName("Corre en la ventana MORNING con su propio tipo")
    void declaresTypeAndWindow() {
        assertThat(producer.type()).isEqualTo(NotificationType.REPRODUCTIVE_TASKS_DIGEST);
        assertThat(producer.window()).isEqualTo(ScheduleWindow.MORNING);
    }

    @Test
    @DisplayName("CA5: 3 para hoy y 2 vencidas generan un resumen con deeplink a la agenda y dedupe diario")
    void summarizesTodayAndOverdue() {
        when(queryService.farmAgenda("F001", TODAY, TODAY)).thenReturn(List.of(
                task(false), task(false), task(false), task(true), task(true)));

        List<NotificationDraft> drafts = producer.evaluate(NOW);

        assertThat(drafts).hasSize(1);
        NotificationDraft draft = drafts.get(0);
        assertThat(draft.body()).isEqualTo("3 tareas para hoy, 2 vencidas.");
        assertThat(draft.deeplink()).isEqualTo("/agenda");
        assertThat(draft.farmId()).isEqualTo("F001");
        assertThat(draft.dedupeKey()).isEqualTo("REPRODUCTIVE_TASKS_DIGEST#F001#2026-10-07");
        assertThat(draft.dataJson()).contains("\"forToday\":3").contains("\"overdue\":2");
    }

    @Test
    @DisplayName("CA5: sin tareas ni vencidas no se notifica")
    void nothingDue_noNotification() {
        when(queryService.farmAgenda("F001", TODAY, TODAY)).thenReturn(List.of());

        assertThat(producer.evaluate(NOW)).isEmpty();
    }

    @Test
    @DisplayName("Singular correcto en el mensaje")
    void singularBody() {
        assertThat(ReproductiveTasksDigestProducer.body(1, 1)).isEqualTo("1 tarea para hoy, 1 vencida.");
        assertThat(ReproductiveTasksDigestProducer.body(0, 2)).isEqualTo("0 tareas para hoy, 2 vencidas.");
    }
}
