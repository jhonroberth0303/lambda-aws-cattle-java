package com.cattle.services;

/**
 * Literales de estado de los perfiles de preñez y lactancia (HU-20260930, D1). Son los mismos
 * de los datos históricos y del contrato de {@code BovineSummaryDTO}; no se migran datos.
 */
public final class ReproductiveProfileStatus {

    public static final String PREGNANCY_ACTIVE = "ACTIVE";
    /** Preñez cerrada: histórico y {@code ExitEventProjector}. */
    public static final String PREGNANCY_CLOSED = "CLOSE";

    public static final String LACTATION_LACTATING = "LACTATING";
    public static final String LACTATION_DRY = "DRY";
    /** Lactancia cerrada: histórico (p. ej. LACT#001 de la 167) y {@code BovineSummaryDTO}. */
    public static final String LACTATION_CLOSED = "CLOSED";

    private ReproductiveProfileStatus() {
    }

    /** Cerrado en cualquiera de los dos literales que conviven en los datos ({@code CLOSE}/{@code CLOSED}). */
    public static boolean isClosed(String status) {
        return "CLOSE".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status);
    }
}
