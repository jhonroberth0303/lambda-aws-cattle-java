package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.enums.SiteSettingValueType;
import com.cattle.services.SiteSettingsCatalog.Definition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

@Tag("unit")
@Tag("service")
class SiteSettingsCatalogTest {

    @Mock
    private LambdaContext lambdaContext;

    private SiteSettingsCatalog catalog;

    @BeforeEach
    void setUp() {
        openMocks(this);
        catalog = new SiteSettingsCatalog(lambdaContext);
    }

    @Test
    void load_exposesBusinessSettingKeys() {
        assertTrue(catalog.contains("GESTATION_DAYS"));
        assertTrue(catalog.contains("ROTATION_SEMAPHORE_YELLOW_DAYS"));
        assertTrue(catalog.contains("MILK_PRICE_PER_LITER"));
        assertFalse(catalog.contains("NOT_A_KEY"));
        assertFalse(catalog.getVersion().isBlank());
    }

    @Test
    void gestationDays_hasCoercedDoubleDefaultAndBounds() {
        Definition def = catalog.find("GESTATION_DAYS").orElseThrow();

        assertEquals(SiteSettingValueType.NUMBER, def.getType());
        assertEquals(279.0, def.getDefaultValue());
        assertEquals(150.0, def.getMin());
        assertEquals(400.0, def.getMax());
        assertNotNull(def.getLabel());
        assertEquals("reproduccion", def.getGroup());
    }

    @Test
    void list_preservesYamlOrder() {
        assertEquals("GESTATION_DAYS", catalog.list().get(0).getKey());
    }

    @Test
    void coerce_numberFromNonNumeric_fails() {
        assertThrows(IllegalStateException.class,
                () -> SiteSettingsCatalog.coerce("K", SiteSettingValueType.NUMBER, "abc"));
    }
}
