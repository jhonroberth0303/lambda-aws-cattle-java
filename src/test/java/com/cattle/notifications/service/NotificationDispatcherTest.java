package com.cattle.notifications.service;

import com.cattle.config.LambdaContext;
import com.cattle.exceptions.RepositoryException;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationStatus;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.entity.NotificationItem;
import com.cattle.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
class NotificationDispatcherTest {

    private static final Instant FIXED = Instant.parse("2026-09-09T14:00:00Z");

    @Mock
    private NotificationRepository repository;

    @Mock
    private LambdaContext lambdaContext;

    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        openMocks(this);
        dispatcher = new NotificationDispatcher(repository, lambdaContext, Clock.fixed(FIXED, ZoneOffset.UTC));
    }

    private static NotificationDraft draft(String dedupeKey) {
        return NotificationDraft.builder()
                .farmId("001")
                .type(NotificationType.MILKING_AM_REMINDER)
                .title("Ordeño de la mañana pendiente")
                .body("Faltan 3 vacas")
                .deeplink("/lactancia?focus=registro")
                .dataJson("{\"pendingCows\":3}")
                .dedupeKey(dedupeKey)
                .build();
    }

    @Test
    void dispatch_claimedDraft_persistsMaterializedNotification() {
        when(repository.tryClaimDedupe(eq("001"), eq("k1"), anyLong())).thenReturn(true);

        NotificationDispatcher.DispatchResult result = dispatcher.dispatch(List.of(draft("k1")));

        ArgumentCaptor<NotificationItem> captor = ArgumentCaptor.forClass(NotificationItem.class);
        verify(repository).save(captor.capture());
        NotificationItem saved = captor.getValue();

        assertAll(
                () -> assertEquals(1, result.created()),
                () -> assertEquals(0, result.deduplicated()),
                () -> assertEquals(0, result.failed()),
                () -> assertEquals("FARM#001", saved.getPk()),
                () -> assertTrue(saved.getSk().startsWith("NOTIF#")),
                () -> assertEquals(NotificationStatus.UNREAD, saved.getStatus()),
                () -> assertEquals("k1", saved.getDedupeKey()),
                () -> assertEquals(FIXED.toString(), saved.getCreatedAt()),
                () -> assertEquals(
                        FIXED.plus(NotificationType.MILKING_AM_REMINDER.retentionDays(), ChronoUnit.DAYS).getEpochSecond(),
                        saved.getTtl())
        );
    }

    @Test
    void dispatch_dedupeAlreadyClaimed_doesNotPersist() {
        when(repository.tryClaimDedupe(eq("001"), eq("k1"), anyLong())).thenReturn(false);

        NotificationDispatcher.DispatchResult result = dispatcher.dispatch(List.of(draft("k1")));

        verify(repository, never()).save(any());
        assertEquals(0, result.created());
        assertEquals(1, result.deduplicated());
    }

    @Test
    void dispatch_repositoryFailsForOneDraft_isolatesAndContinues() {
        when(repository.tryClaimDedupe(eq("001"), eq("boom"), anyLong()))
                .thenThrow(new RepositoryException("dynamo down"));
        when(repository.tryClaimDedupe(eq("001"), eq("ok"), anyLong())).thenReturn(true);

        NotificationDispatcher.DispatchResult result = dispatcher.dispatch(List.of(draft("boom"), draft("ok")));

        verify(repository).save(any());
        assertAll(
                () -> assertEquals(1, result.created()),
                () -> assertEquals(1, result.failed()),
                () -> assertEquals(2, result.total())
        );
    }

    @Test
    void dispatch_emptyList_isNoOp() {
        NotificationDispatcher.DispatchResult result = dispatcher.dispatch(List.of());

        verify(repository, never()).save(any());
        assertEquals(0, result.total());
    }
}
