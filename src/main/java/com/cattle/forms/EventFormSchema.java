package com.cattle.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

/**
 * Esquema de formulario de un tipo de evento (EP-20260909, Fase 2).
 *
 * <p>Fuente única: se define en {@code event-forms.yml} y se consume tanto por el
 * front (para renderizar el formulario) como por el backend (para validar el
 * payload). Inmutable: se construye una vez en {@link com.cattle.services.EventFormCatalog}.
 */
public final class EventFormSchema {

    private final String code;
    private final String title;
    private final String submitLabel;
    private final boolean appendNotes;
    private final List<String> requireOneOf;
    private final List<Field> fields;

    public EventFormSchema(String code, String title, String submitLabel, boolean appendNotes,
                           List<String> requireOneOf, List<Field> fields) {
        this.code = code;
        this.title = title;
        this.submitLabel = submitLabel;
        this.appendNotes = appendNotes;
        this.requireOneOf = requireOneOf == null ? List.of() : List.copyOf(requireOneOf);
        this.fields = fields == null ? List.of() : List.copyOf(fields);
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getSubmitLabel() {
        return submitLabel;
    }

    /** Si el renderer debe añadir un campo genérico de "Observaciones" opcional. */
    public boolean isAppendNotes() {
        return appendNotes;
    }

    /** Nombres de campos de los que al menos uno debe venir informado, o vacío. */
    public List<String> getRequireOneOf() {
        return requireOneOf;
    }

    public List<Field> getFields() {
        return fields;
    }

    /** Un campo del formulario. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Field {

        /** Tipos soportados por el renderer y el validador. */
        public enum Type {
            TEXT, NUMBER, TEXTAREA, DATE;

            @JsonValue
            public String toJson() {
                return name().toLowerCase();
            }
        }

        private final String name;
        private final String label;
        private final Type type;
        private final boolean required;
        private final Double min;
        private final Double max;
        private final Double step;
        private final Integer maxLength;
        private final String placeholder;
        private final String hint;
        private final String minField;
        private final boolean calvingEstimate;
        private final List<Option> options;
        private final String defaultValue;
        private final String optionsFrom;

        public Field(String name, String label, Type type, boolean required, Double min, Double max,
                     Double step, Integer maxLength, String placeholder, String hint, String minField,
                     boolean calvingEstimate, List<Option> options, String defaultValue, String optionsFrom) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.required = required;
            this.min = min;
            this.max = max;
            this.step = step;
            this.maxLength = maxLength;
            this.placeholder = placeholder;
            this.hint = hint;
            this.minField = minField;
            this.calvingEstimate = calvingEstimate;
            this.options = options == null ? List.of() : List.copyOf(options);
            this.defaultValue = defaultValue;
            this.optionsFrom = optionsFrom;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public Type getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }

        public Double getMin() {
            return min;
        }

        public Double getMax() {
            return max;
        }

        public Double getStep() {
            return step;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public String getHint() {
            return hint;
        }

        /** Nombre de otro campo (o {@code eventAt}) que actúa como cota inferior en fechas. */
        public String getMinField() {
            return minField;
        }

        public boolean isCalvingEstimate() {
            return calvingEstimate;
        }

        public List<Option> getOptions() {
            return options;
        }

        /** Valor inicial del campo (front); null si no aplica. */
        @com.fasterxml.jackson.annotation.JsonProperty("default")
        public String getDefaultValue() {
            return defaultValue;
        }

        /** Catálogo de dominio del que el front deriva las opciones del select. */
        public String getOptionsFrom() {
            return optionsFrom;
        }
    }

    /** Opción de un campo con lista cerrada de valores. */
    public static final class Option {
        private final String value;
        private final String label;

        public Option(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}
