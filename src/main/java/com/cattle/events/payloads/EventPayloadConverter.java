package com.cattle.events.payloads;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class EventPayloadConverter
        implements AttributeConverter<EventPayload> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(EventPayload input) {
        try {
            return AttributeValue.fromS(
                    MAPPER.writeValueAsString(input)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EventPayload transformTo(AttributeValue value) {
        throw new UnsupportedOperationException(
                "Deserialization handled at service level"
        );
    }

    @Override
    public EnhancedType<EventPayload> type() {
        return EnhancedType.of(EventPayload.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}

