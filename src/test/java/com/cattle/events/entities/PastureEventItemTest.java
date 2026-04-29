package com.cattle.events.entities;

import com.cattle.enums.EventSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}