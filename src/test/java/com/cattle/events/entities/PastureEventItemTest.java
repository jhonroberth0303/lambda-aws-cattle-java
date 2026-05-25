package com.cattle.events.entities;

import com.cattle.enums.EventSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("unit")
class PastureEventItemTest {

    @Test
    void gettersAndSetters_persistValues() {
        Instant eventAt = Instant.parse("2026-04-28T11:00:00Z");
        PastureEventItem item = new PastureEventItem();

        item.setPk("PASTURE#1");
        item.setSk("EVT#2026-04-28T11:00:00Z#LIMED#10");
        item.setPastureId("1");
        item.setEventId("10");
        item.setEventType("LIMED");
        item.setEventAt(eventAt);
        item.setSource(EventSource.AUTO);
        item.setCreatedBy("system");
        item.setFarmId("farm-001");
        item.setPayloadJson("{\"appliedKg\":100}");
        item.setNotes("Aplicacion de cal");

        assertEquals("PASTURE#1", item.getPk());
        assertEquals("EVT#2026-04-28T11:00:00Z#LIMED#10", item.getSk());
        assertEquals("1", item.getPastureId());
        assertEquals("10", item.getEventId());
        assertEquals("LIMED", item.getEventType());
        assertEquals(eventAt, item.getEventAt());
        assertEquals(EventSource.AUTO, item.getSource());
        assertEquals("system", item.getCreatedBy());
        assertEquals("farm-001", item.getFarmId());
        assertEquals("{\"appliedKg\":100}", item.getPayloadJson());
        assertEquals("Aplicacion de cal", item.getNotes());
    }

    @Test
    void tableSchema_serializesEventItemToDynamoAttributes() {
        Instant eventAt = Instant.parse("2026-04-28T11:00:00Z");
        PastureEventItem item = new PastureEventItem();

        item.setPk("PASTURE#1");
        item.setSk("EVT#2026-04-28T11:00:00Z#LIMED#10");
        item.setPastureId("1");
        item.setFarmId("farm-001");
        item.setEventId("10");
        item.setEventType("LIMED");
        item.setEventAt(eventAt);
        item.setSource(EventSource.MANUAL);
        item.setCreatedBy("manual-web");
        item.setPayloadJson("{\"appliedKg\":100}");
        item.setNotes("Aplicacion de cal");
        item.setCreatedAt(eventAt.toString());
        item.setUpdatedAt(eventAt.toString());
        item.setGsi1pk("farm#farm-001#type#LIMED");
        item.setGsi1sk(eventAt.toString());

        Map<String, AttributeValue> attributes = assertDoesNotThrow(
                () -> TableSchema.fromBean(PastureEventItem.class).itemToMap(item, true)
        );

        assertEquals("PASTURE#1", attributes.get("pk").s());
        assertEquals("EVT#2026-04-28T11:00:00Z#LIMED#10", attributes.get("sk").s());
        assertEquals("1", attributes.get("pastureId").s());
        assertEquals("farm-001", attributes.get("farmId").s());
        assertEquals("10", attributes.get("eventId").s());
        assertEquals("LIMED", attributes.get("eventType").s());
        assertEquals("2026-04-28T11:00:00Z", attributes.get("eventAt").s());
        assertEquals("MANUAL", attributes.get("source").s());
        assertEquals("manual-web", attributes.get("createdBy").s());
        assertEquals("{\"appliedKg\":100}", attributes.get("payloadJson").s());
        assertEquals("Aplicacion de cal", attributes.get("notes").s());
        assertEquals("farm#farm-001#type#LIMED", attributes.get("gsi1pk").s());
        assertEquals("2026-04-28T11:00:00Z", attributes.get("gsi1sk").s());
        assertFalse(attributes.containsKey("PK"));
        assertFalse(attributes.containsKey("SK"));
    }
}