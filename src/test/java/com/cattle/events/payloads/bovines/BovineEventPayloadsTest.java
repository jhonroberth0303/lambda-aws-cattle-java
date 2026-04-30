package com.cattle.events.payloads.bovines;

import com.cattle.events.payloads.EventPayload;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
class BovineEventPayloadsTest {

    @Test
    void payloads_canBeInstantiated() {
        BredPayload bredPayload = new BredPayload();
        DewormedPayload dewormedPayload = new DewormedPayload();
        PurchasedBovinePayload purchasedPayload = new PurchasedBovinePayload();

        assertNotNull(bredPayload);
        assertNotNull(dewormedPayload);
        assertNotNull(purchasedPayload);
        assertInstanceOf(EventPayload.class, bredPayload);
        assertInstanceOf(EventPayload.class, dewormedPayload);
        assertInstanceOf(EventPayload.class, purchasedPayload);
    }
}