package com.cattle.entities.bovines;

import com.cattle.enums.EventSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class BovineEventItemTest {

    @Test
    void gettersAndSetters_persistValues() {
        Instant eventAt = Instant.parse("2026-04-28T10:15:30Z");
        BovineEventItem item = new BovineEventItem();

        item.setPk("BOVINE#101");
        item.setSk("EVT#2026-04-28T10:15:30Z#COMPRA#1");
        item.setBovineId("101");
        item.setEventId("1");
        item.setEventType("COMPRA");
        item.setEventAt(eventAt);
        item.setSource(EventSource.MANUAL);
        item.setCreatedBy("user-1");
        item.setFarmId("farm-001");
        item.setPayload("{\"amount\":1000}");
        item.setNotes("Compra inicial");

        assertEquals("BOVINE#101", item.getPk());
        assertEquals("EVT#2026-04-28T10:15:30Z#COMPRA#1", item.getSk());
        assertEquals("101", item.getBovineId());
        assertEquals("1", item.getEventId());
        assertEquals("COMPRA", item.getEventType());
        assertEquals(eventAt, item.getEventAt());
        assertEquals(EventSource.MANUAL, item.getSource());
        assertEquals("user-1", item.getCreatedBy());
        assertEquals("farm-001", item.getFarmId());
        assertEquals("{\"amount\":1000}", item.getPayload());
        assertEquals("Compra inicial", item.getNotes());
    }
}