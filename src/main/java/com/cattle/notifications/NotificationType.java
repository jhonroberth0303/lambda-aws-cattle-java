package com.cattle.notifications;

/**
 * Catálogo de tipos de notificación de la plataforma.
 * <p>
 * Cada valor tiene exactamente un {@link com.cattle.notifications.producer.NotificationProducer}
 * responsable de generarlo. Añadir un tipo nuevo (sanidad, potreros, ...) implica añadir un
 * productor, sin tocar la plataforma.
 */
public enum NotificationType {

    /**
     * Recordatorio de que el ordeño de la mañana sigue sin registrarse.
     * Retención corta: pasados unos días ya no aporta (el ordeño se hizo o el día pasó).
     */
    MILKING_AM_REMINDER(3),

    /**
     * Resumen diario de la agenda reproductiva: tareas para hoy y vencidas (HU-20260929, CA5).
     * Retención corta: el resumen de mañana lo reemplaza.
     */
    REPRODUCTIVE_TASKS_DIGEST(2);

    /** Días que la notificación permanece antes de que el TTL de DynamoDB la borre. */
    private final int retentionDays;

    NotificationType(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public int retentionDays() {
        return retentionDays;
    }
}
