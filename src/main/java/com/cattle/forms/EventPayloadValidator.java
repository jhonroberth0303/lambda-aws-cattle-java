package com.cattle.forms;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Valida el payload de un evento contra su {@link EventFormSchema} (EP-20260909,
 * Fase 2). Sustituye a la validación hardcodeada por tipo en los processors para
 * los eventos ya migrados a {@code event-forms.yml}.
 *
 * <p>Reglas: campos {@code required}, coherencia de tipo numérico, {@code min}/
 * {@code max}, pertenencia a {@code options} y {@code requireOneOf}. Los mensajes
 * de error se mantienen alineados con los que emitían los processors.
 */
@Component
public class EventPayloadValidator {

    /**
     * @throws IllegalArgumentException si el payload no cumple el esquema
     */
    public void validate(EventFormSchema schema, Map<String, Object> payload) {
        for (EventFormSchema.Field field : schema.getFields()) {
            validateField(field, value(payload, field.getName()));
        }
        validateRequireOneOf(schema, payload);
    }

    private void validateField(EventFormSchema.Field field, Object value) {
        boolean present = isPresent(value);

        if (field.isRequired() && !present) {
            throw new IllegalArgumentException(
                    "El campo " + field.getName() + " es requerido para este tipo de evento");
        }
        if (!present) {
            return;
        }

        Double number = null;
        if (field.getType() == EventFormSchema.Field.Type.NUMBER) {
            number = asNumber(value);
            if (number == null) {
                throw new IllegalArgumentException("El campo " + field.getName() + " debe ser numérico");
            }
            if (field.getMin() != null && number < field.getMin()) {
                throw new IllegalArgumentException(
                        "El campo " + field.getName() + " debe ser mayor o igual a " + trim(field.getMin()));
            }
            if (field.getMax() != null && number > field.getMax()) {
                throw new IllegalArgumentException(
                        "El campo " + field.getName() + " debe ser menor o igual a " + trim(field.getMax()));
            }
        }

        if (!field.getOptions().isEmpty() && !matchesOption(field, value, number)) {
            throw new IllegalArgumentException(
                    "El campo " + field.getName() + " no admite el valor: " + value);
        }
    }

    private boolean matchesOption(EventFormSchema.Field field, Object value, Double asNumber) {
        String asText = String.valueOf(value).trim();
        for (EventFormSchema.Option option : field.getOptions()) {
            if (option.getValue().equals(asText)) {
                return true;
            }
            if (asNumber != null) {
                try {
                    if (Double.compare(Double.parseDouble(option.getValue()), asNumber) == 0) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    // opción no numérica: solo cuenta la comparación textual
                }
            }
        }
        return false;
    }

    private void validateRequireOneOf(EventFormSchema schema, Map<String, Object> payload) {
        List<String> group = schema.getRequireOneOf();
        if (group.isEmpty()) {
            return;
        }
        boolean anyPresent = group.stream().anyMatch(name -> {
            Object v = value(payload, name);
            return v != null && !v.toString().isBlank();
        });
        if (!anyPresent) {
            throw new IllegalArgumentException(
                    "Se requiere " + String.join(" o ", group) + " para el evento " + schema.getCode());
        }
    }

    private boolean isPresent(Object value) {
        if (value == null) {
            return false;
        }
        // Un 0 numérico real cuenta como informado (peso, litros, ...); una cadena
        // en blanco NO — el front opcional envía "" o null indistintamente.
        if (value instanceof Number) {
            return true;
        }
        return !value.toString().isBlank();
    }

    private Object value(Map<String, Object> payload, String name) {
        return payload == null ? null : payload.get(name);
    }

    private Double asNumber(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String trim(Double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }
}
