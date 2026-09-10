package com.cattle.notifications.service;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationStatus;
import com.cattle.notifications.entity.NotificationItem;
import com.cattle.notifications.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Materializa los {@link NotificationDraft} que emiten los productores:
 * reclama la clave de deduplicación y persiste la notificación en el canal in-app.
 * <p>
 * Cada draft se procesa de forma aislada: un fallo se registra y no interrumpe el resto.
 * El canal web push se añade aquí en la Fase 2.
 */
@Component
public class NotificationDispatcher {

    /** Cobertura del centinela de deduplicación: basta con abarcar el día (la clave lleva la fecha). */
    static final int DEDUPE_TTL_DAYS = 2;

    private final NotificationRepository repository;
    private final LambdaContext lambdaContext;
    private final Clock clock;

    @Autowired
    public NotificationDispatcher(NotificationRepository repository, LambdaContext lambdaContext) {
        this(repository, lambdaContext, Clock.systemUTC());
    }

    NotificationDispatcher(NotificationRepository repository, LambdaContext lambdaContext, Clock clock) {
        this.repository = repository;
        this.lambdaContext = lambdaContext;
        this.clock = clock;
    }

    public DispatchResult dispatch(List<NotificationDraft> drafts) {
        int created = 0;
        int deduplicated = 0;
        int failed = 0;

        for (NotificationDraft draft : drafts) {
            try {
                long dedupeTtl = clock.instant().plus(DEDUPE_TTL_DAYS, ChronoUnit.DAYS).getEpochSecond();
                if (!repository.tryClaimDedupe(draft.farmId(), draft.dedupeKey(), dedupeTtl)) {
                    deduplicated++;
                    continue;
                }
                repository.save(materialize(draft));
                created++;
            } catch (Exception ex) {
                lambdaContext.logException(LogType.SERVICE,
                        "Failed to dispatch notification. type=" + draft.type() + ", farmId=" + draft.farmId()
                                + ", dedupeKey=" + draft.dedupeKey(), ex);
                failed++;
            }
        }

        lambdaContext.logInfo(LogType.SERVICE, "Notification dispatch finished. created=" + created
                + ", deduplicated=" + deduplicated + ", failed=" + failed);
        return new DispatchResult(created, deduplicated, failed);
    }

    private NotificationItem materialize(NotificationDraft draft) {
        Instant now = clock.instant();
        String notificationId = now.toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);

        NotificationItem item = new NotificationItem();
        item.setPk(NotificationItem.partitionKey(draft.farmId()));
        item.setSk(NotificationItem.sortKey(notificationId));
        item.setNotificationId(notificationId);
        item.setFarmId(draft.farmId());
        item.setType(draft.type());
        item.setTitle(draft.title());
        item.setBody(draft.body());
        item.setDeeplink(draft.deeplink());
        item.setDataJson(draft.dataJson());
        item.setStatus(NotificationStatus.UNREAD);
        item.setDedupeKey(draft.dedupeKey());
        item.setCreatedAt(now.toString());
        item.setTtl(now.plus(draft.type().retentionDays(), ChronoUnit.DAYS).getEpochSecond());
        return item;
    }

    /**
     * Resultado agregado de un despacho.
     *
     * @param created       notificaciones nuevas persistidas
     * @param deduplicated  drafts descartados por existir ya hoy
     * @param failed        drafts que fallaron y quedaron registrados
     */
    public record DispatchResult(int created, int deduplicated, int failed) {

        public int total() {
            return created + deduplicated + failed;
        }
    }
}
