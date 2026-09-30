package com.cattle.tasks.planner;

import java.util.Map;

/**
 * Parámetros de días por finca ({@code site-settings.yml}, grupo reproduccion).
 */
public record ReproductiveTaskSettings(int gestationDays, int pregnancyCheckDays, int pregnancyRecheckDays,
                                       int dryOffBeforeCalvingDays, int prepartumBeforeCalvingDays,
                                       int postpartumCheckDays, int voluntaryWaitingDays,
                                       int followUpOrangeDays, int followUpRedDays, int repeatBreederServices) {

    /** Mismos valores por defecto que {@code site-settings.yml}. */
    public static ReproductiveTaskSettings defaults() {
        return new ReproductiveTaskSettings(279, 40, 15, 60, 21, 21, 60, 42, 63, 3);
    }

    /** Construye desde los valores vigentes del sitio; una clave ausente conserva el default. */
    public static ReproductiveTaskSettings from(Map<String, Object> values) {
        ReproductiveTaskSettings d = defaults();
        return new ReproductiveTaskSettings(
                intValue(values, "GESTATION_DAYS", d.gestationDays()),
                intValue(values, "PREGNANCY_CHECK_DAYS", d.pregnancyCheckDays()),
                intValue(values, "PREGNANCY_RECHECK_DAYS", d.pregnancyRecheckDays()),
                intValue(values, "DRY_OFF_BEFORE_CALVING_DAYS", d.dryOffBeforeCalvingDays()),
                intValue(values, "PREPARTUM_BEFORE_CALVING_DAYS", d.prepartumBeforeCalvingDays()),
                intValue(values, "POSTPARTUM_CHECK_DAYS", d.postpartumCheckDays()),
                intValue(values, "VOLUNTARY_WAITING_DAYS", d.voluntaryWaitingDays()),
                intValue(values, "SERVICE_FOLLOWUP_ORANGE_DAYS", d.followUpOrangeDays()),
                intValue(values, "SERVICE_FOLLOWUP_RED_DAYS", d.followUpRedDays()),
                intValue(values, "REPEAT_BREEDER_SERVICES", d.repeatBreederServices()));
    }

    private static int intValue(Map<String, Object> values, String key, int fallback) {
        Object value = values == null ? null : values.get(key);
        if (value instanceof Number number) {
            return (int) Math.round(number.doubleValue());
        }
        if (value != null) {
            try {
                return (int) Math.round(Double.parseDouble(value.toString()));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}
