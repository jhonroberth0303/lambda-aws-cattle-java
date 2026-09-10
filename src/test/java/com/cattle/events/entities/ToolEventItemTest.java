package com.cattle.events.entities;

import com.cattle.enums.EventSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
class ToolEventItemTest {

    @Test
    void gettersAndSetters_persistValues() {
        Instant eventAt = Instant.parse("2026-04-28T10:15:30Z");
        ToolEventItem item = new ToolEventItem();

        item.setPk("TOOL#T1");
        item.setSk("EVT#2026-04-28T10:15:30Z#MAINTENANCE#e1");
        item.setToolId("T1");
        item.setPurchaseId("PUR-9");
        item.setEventId("e1");
        item.setEventType("MAINTENANCE");
        item.setEventAt(eventAt);
        item.setSource(EventSource.AUTO);
        item.setCreatedBy("user-1");
        item.setFarmId("farm-001");
        item.setPayloadJson("{\"cost\":120}");
        item.setNotes("Cambio de cuchillas");

        assertEquals("TOOL#T1", item.getPk());
        assertEquals("EVT#2026-04-28T10:15:30Z#MAINTENANCE#e1", item.getSk());
        assertEquals("T1", item.getToolId());
        assertEquals("PUR-9", item.getPurchaseId());
        assertEquals("e1", item.getEventId());
        assertEquals("MAINTENANCE", item.getEventType());
        assertEquals(eventAt, item.getEventAt());
        assertEquals(EventSource.AUTO, item.getSource());
        assertEquals("user-1", item.getCreatedBy());
        assertEquals("farm-001", item.getFarmId());
        assertEquals("{\"cost\":120}", item.getPayloadJson());
        assertEquals("Cambio de cuchillas", item.getNotes());
    }

    @Test
    void dynamoDbBeanSchema_isResolvable() {
        assertNotNull(TableSchema.fromBean(ToolEventItem.class));
    }
}
