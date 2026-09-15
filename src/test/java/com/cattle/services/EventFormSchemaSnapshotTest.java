package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Guardia anti-deriva (DT-20260910, ítem 6): {@code event-forms.yml} es la fuente única de
 * los esquemas de formulario, pero el front mantiene un espejo a mano
 * ({@code BUNDLED_EVENT_FORMS} / {@code BUNDLED_PASTURE_EVENT_FORMS} en
 * {@code cattle-front/src/domain/}) como fallback offline. Nada comparaba ambos lados.
 * <p>
 * Este test serializa {@link EventFormCatalog#getDomainSchemas(String)} con el mismo
 * mapper canónico que ya usa {@code EventFormCatalog.hash(...)} y lo compara contra un
 * snapshot commiteado en {@code src/test/resources/schema-snapshots/}. Ese mismo snapshot
 * lo lee un test del front (ver {@code cattle-front/src/domain/schemaDrift.test.js}) y lo
 * compara contra los bundles — cualquier divergencia en cualquiera de los dos lados hace
 * fallar la suite correspondiente.
 * <p>
 * Si este test falla porque {@code event-forms.yml} cambió intencionalmente, regenera el
 * snapshot: corre el test, copia el JSON del mensaje de fallo (o de stdout) al archivo
 * correspondiente en {@code schema-snapshots/}, y decide si el bundle del front también
 * necesita actualizarse.
 */
class EventFormSchemaSnapshotTest {

    private static final Path SNAPSHOT_DIR = Path.of("src", "test", "resources", "schema-snapshots");

    @Mock
    private LambdaContext lambdaContext;

    private EventFormCatalog newCatalog() {
        openMocks(this);
        return new EventFormCatalog(lambdaContext);
    }

    /**
     * Serializa con salto de línea LF forzado (no el de la plataforma): el pretty-printer
     * de Jackson por defecto usa {@code System.lineSeparator()}, que en Windows es CRLF.
     * Un snapshot commiteado con CRLF es frágil frente a cualquier herramienta del pipeline
     * (editor, futuro `git config core.autocrlf`) que normalice el archivo a LF — forzar LF
     * en la generación y normalizar en la comparación evita falsos positivos por esto.
     */
    private String canonicalJson(Object value) throws IOException {
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
                .withObjectIndenter(new DefaultIndenter("  ", "\n"))
                .withArrayIndenter(new DefaultIndenter("  ", "\n"));
        ObjectMapper mapper = JsonMapper.builder()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .build();
        return mapper.writer(printer).writeValueAsString(value) + "\n";
    }

    private static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }

    @ParameterizedTest(name = "dominio: {0}")
    @Tag("unit")
    @Tag("fast")
    @ValueSource(strings = {"bovine-events", "pasture-events"})
    void domainSchema_matchesCommittedSnapshot(String domain) throws IOException {
        EventFormCatalog catalog = newCatalog();
        String actualJson = canonicalJson(catalog.getDomainSchemas(domain).getSchemas());

        Path snapshotFile = SNAPSHOT_DIR.resolve(domain + ".json");
        if (!Files.exists(snapshotFile)) {
            fail("No existe el snapshot " + snapshotFile
                    + ". Créalo con el contenido de 'actualJson' (ver salida de este test) y confírmalo con el front.");
        }
        String expectedJson = Files.readString(snapshotFile, StandardCharsets.UTF_8);

        assertEquals(normalizeLineEndings(expectedJson), normalizeLineEndings(actualJson),
                "El esquema de '" + domain + "' en event-forms.yml divergió del snapshot commiteado en "
                        + snapshotFile + ". Si el cambio es intencional, regenera el snapshot y revisa si "
                        + "cattle-front/src/domain/ necesita el mismo ajuste (ver EventFormSchemaSnapshotTest).");
    }

    /**
     * Solo para (re)generar los snapshots a mano tras un cambio intencional en
     * event-forms.yml — no es un test de verificación. Deshabilitado por defecto para
     * que un `./gradlew test` normal nunca sobreescriba los snapshots en silencio.
     */
    @Disabled("Habilitar manualmente solo para regenerar los snapshots")
    @Test
    @Tag("unit")
    void regenerateSnapshots() throws IOException {
        Files.createDirectories(SNAPSHOT_DIR);
        EventFormCatalog catalog = newCatalog();
        for (String domain : new String[]{"bovine-events", "pasture-events"}) {
            String json = canonicalJson(catalog.getDomainSchemas(domain).getSchemas());
            Files.writeString(SNAPSHOT_DIR.resolve(domain + ".json"), json, StandardCharsets.UTF_8);
        }
    }
}
