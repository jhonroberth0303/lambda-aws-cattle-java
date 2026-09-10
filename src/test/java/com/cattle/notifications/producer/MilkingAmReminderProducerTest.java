package com.cattle.notifications.producer;

import com.cattle.config.LambdaContext;
import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.ScheduleWindow;
import com.cattle.notifications.producer.milking.MorningMilkingStatus;
import com.cattle.notifications.producer.milking.MorningMilkingStatusService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
class MilkingAmReminderProducerTest {

    private static final String SITE_ID = "001";
    private static final String FARM_ID = "F001";
    private static final ZonedDateTime NOW =
            ZonedDateTime.of(2026, 9, 9, 9, 0, 0, 0, ZoneId.of("America/Bogota"));

    @Mock
    private MorningMilkingStatusService milkingStatusService;

    @Mock
    private LambdaContext lambdaContext;

    private MilkingAmReminderProducer producer;

    @BeforeEach
    void setUp() {
        openMocks(this);
        producer = new MilkingAmReminderProducer(milkingStatusService, new ObjectMapper(), lambdaContext, SITE_ID, FARM_ID);
    }

    @Test
    void metadata_isMilkingReminderInMorningWindow() {
        assertEquals(NotificationType.MILKING_AM_REMINDER, producer.type());
        assertEquals(ScheduleWindow.MORNING, producer.window());
    }

    @Test
    void evaluate_noPendingCows_emitsNothing() {
        when(milkingStatusService.statusFor(SITE_ID, LocalDate.of(2026, 9, 9)))
                .thenReturn(new MorningMilkingStatus(SITE_ID, 8, 0));

        assertTrue(producer.evaluate(NOW).isEmpty());
    }

    @Test
    void evaluate_pendingCows_emitsOneDraftForTheFarm() throws Exception {
        when(milkingStatusService.statusFor(SITE_ID, LocalDate.of(2026, 9, 9)))
                .thenReturn(new MorningMilkingStatus(SITE_ID, 8, 3));

        List<NotificationDraft> drafts = producer.evaluate(NOW);

        assertEquals(1, drafts.size());
        NotificationDraft draft = drafts.get(0);
        JsonNode data = new ObjectMapper().readTree(draft.dataJson());
        assertAll(
                () -> assertEquals(FARM_ID, draft.farmId()),
                () -> assertEquals(NotificationType.MILKING_AM_REMINDER, draft.type()),
                () -> assertTrue(draft.body().contains("3 vacas")),
                () -> assertEquals("/lactancia?focus=registro", draft.deeplink()),
                () -> assertEquals("MILKING_AM_REMINDER#F001#2026-09-09", draft.dedupeKey()),
                () -> assertEquals(3, data.get("pendingCows").asInt()),
                () -> assertEquals(8, data.get("lactatingCows").asInt()),
                () -> assertEquals("2026-09-09", data.get("date").asText())
        );
    }

    @Test
    void evaluate_dedupeKeyChangesWithTheDate() {
        when(milkingStatusService.statusFor(SITE_ID, LocalDate.of(2026, 9, 10)))
                .thenReturn(new MorningMilkingStatus(SITE_ID, 5, 1));
        ZonedDateTime nextDay = NOW.plusDays(1);

        assertEquals("MILKING_AM_REMINDER#F001#2026-09-10", producer.evaluate(nextDay).get(0).dedupeKey());
    }
}
