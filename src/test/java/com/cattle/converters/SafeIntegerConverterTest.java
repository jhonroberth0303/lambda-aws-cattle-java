package com.cattle.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios para SafeIntegerConverter.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Converters
 */
@Tag("unit")
@Tag("fast")
class SafeIntegerConverterTest {

    private SafeIntegerConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SafeIntegerConverter();
    }

    // ==================== transformFrom ====================

    @Test
    void transformFrom_null_returnsNullAttribute() {
        AttributeValue result = converter.transformFrom(null);

        assertTrue(result.nul());
        assertNull(result.n());
    }

    @Test
    void transformFrom_positiveInteger_returnsNumericAttribute() {
        AttributeValue result = converter.transformFrom(42);

        assertEquals("42", result.n());
        assertNull(result.nul());
    }

    @Test
    void transformFrom_negativeInteger_returnsNumericAttribute() {
        assertEquals("-7", converter.transformFrom(-7).n());
    }

    @Test
    void transformFrom_zero_returnsZeroString() {
        assertEquals("0", converter.transformFrom(0).n());
    }

    // ==================== transformTo ====================

    @Test
    void transformTo_null_returnsNull() {
        assertNull(converter.transformTo(null));
    }

    @Test
    void transformTo_numericAttribute_parsesValue() {
        AttributeValue value = AttributeValue.builder().n("123").build();

        assertEquals(123, converter.transformTo(value));
    }

    @Test
    void transformTo_stringAttribute_parsesValue() {
        AttributeValue value = AttributeValue.builder().s("77").build();

        assertEquals(77, converter.transformTo(value));
    }

    @Test
    void transformTo_numericBeatsStringWhenBothPresent() {
        AttributeValue value = AttributeValue.builder().n("5").s("999").build();

        assertEquals(5, converter.transformTo(value));
    }

    @Test
    void transformTo_nonNumericNumberString_returnsNull() {
        AttributeValue value = AttributeValue.builder().n("abc").build();

        assertNull(converter.transformTo(value));
    }

    @Test
    void transformTo_nonNumericString_returnsNull() {
        AttributeValue value = AttributeValue.builder().s("not-a-number").build();

        assertNull(converter.transformTo(value));
    }

    @Test
    void transformTo_attributeWithoutNumberOrString_returnsNull() {
        AttributeValue value = AttributeValue.builder().bool(true).build();

        assertNull(converter.transformTo(value));
    }

    @Test
    void transformTo_nullAttributeValue_returnsNull() {
        AttributeValue value = AttributeValue.builder().nul(true).build();

        assertNull(converter.transformTo(value));
    }

    // ==================== metadata ====================

    @Test
    void type_isInteger() {
        assertEquals(EnhancedType.of(Integer.class), converter.type());
    }

    @Test
    void attributeValueType_isNumeric() {
        assertEquals(AttributeValueType.N, converter.attributeValueType());
    }
}
