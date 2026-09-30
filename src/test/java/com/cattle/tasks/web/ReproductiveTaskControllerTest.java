package com.cattle.tasks.web;

import com.cattle.config.LambdaContext;
import com.cattle.tasks.dto.BovineTasksDTO;
import com.cattle.tasks.dto.ReproductiveTaskDTO;
import com.cattle.tasks.dto.ServiceFollowUpDTO;
import com.cattle.tasks.service.ReproductiveTaskQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class ReproductiveTaskControllerTest {

    @Mock private ReproductiveTaskQueryService queryService;
    @Mock private LambdaContext lambdaContext;

    private ReproductiveTaskController controller;

    @BeforeEach
    void setUp() {
        openMocks(this);
        controller = new ReproductiveTaskController(queryService, lambdaContext);
    }

    @Test
    void farmAgenda_withoutTo_delegatesNull() {
        when(queryService.farmAgenda("F001", null)).thenReturn(List.of(ReproductiveTaskDTO.builder().taskId("t").build()));

        ResponseEntity<List<ReproductiveTaskDTO>> response = controller.farmAgenda("F001", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void farmAgenda_withTo_parsesDate() {
        controller.farmAgenda("F001", "2026-10-12");

        verify(queryService).farmAgenda("F001", LocalDate.parse("2026-10-12"));
    }

    @Test
    void farmAgenda_invalidTo_isBadRequest() {
        assertThatThrownBy(() -> controller.farmAgenda("F001", "12/10/2026"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yyyy-MM-dd");
        verify(queryService, never()).farmAgenda(any(), any());
    }

    @Test
    void bovineTasks_delegates() {
        BovineTasksDTO dto = BovineTasksDTO.builder().bovineId("7").tasks(List.of()).build();
        when(queryService.bovineTasks("F001", "7")).thenReturn(dto);

        assertThat(controller.bovineTasks("F001", "7").getBody()).isSameAs(dto);
    }

    @Test
    void serviceFollowUps_delegates() {
        when(queryService.serviceFollowUps("F001"))
                .thenReturn(List.of(ServiceFollowUpDTO.builder().bovineId("7").color("RED").build()));

        assertThat(controller.serviceFollowUps("F001").getBody()).hasSize(1);
    }
}
