package com.cattle.notifications.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Representación de una notificación para la campana del frontend.
 */
@Getter
@Builder
public class NotificationDTO {

    private String notificationId;
    private String type;
    private String title;
    private String body;
    private String deeplink;
    private String dataJson;
    private String status;
    private String createdAt;
    private String readAt;
}
