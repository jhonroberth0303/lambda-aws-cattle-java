package com.cattle.builders;

import com.cattle.entities.Plan;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PlanBuilder
 * Fase 9 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Builders 22% → 80%
 * Tests: 8
 */
@Tag("unit")
@Tag("builder")
class PlanBuilderTest {

    @Test
    void buildPlanRyegrass_returnsValidPlan() {
        // Act
        Plan result = PlanBuilder.buildPlanRyegrass();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#species#RYEGRASS", result.getPk());
        assertEquals("F001", result.getFarmId());
        assertEquals("RYEGRASS", result.getSpecies());
        assertEquals("GRAZING", result.getPlanType());
        assertEquals(0.5, result.getGrowthRateCmPerDay());
        assertEquals(1, result.getVersion());
        assertNotNull(result.getFertWindows());
        assertEquals(2, result.getFertWindows().size());
    }

    @Test
    void buildPlanRyegrass_hasCorrectRules() {
        // Act
        Plan result = PlanBuilder.buildPlanRyegrass();

        // Assert
        assertNotNull(result.getRules());
        assertEquals(20, result.getRules().getEntryHeightCm());
        assertEquals(7, result.getRules().getExitResidualCm());
        assertEquals(35, result.getRules().getRestDaysMin());
    }

    @Test
    void buildPlanRyegrassFromJson_returnsValidPlan() {
        // Act
        Plan result = PlanBuilder.buildPlanRyegrassFromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#species#RYEGRASS", result.getPk());
        assertEquals("RYEGRASS", result.getSpecies());
        assertEquals("GRAZING", result.getPlanType());
        assertNotNull(result.getRules());
        assertEquals(20, result.getRules().getEntryHeightCm()); // Different from direct builder
    }

    @Test
    void buildPlanCuba22FromJson_returnsValidCutPlan() {
        // Act
        Plan result = PlanBuilder.buildPlanCuba22FromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#species#CUBA22", result.getPk());
        assertEquals("CUBA22", result.getSpecies());
        assertEquals("CUT", result.getPlanType());
        assertEquals(1.7, result.getGrowthRateCmPerDay());
        
        assertNotNull(result.getRules());
        assertEquals(130, result.getRules().getEntryHeightCm());
        assertEquals(25, result.getRules().getExitResidualCm());
        assertEquals(60, result.getRules().getCutIntervalDays());
    }

    @Test
    void buildPlanKikuyoFromJson_returnsValidPlan() {
        // Act
        Plan result = PlanBuilder.buildPlanKikuyoFromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#species#KIKUYO", result.getPk());
        assertEquals("KIKUYO", result.getSpecies());
        assertEquals("GRAZING", result.getPlanType());
        assertEquals(0.5, result.getGrowthRateCmPerDay());
        
        assertNotNull(result.getRules());
        assertEquals(20, result.getRules().getEntryHeightCm());
        assertEquals(6, result.getRules().getExitResidualCm());
        assertEquals(28, result.getRules().getRestDaysMin());
        
        assertNotNull(result.getFertWindows());
        assertEquals(3, result.getFertWindows().size());
    }

    @Test
    void buildPlanAvenaForrajeraFromJson_returnsValidPlan() {
        // Act
        Plan result = PlanBuilder.buildPlanAvenaForrajeraFromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#species#AVENA_FORRAJERA", result.getPk());
        assertEquals("AVENA_FORRAJERA", result.getSpecies());
        assertEquals("GRAZING", result.getPlanType());
        assertEquals(0.4, result.getGrowthRateCmPerDay());
        
        assertNotNull(result.getRules());
        assertEquals(22, result.getRules().getEntryHeightCm());
        assertEquals(8, result.getRules().getExitResidualCm());
        assertEquals(30, result.getRules().getRestDaysMin());
    }

    @Test
    void buildPlanMaizForrajeroFromJson_returnsValidPlan() {
        // Act
        Plan result = PlanBuilder.buildPlanMaizForrajeroFromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#species#MAIZ_FORRAJERO", result.getPk());
        assertEquals("MAIZ_FORRAJERO", result.getSpecies());
        assertEquals("ANNUAL_SILAGE", result.getPlanType());
        assertEquals(1.5, result.getGrowthRateCmPerDay());
    }

    @Test
    void allBuilders_returnDifferentSpecies() {
        // Act
        Plan ryegrass = PlanBuilder.buildPlanRyegrass();
        Plan cuba22 = PlanBuilder.buildPlanCuba22FromJson();
        Plan kikuyo = PlanBuilder.buildPlanKikuyoFromJson();
        Plan avena = PlanBuilder.buildPlanAvenaForrajeraFromJson();

        // Assert
        assertNotEquals(ryegrass.getSpecies(), cuba22.getSpecies());
        assertNotEquals(ryegrass.getSpecies(), kikuyo.getSpecies());
        assertNotEquals(ryegrass.getSpecies(), avena.getSpecies());
        assertNotEquals(cuba22.getSpecies(), kikuyo.getSpecies());
        
        // Verify different plan types
        assertEquals("GRAZING", ryegrass.getPlanType());
        assertEquals("CUT", cuba22.getPlanType());
        assertEquals("GRAZING", kikuyo.getPlanType());
    }
}
