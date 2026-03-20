package com.cattle.services;

import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.entities.bovines.ProfileLifecycle;
import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.Source;
import com.cattle.rules.BovineCategoryRulesConfig;
import com.cattle.services.LifecycleRecalculationService.RecalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para LifecycleRecalculationService
 * Verifica el comportamiento del batch de recalculación.
 */
@Tag("unit")
@Tag("fast")
@Tag("services")
@DisplayName("LifecycleRecalculationService Tests")
class LifecycleRecalculationServiceTest {

    private LifecycleRecalculationService service;
    private BovineCategoryRulesService rulesService;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @BeforeEach
    void setUp() {
        BovineCategoryRulesConfig config = createTestConfig();
        rulesService = new BovineCategoryRulesService(config);
        service = new LifecycleRecalculationService(rulesService);
    }

    private BovineCategoryRulesConfig createTestConfig() {
        BovineCategoryRulesConfig config = new BovineCategoryRulesConfig();
        
        // Create lifeStage rules
        BovineCategoryRulesConfig.LifeStageRule newborn = new BovineCategoryRulesConfig.LifeStageRule();
        newborn.setMinAge(0);
        newborn.setMaxAge(1);
        newborn.setStage("NEWBORN");

        BovineCategoryRulesConfig.LifeStageRule calf = new BovineCategoryRulesConfig.LifeStageRule();
        calf.setMinAge(1);
        calf.setMaxAge(6);
        calf.setStage("CALF");

        BovineCategoryRulesConfig.LifeStageRule weaned = new BovineCategoryRulesConfig.LifeStageRule();
        weaned.setMinAge(6);
        weaned.setMaxAge(9);
        weaned.setStage("WEANED");

        BovineCategoryRulesConfig.LifeStageRule grower = new BovineCategoryRulesConfig.LifeStageRule();
        grower.setMinAge(9);
        grower.setMaxAge(24);
        grower.setStage("GROWER");

        BovineCategoryRulesConfig.LifeStageRule adult = new BovineCategoryRulesConfig.LifeStageRule();
        adult.setMinAge(24);
        adult.setMaxAge(999);
        adult.setStage("ADULT");

        // Create female category rules
        BovineCategoryRulesConfig.CategoryRule femaleCalf = new BovineCategoryRulesConfig.CategoryRule();
        femaleCalf.setMinAge(0);
        femaleCalf.setMaxAge(6);
        femaleCalf.setCategory("CALF");

        BovineCategoryRulesConfig.CategoryRule heifer = new BovineCategoryRulesConfig.CategoryRule();
        heifer.setMinAge(6);
        heifer.setMaxAge(24);
        heifer.setCategory("HEIFER");

        BovineCategoryRulesConfig.CategoryRule cow = new BovineCategoryRulesConfig.CategoryRule();
        cow.setMinAge(24);
        cow.setMaxAge(999);
        cow.setCategory("COW");

        // Create male category rules
        BovineCategoryRulesConfig.CategoryRule maleCalf = new BovineCategoryRulesConfig.CategoryRule();
        maleCalf.setMinAge(0);
        maleCalf.setMaxAge(6);
        maleCalf.setCategory("CALF");

        BovineCategoryRulesConfig.CategoryRule youngBull = new BovineCategoryRulesConfig.CategoryRule();
        youngBull.setMinAge(6);
        youngBull.setMaxAge(24);
        youngBull.setCategory("YOUNG_BULL");

        BovineCategoryRulesConfig.CategoryRule bull = new BovineCategoryRulesConfig.CategoryRule();
        bull.setMinAge(24);
        bull.setMaxAge(999);
        bull.setCategory("BULL");

        // Create OX rule
        BovineCategoryRulesConfig.OxRule ox = new BovineCategoryRulesConfig.OxRule();
        ox.setCastrated(true);
        ox.setCategory("OX");

        // Assemble farm rules
        BovineCategoryRulesConfig.FarmRules farmRules = new BovineCategoryRulesConfig.FarmRules();
        farmRules.setLifeStage(List.of(newborn, calf, weaned, grower, adult));
        farmRules.setFemale(List.of(femaleCalf, heifer, cow));
        farmRules.setMale(List.of(maleCalf, youngBull, bull));
        farmRules.setOx(ox);

        config.setFarms(Map.of("finca1", farmRules, "default", farmRules));
        return config;
    }

