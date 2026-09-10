package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.forms.EventFormSchemasDTO;
import com.cattle.enums.BovineEventType;
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
    void exposesPilotSchemasAndNothingElse() {
        assertThat(catalog.findBovineEvent("PESAJE")).isPresent();
        assertThat(catalog.findBovineEvent("TRATAMIENTO")).isPresent();
        assertThat(catalog.findBovineEvent("INSEMINACION")).isPresent();
        assertThat(catalog.findBovineEvent("MUERTE")).isPresent();
        // Aún no migrados -> validación hardcodeada
        assertThat(catalog.findBovineEvent("COMPRA")).isEmpty();
        assertThat(catalog.findBovineEvent("PARTO")).isEmpty();
    }

    @Test
    void everySchemaKeyIsAValidBovineEventType() {
        EventFormSchemasDTO dto = catalog.getDomainSchemas("bovine-events");
        assertThat(dto.getSchemas().keySet())
                .allSatisfy(code -> BovineEventType.valueOf(code));
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
        assertNull(catalog.getDomainSchemas("pasture-events"));
    }
}
