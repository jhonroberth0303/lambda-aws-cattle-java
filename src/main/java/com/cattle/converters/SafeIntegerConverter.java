package com.cattle.converters;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class SafeIntegerConverter implements AttributeConverter<Integer> {

    @Override
    public AttributeValue transformFrom(Integer input) {
        if (input == null) return AttributeValue.builder().nul(true).build();
        return AttributeValue.builder().n(String.valueOf(input)).build();
    }

    @Override
    public Integer transformTo(AttributeValue value) {
        if (value == null) return null;
        try {
            if (value.n() != null) return Integer.parseInt(value.n());
            if (value.s() != null) return Integer.parseInt(value.s());
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    @Override
    public EnhancedType<Integer> type() {
        return EnhancedType.of(Integer.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.N;
    }
}
