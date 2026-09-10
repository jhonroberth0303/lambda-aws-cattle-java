package com.cattle.rules;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios para BovineCategoryRulesLoader.
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Rules
 */
@Tag("unit")
@Tag("fast")
@Tag("rules")
class BovineCategoryRulesLoaderTest {

    private static final String RULES_FILE = "bovine-category-rules.yaml";

    @Test
    void loadRules_existingFile_parsesFarmRules() {
        BovineCategoryRulesConfig config = BovineCategoryRulesLoader.loadRules(RULES_FILE);

        assertNotNull(config);
        assertNotNull(config.getFarms());
        assertTrue(config.getFarms().containsKey("FARM#001"));
    }

    @Test
    void loadRules_existingFile_populatesLifeStageAndCategoryRules() {
        BovineCategoryRulesConfig config = BovineCategoryRulesLoader.loadRules(RULES_FILE);
        BovineCategoryRulesConfig.FarmRules farm = config.getFarms().get("FARM#001");

        assertNotNull(farm.getLifeStage());
        assertFalse(farm.getLifeStage().isEmpty());
        assertNotNull(farm.getFemale());
        assertFalse(farm.getFemale().isEmpty());

        BovineCategoryRulesConfig.LifeStageRule newborn = farm.getLifeStage().get(0);
        assertEquals(0, newborn.getMinAge());
        assertEquals("NEWBORN", newborn.getStage());
    }

    @Test
    void loadRules_missingFile_throwsRuntimeExceptionNamingThePath() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> BovineCategoryRulesLoader.loadRules("no-existe/reglas.yaml"));

        assertTrue(ex.getMessage().contains("no-existe/reglas.yaml"));
    }
}
