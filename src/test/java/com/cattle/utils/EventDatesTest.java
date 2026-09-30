package com.cattle.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("unit")
@Tag("fast")
@DisplayName("EventDates Tests")
class EventDatesTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    @Test
    @DisplayName("Medianoche UTC (fecha elegida en el formulario) conserva el día calendario")
    void explicitDate_isReadInUtc() {
        assertEquals(LocalDate.of(2026, 10, 1),
                EventDates.toOperationalDate(Instant.parse("2026-10-01T00:00:00Z"), BOGOTA));
    }

    @Test
    @DisplayName("Instante de registro real se lee en la zona operativa")
    void registrationInstant_isReadInOperationalZone() {
        // 02:00 UTC = 21:00 del día anterior en Bogotá
        assertEquals(LocalDate.of(2026, 10, 1),
                EventDates.toOperationalDate(Instant.parse("2026-10-02T02:00:00Z"), BOGOTA));
    }

    @Test
    @DisplayName("Null devuelve null")
    void nullInstant_returnsNull() {
        assertNull(EventDates.toOperationalDate(null, BOGOTA));
    }
}
