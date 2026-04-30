package com.cattle.events.payloads;

import com.cattle.events.payloads.bovines.BredPayload;
import com.cattle.events.payloads.bovines.DewormedPayload;
import com.cattle.events.payloads.bovines.PurchasedBovinePayload;
import com.cattle.events.payloads.pastures.FertilizedPayload;
import com.cattle.events.payloads.pastures.LimedPayload;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class EventPayloadMapperTest {

    @Test
    void toJson_serializesPayload() {
        String result = EventPayloadMapper.toJson(new SerializablePayload("sensor"));

        assertTrue(result.contains("sensor"));
    }

    @Test
    void fromJson_knownBovineTypes_returnsExpectedPayloadClass() {
        assertInstanceOf(DewormedPayload.class, EventPayloadMapper.fromJson("DEWORMED", "{}"));
        assertInstanceOf(BredPayload.class, EventPayloadMapper.fromJson("BRED", "{}"));
        assertInstanceOf(PurchasedBovinePayload.class, EventPayloadMapper.fromJson("PURCHASED", "{}"));
    }

    @Test
    void fromJson_knownPastureTypes_returnsExpectedPayloadClass() {
        assertInstanceOf(LimedPayload.class, EventPayloadMapper.fromJson("LIMED", "{}"));
        assertInstanceOf(FertilizedPayload.class, EventPayloadMapper.fromJson("FERTILIZED", "{}"));
    }

    @Test
    void fromJson_unknownType_returnsNull() {
        assertNull(EventPayloadMapper.fromJson("UNKNOWN", "{}"));
    }

    @Test
    void fromJson_invalidJson_throwsRuntimeException() {
        assertThrows(RuntimeException.class,
                () -> EventPayloadMapper.fromJson("DEWORMED", "{bad-json"));
    }

    static final class SerializablePayload implements EventPayload {
        private final String origin;

        SerializablePayload(String origin) {
            this.origin = origin;
        }

        public String getOrigin() {
            return origin;
        }
    }
}