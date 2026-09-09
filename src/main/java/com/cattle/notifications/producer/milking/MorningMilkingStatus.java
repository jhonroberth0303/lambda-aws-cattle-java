package com.cattle.notifications.producer.milking;

/**
 * Foto del ordeño de la mañana de una finca en una fecha.
 *
 * @param siteId         finca / sitio evaluado
 * @param lactatingCows  vacas operativas con lactancia activa
 * @param pendingCows    de esas, cuántas aún no tienen registro de ordeño AM
 */
public record MorningMilkingStatus(String siteId, int lactatingCows, int pendingCows) {

    public boolean hasPending() {
        return pendingCows > 0;
    }
}