    private BovineIdentityItem createBovine(int ageMonths, String gender) {
        LocalDate bornDate = LocalDate.now().minusMonths(ageMonths);
        return BovineIdentityItem.builder()
                .pk("BOVINE#100")
                .sk("IDENTITY")
                .bovineId(100)
                .name("Test Bovine")
                .gender(gender)
                .bornDate(bornDate.format(ISO_DATE))
                .farmId("finca1")
                .build();
    }

    private ProfileLifecycle createLifecycle(LifeStage lifeStage, BovineCategory category, Source categorySource) {
        return ProfileLifecycle.builder()
                .pk("BOVINE#100")
                .sk("PROFILE#LIFECYCLE")
                .lifeStage(lifeStage)
                .lifeStageSource(Source.AUTO)
                .category(category)
                .categorySource(categorySource)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("Recalculation Tests")
    class RecalculationTests {

        @Test
        @DisplayName("Detecta cambio de lifeStage de CALF a WEANED")
        void recalculate_lifeStageChange_detected() {
            BovineIdentityItem bovineIdentityItem = createBovine(7, "female"); // 7 meses
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.CALF, BovineCategory.CALF, Source.AUTO);

            RecalculationResult result = service.recalculate(bovineIdentityItem, lifecycle);

            assertTrue(result.isLifeStageChanged());
            assertEquals(LifeStage.WEANED, result.getNewLifeStage());
        }

        @Test
        @DisplayName("Detecta cambio de category de CALF a HEIFER")
        void recalculate_categoryChange_detected() {
            BovineIdentityItem bovineIdentityItem = createBovine(7, "female"); // 7 meses
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.CALF, BovineCategory.CALF, Source.AUTO);

            RecalculationResult result = service.recalculate(bovineIdentityItem, lifecycle);

            assertTrue(result.isCategoryChanged());
            assertEquals(BovineCategory.HEIFER, result.getNewCategory());
        }

        @Test
        @DisplayName("No detecta cambio si ya está actualizado")
        void recalculate_noChange_detected() {
            BovineIdentityItem bovineIdentityItem = createBovine(14, "female"); // 14 meses
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.GROWER, BovineCategory.HEIFER, Source.AUTO);

            RecalculationResult result = service.recalculate(bovineIdentityItem, lifecycle);

