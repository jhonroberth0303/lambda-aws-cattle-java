package com.cattle.events;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class EventOriginTest {

    @Test
    void values_containsExpectedOrigins() {
        assertArrayEquals(
                new EventOrigin[]{EventOrigin.MANUAL, EventOrigin.SISTEMA, EventOrigin.SENSOR},
                EventOrigin.values()
        );
    }

    @Test
    void valueOf_resolvesKnownOrigin() {
        assertEquals(EventOrigin.SISTEMA, EventOrigin.valueOf("SISTEMA"));
    }
}