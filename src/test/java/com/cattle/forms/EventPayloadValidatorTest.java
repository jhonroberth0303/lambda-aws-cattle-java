package com.cattle.forms;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
@Tag("fast")
class EventPayloadValidatorTest {

    private final EventPayloadValidator validator = new EventPayloadValidator();

    private EventFormSchema.Field field(String name, EventFormSchema.Field.Type type, boolean required,
                                        Double min, Double max, List<EventFormSchema.Option> options) {
        return new EventFormSchema.Field(name, name, type, required, min, max, null, null, null, null, null, false, options);
    }

    private EventFormSchema schema(List<String> requireOneOf, EventFormSchema.Field... fields) {
        return new EventFormSchema("TEST", "t", "s", true, requireOneOf, List.of(fields));
    }

    @Test
    void requiredTextMissing_throwsWithFieldName() {
        EventFormSchema s = schema(null, field("cause", EventFormSchema.Field.Type.TEXT, true, null, null, null));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(s, Map.of()));

        assertEquals("El campo cause es requerido para este tipo de evento", ex.getMessage());
    }

    @Test
    void requiredTextBlank_throws() {
        EventFormSchema s = schema(null, field("cause", EventFormSchema.Field.Type.TEXT, true, null, null, null));
        Map<String, Object> payload = new HashMap<>();
        payload.put("cause", "   ");

        assertThrows(IllegalArgumentException.class, () -> validator.validate(s, payload));
    }

    @Test
    void requiredNumberZero_isAccepted() {
        EventFormSchema s = schema(null, field("weightKg", EventFormSchema.Field.Type.NUMBER, true, 0.0, null, null));

        assertDoesNotThrow(() -> validator.validate(s, Map.of("weightKg", 0)));
    }

    @Test
    void numberBelowMin_throws() {
        EventFormSchema s = schema(null, field("weightKg", EventFormSchema.Field.Type.NUMBER, true, 0.0, null, null));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(s, Map.of("weightKg", -1)));

        assertEquals("El campo weightKg debe ser mayor o igual a 0", ex.getMessage());
    }

    @Test
    void nonNumericNumber_throws() {
        EventFormSchema s = schema(null, field("weightKg", EventFormSchema.Field.Type.NUMBER, true, null, null, null));

        assertThrows(IllegalArgumentException.class, () -> validator.validate(s, Map.of("weightKg", "abc")));
    }

    @Test
    void valueOutsideOptions_throws() {
        EventFormSchema s = schema(null, field("conditionScore", EventFormSchema.Field.Type.NUMBER, false, null, null,
                List.of(new EventFormSchema.Option("1", "uno"), new EventFormSchema.Option("2", "dos"))));

        assertThrows(IllegalArgumentException.class, () -> validator.validate(s, Map.of("conditionScore", "9")));
        assertDoesNotThrow(() -> validator.validate(s, Map.of("conditionScore", "2")));
    }

    @Test
    void requireOneOf_nonePresent_throwsWithJoinedNames() {
        EventFormSchema s = schema(List.of("bullId", "semenBatch"),
                field("bullId", EventFormSchema.Field.Type.TEXT, false, null, null, null),
                field("semenBatch", EventFormSchema.Field.Type.TEXT, false, null, null, null));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(s, Map.of()));

        assertEquals("Se requiere bullId o semenBatch para el evento TEST", ex.getMessage());
    }

    @Test
    void requireOneOf_onePresent_isAccepted() {
        EventFormSchema s = schema(List.of("bullId", "semenBatch"),
                field("bullId", EventFormSchema.Field.Type.TEXT, false, null, null, null),
                field("semenBatch", EventFormSchema.Field.Type.TEXT, false, null, null, null));

        assertDoesNotThrow(() -> validator.validate(s, Map.of("semenBatch", "SB-22")));
    }

    @Test
    void nullPayload_withoutRequiredFields_isAccepted() {
        EventFormSchema s = schema(null, field("opt", EventFormSchema.Field.Type.TEXT, false, null, null, null));

        assertDoesNotThrow(() -> validator.validate(s, null));
    }
}
