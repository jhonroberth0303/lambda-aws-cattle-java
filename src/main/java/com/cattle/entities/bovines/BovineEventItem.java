package com.cattle.entities.bovines;

import com.cattle.entities.BaseDdbItem;
import com.cattle.enums.EventSource;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.Instant;

/**
 * TABLE_BOVINES item: EVENT
 * PK = BOVINE#<bovineId>
 * SK = EVT#<ISO_TS>#<EVENT_TYPE>#<eventId>
 *
 * payloadJson: aquí guardas el "payload" del evento como JSON string (compra, desparasitación, baño, etc).
 */
@DynamoDbBean
public class BovineEventItem extends BaseDdbItem {

    private String bovineId;

    private String eventId;
    private String eventType;      // PURCHASED, DEWORMED, TICK_BATH, BRED, FOLLOWUP...
    private Instant eventAt;

    private EventSource source;    // MANUAL | AUTO
    private String createdBy;      // userId o "SYSTEM"
    private String farmId;

    private String payload;    // JSON string con campos variables por tipo de evento
    private String notes;

    @DynamoDbAttribute("bovineId")
    public String getBovineId() { return bovineId; }
    public void setBovineId(String bovineId) { this.bovineId = bovineId; }

    @DynamoDbAttribute("eventId")
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    @DynamoDbAttribute("eventType")
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    @DynamoDbAttribute("eventAt")
    public Instant getEventAt() { return eventAt; }
    public void setEventAt(Instant eventAt) { this.eventAt = eventAt; }

    @DynamoDbAttribute("source")
    public EventSource getSource() { return source; }
    public void setSource(EventSource source) { this.source = source; }

    @DynamoDbAttribute("createdBy")
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @DynamoDbAttribute("farmId")
    public String getFarmId() { return farmId; }
    public void setFarmId(String farmId) { this.farmId = farmId; }

    @DynamoDbAttribute("payload")
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    @DynamoDbAttribute("notes")
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

