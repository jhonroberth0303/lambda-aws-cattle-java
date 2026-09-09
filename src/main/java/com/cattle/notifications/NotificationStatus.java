package com.cattle.notifications;

/**
 * Estado de lectura de una notificación para una finca.
 */
public enum NotificationStatus {

    /** Generada y aún no vista por ningún operario de la finca. */
    UNREAD,

    /** Marcada como leída desde la campana. */
    READ
}
