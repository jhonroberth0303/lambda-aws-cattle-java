package com.cattle.events.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class PastureIdentityItemTest {

    @Test
    void gettersAndSetters_persistValues() {
        PastureIdentityItem item = new PastureIdentityItem();

        item.setPk("PASTURE#1");
        item.setSk("IDENTITY");
        item.setPastureId("1");
        item.setName("Potrero 04");
        item.setAreaM2(1250.5);
        item.setGrassType("Kikuyo");
        item.setLocation("Lote Norte");

        assertEquals("PASTURE#1", item.getPk());
        assertEquals("IDENTITY", item.getSk());
        assertEquals("1", item.getPastureId());
        assertEquals("Potrero 04", item.getName());
        assertEquals(1250.5, item.getAreaM2());
        assertEquals("Kikuyo", item.getGrassType());
        assertEquals("Lote Norte", item.getLocation());
    }
}