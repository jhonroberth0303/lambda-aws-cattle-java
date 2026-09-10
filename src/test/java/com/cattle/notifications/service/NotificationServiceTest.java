package com.cattle.notifications.service;

import com.cattle.config.LambdaContext;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.notifications.NotificationStatus;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.dto.NotificationDTO;
import com.cattle.notifications.entity.NotificationItem;
import com.cattle.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
class NotificationServiceTest {

    private static final String FARM_ID = "001";

    @Mock
    private NotificationRepository repository;

    @Mock
    private LambdaContext lambdaContext;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new NotificationService(repository, lambdaContext);
    }

    @Test
    void listByFarm_mapsEntitiesToDto() {
        when(repository.findByFarm(FARM_ID, 30)).thenReturn(List.of(item("n1", NotificationStatus.UNREAD)));

        List<NotificationDTO> result = service.listByFarm(FARM_ID, 30, false);

        assertEquals(1, result.size());
        NotificationDTO dto = result.get(0);
        assertEquals("n1", dto.getNotificationId());
        assertEquals("MILKING_AM_REMINDER", dto.getType());
        assertEquals("UNREAD", dto.getStatus());
    }

    @Test
    void listByFarm_unreadOnly_usesUnreadQuery() {
        when(repository.findUnreadByFarm(FARM_ID, 15)).thenReturn(List.of());

        service.listByFarm(FARM_ID, 15, true);

        verify(repository).findUnreadByFarm(FARM_ID, 15);
        verify(repository, never()).findByFarm(eq(FARM_ID), anyInt());
    }

    @Test
    void unreadCount_returnsSizeOfUnreadQuery() {
        when(repository.findUnreadByFarm(FARM_ID, NotificationService.UNREAD_CAP))
                .thenReturn(List.of(item("a", NotificationStatus.UNREAD), item("b", NotificationStatus.UNREAD)));

        assertEquals(2L, service.unreadCount(FARM_ID));
    }

    @Test
    void markRead_unreadNotification_persistsAsRead() {
        NotificationItem stored = item("n9", NotificationStatus.UNREAD);
        when(repository.findById(FARM_ID, "n9")).thenReturn(Optional.of(stored));

        service.markRead(FARM_ID, "n9");

        ArgumentCaptor<NotificationItem> captor = ArgumentCaptor.forClass(NotificationItem.class);
        verify(repository).save(captor.capture());
        assertEquals(NotificationStatus.READ, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getReadAt());
    }

    @Test
    void markRead_alreadyRead_isNoOp() {
        when(repository.findById(FARM_ID, "n9")).thenReturn(Optional.of(item("n9", NotificationStatus.READ)));

        service.markRead(FARM_ID, "n9");

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void markRead_missingNotification_isNoOp() {
        when(repository.findById(FARM_ID, "ghost")).thenReturn(Optional.empty());

        service.markRead(FARM_ID, "ghost");

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void markAllRead_persistsEachUnreadAndReturnsCount() {
        when(repository.findUnreadByFarm(FARM_ID, NotificationService.UNREAD_CAP))
                .thenReturn(List.of(item("a", NotificationStatus.UNREAD), item("b", NotificationStatus.UNREAD)));

        int updated = service.markAllRead(FARM_ID);

        assertEquals(2, updated);
        verify(repository, org.mockito.Mockito.times(2)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void repositoryException_isTranslatedToServiceException() {
        when(repository.findByFarm(FARM_ID, 30)).thenThrow(new RepositoryException("dynamo down"));

        assertThrows(ServiceException.class, () -> service.listByFarm(FARM_ID, 30, false));
    }

    private static NotificationItem item(String id, NotificationStatus status) {
        NotificationItem item = new NotificationItem();
        item.setNotificationId(id);
        item.setPk(NotificationItem.partitionKey(FARM_ID));
        item.setSk(NotificationItem.sortKey(id));
        item.setFarmId(FARM_ID);
        item.setType(NotificationType.MILKING_AM_REMINDER);
        item.setTitle("Título");
        item.setBody("Cuerpo");
        item.setDeeplink("/lactancia");
        item.setDataJson("{}");
        item.setStatus(status);
        item.setCreatedAt("2026-09-09T09:00:00Z");
        return item;
    }
}
