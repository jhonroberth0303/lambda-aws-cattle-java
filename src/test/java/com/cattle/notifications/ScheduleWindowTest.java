package com.cattle.notifications;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@Tag("fast")
class ScheduleWindowTest {

    @Test
    void parse_knownValue_isCaseAndSpaceInsensitive() {
        assertEquals(Optional.of(ScheduleWindow.MORNING), ScheduleWindow.parse("morning"));
        assertEquals(Optional.of(ScheduleWindow.MORNING), ScheduleWindow.parse("  MoRnInG  "));
    }

    @Test
    void parse_nullOrBlank_isEmpty() {
        assertTrue(ScheduleWindow.parse(null).isEmpty());
        assertTrue(ScheduleWindow.parse("   ").isEmpty());
    }

    @Test
    void parse_unknownValue_isEmpty() {
        assertTrue(ScheduleWindow.parse("NIGHT").isEmpty());
    }
}
