package com.cattle.notifications.web;

import com.cattle.config.LambdaContext;
import com.cattle.notifications.dto.NotificationDTO;
import com.cattle.notifications.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("controller")
class NotificationControllerTest {

    private static final String FARM_ID = "001";

    @Mock
    private NotificationService notificationService;

    @Mock
    private LambdaContext lambdaContext;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        openMocks(this);
        controller = new NotificationController(notificationService, lambdaContext);
    }

    @Test
    void list_defaultParams_returnsAllWithLimit30() {
        when(notificationService.listByFarm(FARM_ID, 30, false)).thenReturn(List.of(dto("n1")));

        ResponseEntity<List<NotificationDTO>> response = controller.list(FARM_ID, 30, "all");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(notificationService).listByFarm(FARM_ID, 30, false);
    }

    @Test
    void list_statusUnread_delegatesUnreadOnly() {
        when(notificationService.listByFarm(FARM_ID, 30, true)).thenReturn(List.of());

        controller.list(FARM_ID, 30, "UNREAD");

        verify(notificationService).listByFarm(FARM_ID, 30, true);
    }

    @Test
    void list_limitAboveMax_isClampedTo100() {
        when(notificationService.listByFarm(FARM_ID, 100, false)).thenReturn(List.of());

        controller.list(FARM_ID, 5000, "all");

        verify(notificationService).listByFarm(FARM_ID, 100, false);
    }

    @Test
    void list_limitBelowOne_fallsBackToDefault() {
        when(notificationService.listByFarm(FARM_ID, 30, false)).thenReturn(List.of());

        controller.list(FARM_ID, 0, "all");

        verify(notificationService).listByFarm(FARM_ID, 30, false);
    }

    @Test
    void unreadCount_returnsCountPayload() {
        when(notificationService.unreadCount(FARM_ID)).thenReturn(4L);

        ResponseEntity<Map<String, Long>> response = controller.unreadCount(FARM_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4L, response.getBody().get("count"));
    }

    @Test
    void markRead_returnsNoContentAndDelegates() {
        ResponseEntity<Void> response = controller.markRead(FARM_ID, "n1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationService).markRead(FARM_ID, "n1");
    }

    @Test
    void markAllRead_returnsUpdatedCount() {
        when(notificationService.markAllRead(FARM_ID)).thenReturn(3);

        ResponseEntity<Map<String, Integer>> response = controller.markAllRead(FARM_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().get("updated"));
    }

    private static NotificationDTO dto(String id) {
        return NotificationDTO.builder().notificationId(id).type("MILKING_AM_REMINDER").status("UNREAD").build();
    }
}
