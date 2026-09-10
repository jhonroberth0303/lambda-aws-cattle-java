package com.cattle.notifications.producer.milking;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.CowWithLactationsDTO;
import com.cattle.entities.MilkingRecord;
import com.cattle.processor.MilkingProcessor;
import com.cattle.services.MilkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("fast")
class MorningMilkingStatusServiceTest {

    private static final String SITE_ID = "001";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 9);

    @Mock
    private MilkingProcessor milkingProcessor;

    @Mock
    private MilkingService milkingService;

    @Mock
    private LambdaContext lambdaContext;

    private MorningMilkingStatusService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new MorningMilkingStatusService(milkingProcessor, milkingService, lambdaContext);
    }

    @Test
    void statusFor_noOperationalCows_reportsZero() {
        when(milkingProcessor.getCowsWithLactations(SITE_ID)).thenReturn(Optional.empty());

        MorningMilkingStatus status = service.statusFor(SITE_ID, TODAY);

        assertEquals(0, status.lactatingCows());
        assertEquals(0, status.pendingCows());
        assertFalse(status.hasPending());
    }

    @Test
    void statusFor_cowWithMorningRecordToday_notPending() {
        givenCows(cow(10));
        when(milkingService.getMilkingByPk("BOVINE#10"))
                .thenReturn(Optional.of(List.of(record("2026-09-09", "AM"))));

        MorningMilkingStatus status = service.statusFor(SITE_ID, TODAY);

        assertEquals(1, status.lactatingCows());
        assertEquals(0, status.pendingCows());
    }

    @Test
    void statusFor_cowWithOnlyAfternoonRecord_isPending() {
        givenCows(cow(11));
        when(milkingService.getMilkingByPk("BOVINE#11"))
                .thenReturn(Optional.of(List.of(record("2026-09-09", "PM"))));

        assertEquals(1, service.statusFor(SITE_ID, TODAY).pendingCows());
    }

    @Test
    void statusFor_cowWithMorningRecordOnAnotherDay_isPending() {
        givenCows(cow(12));
        when(milkingService.getMilkingByPk("BOVINE#12"))
                .thenReturn(Optional.of(List.of(record("2026-09-08", "AM"))));

        assertEquals(1, service.statusFor(SITE_ID, TODAY).pendingCows());
    }

    @Test
    void statusFor_shiftMatchIsCaseInsensitiveAndTrimmed() {
        givenCows(cow(13));
        when(milkingService.getMilkingByPk("BOVINE#13"))
                .thenReturn(Optional.of(List.of(record("2026-09-09", " am "))));

        assertEquals(0, service.statusFor(SITE_ID, TODAY).pendingCows());
    }

    @Test
    void statusFor_mixedCows_countsOnlyThePending() {
        givenCows(cow(20), cow(21), cow(22));
        when(milkingService.getMilkingByPk("BOVINE#20"))
                .thenReturn(Optional.of(List.of(record("2026-09-09", "AM"))));
        when(milkingService.getMilkingByPk("BOVINE#21")).thenReturn(Optional.of(List.of()));
        when(milkingService.getMilkingByPk("BOVINE#22")).thenReturn(Optional.empty());

        MorningMilkingStatus status = service.statusFor(SITE_ID, TODAY);

        assertEquals(3, status.lactatingCows());
        assertEquals(2, status.pendingCows());
        assertTrue(status.hasPending());
    }

    private void givenCows(CowWithLactationsDTO... cows) {
        when(milkingProcessor.getCowsWithLactations(SITE_ID)).thenReturn(Optional.of(List.of(cows)));
        lenient().when(milkingService.getMilkingByPk(anyString())).thenReturn(Optional.of(List.of()));
    }

    private static CowWithLactationsDTO cow(int bovineId) {
        return CowWithLactationsDTO.builder().bovineId(bovineId).build();
    }

    private static MilkingRecord record(String date, String shift) {
        return MilkingRecord.builder().date(date).shift(shift).build();
    }
}
