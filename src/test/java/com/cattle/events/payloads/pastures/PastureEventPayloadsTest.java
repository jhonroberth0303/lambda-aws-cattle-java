package com.cattle.events.payloads.pastures;

import com.cattle.events.payloads.EventPayload;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("unit")
class PastureEventPayloadsTest {

    @Test
    void payloads_canBeInstantiated() {
        FertilizedPayload fertilizedPayload = new FertilizedPayload();
        LimedPayload limedPayload = new LimedPayload();

        assertNotNull(fertilizedPayload);
        assertNotNull(limedPayload);
        assertInstanceOf(EventPayload.class, fertilizedPayload);
        assertInstanceOf(EventPayload.class, limedPayload);
    }
}