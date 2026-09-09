package com.cattle.notifications.entity;

import com.cattle.notifications.NotificationStatus;
import com.cattle.notifications.NotificationType;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Item de la tabla {@code Notifications}: una notificación materializada para una finca.
 * <pre>
 * PK = FARM#&lt;farmId&gt;
 * SK = NOTIF#&lt;notificationId&gt;     (notificationId lleva prefijo de epoch millis: ordena por SK = más reciente primero)
 * </pre>
 */
@DynamoDbBean
public class NotificationItem {

    public static final String PK_PREFIX = "FARM#";
    public static final String SK_PREFIX = "NOTIF#";

    private String pk;
    private String sk;

    /** Clave de partición para todas las notificaciones de una finca. */
    public static String partitionKey(String farmId) {
        return PK_PREFIX + farmId;
    }

    /** Clave de ordenación de una notificación concreta. */
    public static String sortKey(String notificationId) {
        return SK_PREFIX + notificationId;
    }

    private String notificationId;
    private String farmId;
    private NotificationType type;
    private String title;
    private String body;
    private String deeplink;
    private String dataJson;
    private NotificationStatus status;
    private String dedupeKey;
    private String createdAt;
    private String readAt;
    private Long ttl;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }

    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }

    @DynamoDbAttribute("notificationId")
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    @DynamoDbAttribute("farmId")
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }

    @DynamoDbAttribute("type")
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    @DynamoDbAttribute("title")
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @DynamoDbAttribute("body")
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    @DynamoDbAttribute("deeplink")
    public String getDeeplink() { return deeplink; }
    public void setDeeplink(String deeplink) { this.deeplink = deeplink; }

    @DynamoDbAttribute("dataJson")
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }

    @DynamoDbAttribute("status")
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    @DynamoDbAttribute("dedupeKey")
    public String getDedupeKey() { return dedupeKey; }
    public void setDedupeKey(String dedupeKey) { this.dedupeKey = dedupeKey; }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbAttribute("readAt")
    public String getReadAt() { return readAt; }
    public void setReadAt(String readAt) { this.readAt = readAt; }

    @DynamoDbAttribute("ttl")
    public Long getTtl() { return ttl; }
    public void setTtl(Long ttl) { this.ttl = ttl; }
}
