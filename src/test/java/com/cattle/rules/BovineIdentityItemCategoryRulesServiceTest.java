package com.cattle.rules;

import com.cattle.enums.profiles.BovineCategory;
import com.cattle.enums.profiles.LifeStage;
import com.cattle.enums.profiles.Sex;
import com.cattle.enums.profiles.Source;
import com.cattle.services.BovineCategoryRulesService;
import com.cattle.services.BovineCategoryRulesService.InferenceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BovineCategoryRulesService
 * Verifica la inferencia de LifeStage y BovineCategory basada en reglas configurables.
 */
@Tag("unit")
@Tag("fast")
@Tag("rules")
@DisplayName("BovineCategoryRulesService Tests")
class BovineIdentityItemCategoryRulesServiceTest {

    private BovineCategoryRulesService service;
    private BovineCategoryRulesConfig config;

    @BeforeEach
    void setUp() {
        config = createTestConfig();
        service = new BovineCategoryRulesService(config);
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

    @Nested
    @DisplayName("LifeStage Inference Tests")
    class LifeStageTests {

        @Test
        @DisplayName("Recién nacido (0 meses) → NEWBORN")
        void inferLifeStage_newborn_returnsNewborn() {
            LifeStage result = service.inferLifeStage("finca1", 0);
            assertEquals(LifeStage.NEWBORN, result);
        }

        @Test
        @DisplayName("Ternero (3 meses) → CALF")
        void inferLifeStage_threeMOnths_returnsCalf() {
            LifeStage result = service.inferLifeStage("finca1", 3);
            assertEquals(LifeStage.CALF, result);
        }

        @Test
        @DisplayName("Destete (7 meses) → WEANED")
        void inferLifeStage_sevenMonths_returnsWeaned() {
            LifeStage result = service.inferLifeStage("finca1", 7);
            assertEquals(LifeStage.WEANED, result);
        }

        @Test
        @DisplayName("Levante (12 meses) → GROWER")
        void inferLifeStage_twelveMonths_returnsGrower() {
            LifeStage result = service.inferLifeStage("finca1", 12);
            assertEquals(LifeStage.GROWER, result);
        }

        @Test
        @DisplayName("Adulto (30 meses) → ADULT")
        void inferLifeStage_thirtyMonths_returnsAdult() {
            LifeStage result = service.inferLifeStage("finca1", 30);
            assertEquals(LifeStage.ADULT, result);
        }

        @Test
        @DisplayName("Farm desconocida usa reglas default")
        void inferLifeStage_unknownFarm_usesDefault() {
            LifeStage result = service.inferLifeStage("fincaDesconocida", 12);
            assertEquals(LifeStage.GROWER, result);
        }
    }

    @Nested
    @DisplayName("BovineCategory Inference Tests")
    class CategoryTests {

        @Test
        @DisplayName("Hembra joven (3 meses) → CALF")
        void inferCategory_youngFemale_returnsCalf() {
            BovineCategory result = service.inferCategory("finca1", Sex.FEMALE, 3, false);
            assertEquals(BovineCategory.CALF, result);
        }

        @Test
        @DisplayName("Hembra (14 meses) → HEIFER")
        void inferCategory_femaleFourteen_returnsHeifer() {
            BovineCategory result = service.inferCategory("finca1", Sex.FEMALE, 14, false);
            assertEquals(BovineCategory.HEIFER, result);
        }

        @Test
        @DisplayName("Hembra adulta (30 meses) → COW")
        void inferCategory_adultFemale_returnsCow() {
            BovineCategory result = service.inferCategory("finca1", Sex.FEMALE, 30, false);
            assertEquals(BovineCategory.COW, result);
        }

        @Test
        @DisplayName("Macho joven (3 meses) → CALF")
        void inferCategory_youngMale_returnsCalf() {
            BovineCategory result = service.inferCategory("finca1", Sex.MALE, 3, false);
            assertEquals(BovineCategory.CALF, result);
        }

        @Test
        @DisplayName("Macho (14 meses) → YOUNG_BULL")
        void inferCategory_maleFourteen_returnsYoungBull() {
            BovineCategory result = service.inferCategory("finca1", Sex.MALE, 14, false);
            assertEquals(BovineCategory.YOUNG_BULL, result);
        }

        @Test
        @DisplayName("Macho adulto (30 meses) → BULL")
        void inferCategory_adultMale_returnsBull() {
            BovineCategory result = service.inferCategory("finca1", Sex.MALE, 30, false);
            assertEquals(BovineCategory.BULL, result);
        }

        @Test
        @DisplayName("Macho castrado → OX (sin importar edad)")
        void inferCategory_castratedMale_returnsOx() {
            BovineCategory result = service.inferCategory("finca1", Sex.MALE, 14, true);
            assertEquals(BovineCategory.OX, result);
        }
    }

    @Nested
    @DisplayName("Full Inference Tests")
    class FullInferenceTests {

        @Test
        @DisplayName("Inferencia completa para novilla de 14 meses")
        void inferAll_heiferFourteen_returnsCompleteResult() {
            LocalDate bornDate = LocalDate.now().minusMonths(14);
            
            InferenceResult result = service.inferAll("finca1", bornDate, Sex.FEMALE, false, Source.AUTO);
            
            assertEquals(LifeStage.GROWER, result.getLifeStage());
            assertEquals(BovineCategory.HEIFER, result.getCategory());
            assertNotNull(result.getNextRecalcDate());
        }

        @Test
        @DisplayName("Inferencia con categorySource MANUAL no cambia categoría")
        void inferAll_manualCategory_doesNotInferCategory() {
            LocalDate bornDate = LocalDate.now().minusMonths(14);
            
            InferenceResult result = service.inferAll("finca1", bornDate, Sex.FEMALE, false, Source.MANUAL);
            
            assertEquals(LifeStage.GROWER, result.getLifeStage());
            assertNull(result.getCategory()); // No infiere si es MANUAL
        }

        @Test
        @DisplayName("NextRecalcDate se calcula correctamente")
        void inferAll_calculatesNextRecalcDate() {
            LocalDate bornDate = LocalDate.now().minusMonths(5);
            
            InferenceResult result = service.inferAll("finca1", bornDate, Sex.FEMALE, false, Source.AUTO);
            
            // A los 5 meses, próximo threshold es 6 (WEANED)
            LocalDate expectedNext = bornDate.plusMonths(6);
            assertEquals(expectedNext, result.getNextRecalcDate());
        }
    }

    @Nested
    @DisplayName("Age Calculation Tests")
    class AgeCalculationTests {

        @Test
        @DisplayName("Calcula edad en meses correctamente")
        void calculateAgeInMonths_correctCalculation() {
            LocalDate bornDate = LocalDate.now().minusMonths(15);
            int age = service.calculateAgeInMonths(bornDate);
            assertEquals(15, age);
        }

        @Test
        @DisplayName("BornDate null retorna 0")
        void calculateAgeInMonths_nullDate_returnsZero() {
            int age = service.calculateAgeInMonths(null);
            assertEquals(0, age);
        }
    }

    @Nested
    @DisplayName("Backward Compatibility Tests")
    class BackwardCompatibilityTests {

        @Test
        @DisplayName("Método deprecated sigue funcionando con String gender")
        @SuppressWarnings("deprecation")
        void inferCategory_legacyMethod_stillWorks() {
            String result = service.inferCategory("finca1", "female", 14, false);
            assertEquals("HEIFER", result);
        }
    }
}
