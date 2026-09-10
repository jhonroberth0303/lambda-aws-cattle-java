package com.cattle.notifications.service;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.notifications.NotificationStatus;
import com.cattle.notifications.dto.NotificationDTO;
import com.cattle.notifications.entity.NotificationItem;
import com.cattle.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

/**
 * Operaciones de la campana: listar, contar no leídas y marcar leídas.
 * Traduce errores de repositorio a excepciones de servicio y no expone entidades.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** Tope de no leídas que se recorren para contar o marcar en bloque. */
    static final int UNREAD_CAP = 200;

    private final NotificationRepository repository;
    private final LambdaContext lambdaContext;

    public List<NotificationDTO> listByFarm(String farmId, int limit, boolean unreadOnly) {
        return guard("listing notifications", () -> {
            List<NotificationItem> items = unreadOnly
                    ? repository.findUnreadByFarm(farmId, limit)
                    : repository.findByFarm(farmId, limit);
            return items.stream().map(NotificationService::toDTO).toList();
        });
    }

    public long unreadCount(String farmId) {
        return guard("counting unread notifications",
                () -> (long) repository.findUnreadByFarm(farmId, UNREAD_CAP).size());
    }

    public void markRead(String farmId, String notificationId) {
        guard("marking notification as read", () -> {
            repository.findById(farmId, notificationId)
                    .filter(item -> item.getStatus() != NotificationStatus.READ)
                    .ifPresent(item -> repository.save(markRead(item)));
            return null;
        });
    }

    public int markAllRead(String farmId) {
        return guard("marking all notifications as read", () -> {
            List<NotificationItem> unread = repository.findUnreadByFarm(farmId, UNREAD_CAP);
            unread.forEach(item -> repository.save(markRead(item)));
            return unread.size();
        });
    }

    private static NotificationItem markRead(NotificationItem item) {
        item.setStatus(NotificationStatus.READ);
        item.setReadAt(Instant.now().toString());
        return item;
    }

    private static NotificationDTO toDTO(NotificationItem item) {
        return NotificationDTO.builder()
                .notificationId(item.getNotificationId())
                .type(item.getType() != null ? item.getType().name() : null)
                .title(item.getTitle())
                .body(item.getBody())
                .deeplink(item.getDeeplink())
                .dataJson(item.getDataJson())
                .status(item.getStatus() != null ? item.getStatus().name() : null)
                .createdAt(item.getCreatedAt())
                .readAt(item.getReadAt())
                .build();
    }

    private <T> T guard(String action, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (ServiceException | ProcessingException ex) {
            throw ex;
        } catch (RepositoryException ex) {
            lambdaContext.logException(LogType.SERVICE, "Repository error while " + action, ex);
            throw new ServiceException("Repository error while " + action, ex);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.SERVICE, "Unexpected error while " + action, ex);
            throw new ProcessingException("Unexpected error while " + action, ex);
        }
    }
}
