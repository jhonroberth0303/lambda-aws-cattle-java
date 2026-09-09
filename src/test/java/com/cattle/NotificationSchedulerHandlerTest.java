package com.cattle;

import com.amazonaws.services.lambda.runtime.Context;
import com.cattle.config.LambdaContext;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.ScheduleWindow;
import com.cattle.notifications.producer.NotificationProducer;
import com.cattle.notifications.service.NotificationDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
class NotificationSchedulerHandlerTest {

    @Mock
    private NotificationProducer morningProducer;

    @Mock
    private NotificationProducer middayProducer;

    @Mock
    private NotificationDispatcher dispatcher;

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private Context awsContext;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(morningProducer.window()).thenReturn(ScheduleWindow.MORNING);
        when(morningProducer.type()).thenReturn(NotificationType.MILKING_AM_REMINDER);
        lenientWindow(middayProducer, ScheduleWindow.MIDDAY);
        when(awsContext.getAwsRequestId()).thenReturn("req-1");
        when(dispatcher.dispatch(anyList())).thenReturn(new NotificationDispatcher.DispatchResult(1, 0, 0));
    }

    private void lenientWindow(NotificationProducer producer, ScheduleWindow window) {
        org.mockito.Mockito.lenient().when(producer.window()).thenReturn(window);
        org.mockito.Mockito.lenient().when(producer.type()).thenReturn(NotificationType.MILKING_AM_REMINDER);
    }

    private NotificationSchedulerHandler handler() {
        return new NotificationSchedulerHandler(
                List.of(morningProducer, middayProducer), dispatcher, lambdaContext,
                ZoneId.of("America/Bogota"));
    }

    @Test
    void handleRequest_withWindow_runsOnlyMatchingProducers() {
        when(morningProducer.evaluate(org.mockito.ArgumentMatchers.any(ZonedDateTime.class)))
                .thenReturn(List.of(draft("k1")));

        Map<String, Object> response = handler().handleRequest(Map.of("window", "MORNING"), awsContext);

        verify(morningProducer).evaluate(org.mockito.ArgumentMatchers.any());
        verify(middayProducer, never()).evaluate(org.mockito.ArgumentMatchers.any());

        ArgumentCaptor<List<NotificationDraft>> captor = ArgumentCaptor.forClass(List.class);
        verify(dispatcher).dispatch(captor.capture());
        assertEquals(1, captor.getValue().size());

        assertEquals("MORNING", response.get("window"));
        assertEquals(1, response.get("drafts"));
        assertEquals(1, response.get("created"));
        assertEquals("req-1", response.get("requestId"));
    }

    @Test
    void handleRequest_withoutWindow_runsAllProducers() {
        when(morningProducer.evaluate(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(draft("k1")));
        when(middayProducer.evaluate(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(draft("k2")));

        Map<String, Object> response = handler().handleRequest(Map.of(), awsContext);

        verify(morningProducer).evaluate(org.mockito.ArgumentMatchers.any());
        verify(middayProducer).evaluate(org.mockito.ArgumentMatchers.any());
        assertEquals("ALL", response.get("window"));
        assertEquals(2, response.get("drafts"));
    }

    @Test
    void handleRequest_producerThrows_isIsolatedAndOthersStillRun() {
        when(morningProducer.evaluate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("boom"));
        when(middayProducer.evaluate(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(draft("k2")));

        Map<String, Object> response = handler().handleRequest(Map.of(), awsContext);

        verify(dispatcher).dispatch(anyList());
        assertEquals(1, response.get("drafts"));
    }

    private static NotificationDraft draft(String dedupeKey) {
        return NotificationDraft.builder()
                .farmId("001")
                .type(NotificationType.MILKING_AM_REMINDER)
                .title("t")
                .body("b")
                .deeplink("/lactancia")
                .dedupeKey(dedupeKey)
                .build();
    }
}
