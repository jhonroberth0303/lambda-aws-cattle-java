package com.cattle.events.payloads;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class EventPayloadConverterTest {

    private final EventPayloadConverter converter = new EventPayloadConverter();

    @Test
    void transformFrom_serializesPayloadAsStringAttribute() {
        AttributeValue result = converter.transformFrom(new SimplePayload("manual"));

        assertTrue(result.s() != null);
        assertTrue(result.s().contains("manual"));
    }

    @Test
    void transformTo_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
                () -> converter.transformTo(AttributeValue.fromS("{}")));
    }

    @Test
    void type_andAttributeValueType_returnExpectedMetadata() {
        assertEquals(EventPayload.class, converter.type().rawClass());
        assertEquals(AttributeValueType.S, converter.attributeValueType());
    }

    @Test
    void transformFrom_unserializablePayload_throwsRuntimeException() {
        assertThrows(RuntimeException.class,
                () -> converter.transformFrom(new RecursivePayload()));
    }

    static final class SimplePayload implements EventPayload {
        private final String source;

        SimplePayload(String source) {
            this.source = source;
        }

        public String getSource() {
            return source;
        }
    }

    static final class RecursivePayload implements EventPayload {
        public RecursivePayload getSelf() {
            return this;
        }
    }
}