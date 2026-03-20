package com.cattle.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class Dates {

    static LocalDate parseDate(String iso) {
        if (iso == null || iso.isBlank()) return null;
        if (iso.length() == 10) return LocalDate.parse(iso);
        return null;
    }
    static Instant parseInstant(String iso) {
        if (iso == null || iso.isBlank()) return null;
        if (iso.length() == 10) {
            LocalDate d = LocalDate.parse(iso);
            return d.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
        return null;
    }

    static long daysBetween(LocalDate from, LocalDate to) {
        return Duration.between(from.atStartOfDay(), to.atStartOfDay()).toDays();
    }

    static String isoDateTimeUTC(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
