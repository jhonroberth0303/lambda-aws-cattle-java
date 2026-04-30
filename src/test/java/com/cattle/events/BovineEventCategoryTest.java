package com.cattle.events;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class BovineEventCategoryTest {

    @Test
    void values_containsExpectedCategories() {
        assertArrayEquals(new BovineEventCategory[]{
                BovineEventCategory.ADMINISTRATIVO,
                BovineEventCategory.IDENTIFICACION,
                BovineEventCategory.SANIDAD,
                BovineEventCategory.REPRODUCCION,
                BovineEventCategory.PRODUCCION,
                BovineEventCategory.ALIMENTACION,
                BovineEventCategory.MANEJO,
                BovineEventCategory.CICLO_DE_VIDA
        }, BovineEventCategory.values());
    }

    @Test
    void valueOf_resolvesKnownCategory() {
        assertEquals(BovineEventCategory.REPRODUCCION, BovineEventCategory.valueOf("REPRODUCCION"));
    }
}