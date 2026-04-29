package com.cattle.events;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class BovineEventTypeTest {

    @Test
    void values_containsExpectedTypes() {
        assertArrayEquals(new BovineEventType[]{
                BovineEventType.VACUNACION,
                BovineEventType.DESPARASITACION,
                BovineEventType.BANO_GARRAPATICIDA,
                BovineEventType.SERVICIO,
                BovineEventType.PARTO,
                BovineEventType.DESTETE,
                BovineEventType.PESAJE,
                BovineEventType.OBSERVACION,
                BovineEventType.SEGUIMIENTO,
                BovineEventType.COMPRA,
                BovineEventType.VENTA
        }, BovineEventType.values());
    }

    @Test
    void valueOf_resolvesKnownType() {
        assertEquals(BovineEventType.COMPRA, BovineEventType.valueOf("COMPRA"));
    }
}