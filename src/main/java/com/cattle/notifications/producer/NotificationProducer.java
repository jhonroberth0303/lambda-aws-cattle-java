package com.cattle.notifications.producer;

import com.cattle.notifications.NotificationDraft;
import com.cattle.notifications.NotificationType;
import com.cattle.notifications.ScheduleWindow;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Regla de negocio que decide, para una ventana horaria, si hay que notificar algo.
 * <p>
 * Cada implementación es un {@code @Component} de Spring; el scheduler las descubre por
 * inyección y ejecuta solo las de la ventana que dispara el schedule. Añadir un tipo de
 * notificación (sanidad, potreros, ...) es implementar esta interfaz.
 */
public interface NotificationProducer {

    /** Tipo de notificación que produce. */
    NotificationType type();

    /** Ventana horaria en la que este productor debe evaluarse. */
    ScheduleWindow window();

    /**
     * Evalúa el estado actual y devuelve 0..N intenciones de notificación (una por finca).
     *
     * @param now instante actual en la zona horaria operativa
     * @return lista posiblemente vacía; nunca {@code null}
     */
    List<NotificationDraft> evaluate(ZonedDateTime now);
}
