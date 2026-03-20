package com.cattle.events.entities;

import com.cattle.entities.bovines.BaseDdbItem;
import com.cattle.enums.EventSource;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.Instant;

/**
 * TABLE_TOOLS item: EVENT
 * PK = TOOL#<toolId>         (mantenimiento)
 * o PK = PURCHASE#<id>       (si decides meter compras aquí)
 * SK = EVT#<ISO_TS>#<EVENT_TYPE>#<eventId>
 *
 * eventType ejemplos: MAINTENANCE, FUEL, SUPPLY_PURCHASE...
 */
@DynamoDbBean
public class ToolEventItem extends BaseDdbItem {

    private String toolId;         // null si es un PURCHASE#... (opcional)
    private String purchaseId;     // opcional si eventType=SUPPLY_PURCHASE

    private String eventId;
    private String eventType;
    private Instant eventAt;

    private EventSource source;
    private String createdBy;
    private String farmId;

    private String payloadJson;
    private String notes;

    @DynamoDbAttribute("toolId")
    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }

    @DynamoDbAttribute("purchaseId")
    public String getPurchaseId() { return purchaseId; }
    public void setPurchaseId(String purchaseId) { this.purchaseId = purchaseId; }

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

    @DynamoDbAttribute("payloadJson")
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    @DynamoDbAttribute("notes")
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