            assertFalse(result.hasChanges());
        }

        @Test
        @DisplayName("Respeta categorySource MANUAL - no cambia categoría")
        void recalculate_manualCategory_notChanged() {
            BovineIdentityItem bovineIdentityItem = createBovine(30, "male"); // 30 meses, debería ser BULL
            // Pero el usuario lo marcó manualmente como YOUNG_BULL
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.GROWER, BovineCategory.YOUNG_BULL, Source.MANUAL);

            RecalculationResult result = service.recalculate(bovineIdentityItem, lifecycle);

            // LifeStage sí cambia (siempre AUTO)
            assertTrue(result.isLifeStageChanged());
            assertEquals(LifeStage.ADULT, result.getNewLifeStage());
            
            // Category NO cambia (es MANUAL)
            assertFalse(result.isCategoryChanged());
        }

        @Test
        @DisplayName("Calcula nextRecalcDate correctamente")
        void recalculate_calculatesNextRecalcDate() {
            BovineIdentityItem bovineIdentityItem = createBovine(5, "female"); // 5 meses
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.CALF, BovineCategory.CALF, Source.AUTO);

            RecalculationResult result = service.recalculate(bovineIdentityItem, lifecycle);

            assertNotNull(result.getNextRecalcDate());
            // Próximo threshold es 6 meses (WEANED)
            LocalDate bornDate = LocalDate.parse(bovineIdentityItem.getBornDate(), ISO_DATE);
            assertEquals(bornDate.plusMonths(6), result.getNextRecalcDate());
        }
    }

    @Nested
    @DisplayName("Apply Recalculation Tests")
    class ApplyRecalculationTests {

        @Test
        @DisplayName("Aplica cambios de lifeStage y category")
        void applyRecalculation_appliesChanges() {
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.CALF, BovineCategory.CALF, Source.AUTO);
            RecalculationResult result = new RecalculationResult(
                    true, true, 
                    LifeStage.WEANED, BovineCategory.HEIFER,
                    LocalDate.now().plusMonths(2)
            );

            service.applyRecalculation(lifecycle, result);

            assertEquals(LifeStage.WEANED, lifecycle.getLifeStage());
            assertEquals(Source.AUTO, lifecycle.getLifeStageSource());
            assertEquals(BovineCategory.HEIFER, lifecycle.getCategory());
            assertEquals(Source.AUTO, lifecycle.getCategorySource());
            assertNotNull(lifecycle.getLastEvaluatedAt());
            assertNotNull(lifecycle.getNextRecalcDate());
        }

        @Test
        @DisplayName("No aplica cambio de category si es MANUAL")
        void applyRecalculation_respectsManualCategory() {
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.GROWER, BovineCategory.YOUNG_BULL, Source.MANUAL);
            RecalculationResult result = new RecalculationResult(
                    true, false, // categoryChanged = false porque es MANUAL
                    LifeStage.ADULT, null,
                    LocalDate.now().plusYears(1)
            );

            service.applyRecalculation(lifecycle, result);

            assertEquals(LifeStage.ADULT, lifecycle.getLifeStage());
            // Category no cambio
            assertEquals(BovineCategory.YOUNG_BULL, lifecycle.getCategory());
            assertEquals(Source.MANUAL, lifecycle.getCategorySource());
        }

        @Test
        @DisplayName("Actualiza nextRecalcDate")
        void applyRecalculation_updatesNextRecalcDate() {
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.CALF, BovineCategory.CALF, Source.AUTO);
            LocalDate nextRecalc = LocalDate.now().plusMonths(1);
            RecalculationResult result = new RecalculationResult(
                    true, true,
                    LifeStage.WEANED, BovineCategory.HEIFER,
                    nextRecalc
            );

            service.applyRecalculation(lifecycle, result);

            assertEquals(nextRecalc.format(ISO_DATE), lifecycle.getNextRecalcDate());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Maneja bovine null sin error")
        void recalculate_nullBovine_handlesGracefully() {
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.CALF, BovineCategory.CALF, Source.AUTO);

            RecalculationResult result = service.recalculate(null, lifecycle);

            assertFalse(result.hasChanges());
        }

        @Test
        @DisplayName("Maneja bornDate null sin error")
        void recalculate_nullBornDate_handlesGracefully() {
            BovineIdentityItem bovineIdentityItem = BovineIdentityItem.builder()
                    .pk("BOVINE#100")
                    .sk("IDENTITY")
                    .gender("female")
                    .bornDate(null)
                    .build();
            ProfileLifecycle lifecycle = createLifecycle(LifeStage.CALF, BovineCategory.CALF, Source.AUTO);

            RecalculationResult result = service.recalculate(bovineIdentityItem, lifecycle);

            assertFalse(result.hasChanges());
        }

        @Test
        @DisplayName("Maneja gender null en bovine - no infiere category")
        void recalculate_nullGender_handlesGracefully() {
            BovineIdentityItem bovineIdentityItem = BovineIdentityItem.builder()
                    .pk("BOVINE#100")
                    .sk("IDENTITY")
                    .bornDate(LocalDate.now().minusMonths(14).format(ISO_DATE))
                    .farmId("finca1")
                    .gender(null) // No tiene gender
                    .build();
            ProfileLifecycle lifecycle = ProfileLifecycle.builder()
                    .pk("BOVINE#100")
                    .sk("PROFILE#LIFECYCLE")
                    .lifeStage(LifeStage.CALF)
                    .category(BovineCategory.CALF)
                    .categorySource(Source.AUTO)
                    .build();

            RecalculationResult result = service.recalculate(bovineIdentityItem, lifecycle);

            // Sin gender no puede inferir category
            assertNull(result.getNewCategory());
        }
    }
}
