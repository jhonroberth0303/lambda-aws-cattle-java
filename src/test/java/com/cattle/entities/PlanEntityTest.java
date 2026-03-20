package com.cattle.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Plan entity
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Entities
 */
@Tag("unit")
@Tag("entity")
class PlanTest {

    @Test
    void plan_builder_createsInstance() {
        // Act
        Plan plan = Plan.builder()
                .pk("PLAN#farm-001#kikuyo")
                .farmId("farm-001")
                .species("kikuyo")
                .planType("rotation")
                .rules(Plan.Rules.builder()
                        .restDaysMin(30)
                        .entryHeightCm(25)
                        .exitResidualCm(5)
                        .build())
                .build();

        // Assert
        assertNotNull(plan);
        assertEquals("PLAN#farm-001#kikuyo", plan.getPk());
        assertEquals("kikuyo", plan.getSpecies());
        assertEquals("rotation", plan.getPlanType());
    }

    @Test
    void plan_noArgsConstructor_createsInstance() {
        // Act
        Plan plan = new Plan();

        // Assert
        assertNotNull(plan);
        assertNull(plan.getPk());
    }

    @Test
    void plan_withRules_hasAllRuleFields() {
        // Arrange
        Plan.Rules rules = Plan.Rules.builder()
                .restDaysMin(28)
                .entryHeightCm(20)
                .exitResidualCm(6)
                .cutIntervalDays(45)
                .harvestDaysAfterSowing(90)
                .harvestCue("flowering")
                .rowSpacingCm(30)
                .targetDryMatterPercent(25)
                .build();

        Plan plan = Plan.builder()
                .pk("PLAN#farm-002#rye_grass")
                .farmId("farm-002")
                .species("rye_grass")
                .rules(rules)
                .build();

        // Assert
        assertNotNull(plan.getRules());
        assertEquals(28, plan.getRules().getRestDaysMin());
        assertEquals(20, plan.getRules().getEntryHeightCm());
        assertEquals("flowering", plan.getRules().getHarvestCue());
    }

    @Test
    void plan_settersAndGetters_work() {
        // Arrange
        Plan plan = new Plan();

        // Act
        plan.setPk("PLAN#farm-003#brachiaria");
        plan.setFarmId("farm-003");
        plan.setSpecies("brachiaria");
        plan.setPlanType("grazing");
        plan.setNotes("Plan para potrero norte");
        plan.setVersion(1);
        plan.setUpdatedAt("2026-01-20T10:00:00Z");

        // Assert
        assertEquals("PLAN#farm-003#brachiaria", plan.getPk());
        assertEquals("farm-003", plan.getFarmId());
        assertEquals("brachiaria", plan.getSpecies());
        assertEquals("Plan para potrero norte", plan.getNotes());
    }

    @Test
    void plan_rules_noArgsConstructor_createsInstance() {
        // Act
        Plan.Rules rules = new Plan.Rules();

        // Assert
        assertNotNull(rules);
        assertNull(rules.getRestDaysMin());
    }

    @Test
    void plan_rules_settersAndGetters_work() {
        // Arrange
        Plan.Rules rules = new Plan.Rules();

        // Act
        rules.setRestDaysMin(35);
        rules.setEntryHeightCm(30);
        rules.setExitResidualCm(8);
        rules.setCutIntervalDays(60);
        rules.setHarvestDaysAfterSowing(120);
        rules.setHarvestCue("seeding");
        rules.setRowSpacingCm(40);
        rules.setTargetDryMatterPercent(30);

        // Assert
        assertEquals(35, rules.getRestDaysMin());
        assertEquals(30, rules.getEntryHeightCm());
        assertEquals("seeding", rules.getHarvestCue());
        assertEquals(30, rules.getTargetDryMatterPercent());
    }

    @Test
    void plan_withFertWindows_setCorrectly() {
        // Arrange
        Plan plan = Plan.builder()
                .pk("PLAN#farm-001#kikuyo")
                .farmId("farm-001")
                .species("kikuyo")
                .fertWindows(java.util.List.of("march", "september"))
                .build();

        // Assert
        assertNotNull(plan.getFertWindows());
        assertEquals(2, plan.getFertWindows().size());
        assertTrue(plan.getFertWindows().contains("march"));
    }
}
