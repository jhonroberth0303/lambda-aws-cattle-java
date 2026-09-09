package com.cattle.notifications;

import lombok.Builder;

import java.util.Objects;

/**
 * Intención de notificación que emite un productor, antes de persistirse.
 * <p>
 * Es inmutable y no conoce identidad, fecha de creación ni estado: eso lo añade el
 * {@link com.cattle.notifications.service.NotificationDispatcher} al materializarla.
 *
 * @param farmId    finca destinataria
 * @param type      tipo de notificación
 * @param title     título corto para la campana y el push
 * @param body      cuerpo del mensaje
 * @param deeplink  ruta relativa del frontend a abrir al tocar la notificación
 * @param dataJson  payload libre por tipo, serializado como JSON (nunca {@code null}; usar {@code "{}"})
 * @param dedupeKey clave estable que impide duplicar la notificación el mismo día
 */
@Builder
public record NotificationDraft(
        String farmId,
        NotificationType type,
        String title,
        String body,
        String deeplink,
        String dataJson,
        String dedupeKey
) {

    public NotificationDraft {
        farmId = requireText(farmId, "farmId");
        type = Objects.requireNonNull(type, "type es obligatorio");
        title = requireText(title, "title");
        body = requireText(body, "body");
        deeplink = requireText(deeplink, "deeplink");
        dedupeKey = requireText(dedupeKey, "dedupeKey");
        dataJson = (dataJson == null || dataJson.isBlank()) ? "{}" : dataJson;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " es obligatorio");
        }
        return value;
    }
}
