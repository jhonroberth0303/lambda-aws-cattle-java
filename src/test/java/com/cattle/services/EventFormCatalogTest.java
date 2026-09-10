package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.forms.EventFormSchemasDTO;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.EventType;
import com.cattle.forms.EventFormSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("service")
class EventFormCatalogTest {

    @Mock
    private LambdaContext lambdaContext;

    private EventFormCatalog catalog;

    @BeforeEach
    void setUp() {
        openMocks(this);
        catalog = new EventFormCatalog(lambdaContext);
    }

    @Test
    void exposesASchemaForEveryBovineEventType() {
        EventFormSchemasDTO dto = catalog.getDomainSchemas("bovine-events");

        assertThat(dto.getSchemas()).hasSize(BovineEventType.values().length);
        for (BovineEventType type : BovineEventType.values()) {
            assertThat(catalog.findBovineEvent(type.name()))
                    .as("esquema de %s", type)
                    .isPresent();
        }
    }

    @Test
    void everySchemaKeyIsAValidBovineEventType() {
        EventFormSchemasDTO dto = catalog.getDomainSchemas("bovine-events");
        assertThat(dto.getSchemas().keySet())
                .allSatisfy(code -> BovineEventType.valueOf(code));
    }

    @Test
    void everySchemaHasTitleSubmitLabelAndFields() {
        catalog.getDomainSchemas("bovine-events").getSchemas().forEach((code, schema) -> {
            assertThat(schema.getTitle()).as("title de %s", code).isNotBlank();
            assertThat(schema.getSubmitLabel()).as("submitLabel de %s", code).isNotBlank();
            assertThat(schema.getFields()).as("fields de %s", code).isNotEmpty();
        });
    }

    @Test
    void pesajeSchema_hasRequiredWeightAndOptionalScore() {
        EventFormSchema pesaje = catalog.findBovineEvent("PESAJE").orElseThrow();

        EventFormSchema.Field weight = pesaje.getFields().get(0);
        assertThat(weight.getName()).isEqualTo("weightKg");
        assertThat(weight.getType()).isEqualTo(EventFormSchema.Field.Type.NUMBER);
        assertThat(weight.isRequired()).isTrue();
        assertThat(weight.getMin()).isEqualTo(0.0);

        EventFormSchema.Field score = pesaje.getFields().get(1);
        assertThat(score.isRequired()).isFalse();
        assertThat(score.getOptions()).hasSize(5);
    }

    @Test
    void inseminacionSchema_carriesRequireOneOf() {
        EventFormSchema ins = catalog.findBovineEvent("INSEMINACION").orElseThrow();
        assertThat(ins.getRequireOneOf()).containsExactly("bullId", "semenBatch");
    }

    @Test
    void muerteSchema_doesNotAppendNotes() {
        assertThat(catalog.findBovineEvent("MUERTE").orElseThrow().isAppendNotes()).isFalse();
        assertThat(catalog.findBovineEvent("PESAJE").orElseThrow().isAppendNotes()).isTrue();
    }

    @Test
    void etag_isQuotedStableAndContentDerived() {
        String etag = catalog.getEtag();
        assertThat(etag).startsWith("\"").endsWith("\"");

        EventFormCatalog other = new EventFormCatalog(lambdaContext);
        assertThat(other.getEtag()).isEqualTo(etag);
    }

    @Test
    void getDomainSchemas_unknownDomain_returnsNull() {
        assertNull(catalog.getDomainSchemas("no-existe"));
    }

    @Test
    void pastureEvents_coversEventTypeExceptPreEntryCheck() {
        assertThat(catalog.getDomainSchemas("pasture-events").getSchemas())
                .hasSize(EventType.values().length - 1)
                .doesNotContainKey("PRE_ENTRY_CHECK");
        assertThat(catalog.findEvent("pasture-events", "OPEN")).isPresent();
        assertThat(catalog.findEvent("pasture-events", "PRE_ENTRY_CHECK")).isEmpty();
    }
}
