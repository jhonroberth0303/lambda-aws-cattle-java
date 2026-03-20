package com.cattle.builders;

import com.cattle.entities.Pasture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PastureBuilder
 * Fase 9 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Builders 22% → 80%
 * Tests: 6
 */
@Tag("unit")
@Tag("builder")
class PastureBuilderTest {

    @Test
    void buildPastureRyegrass_returnsValidPasture() {
        // Act
        Pasture result = PastureBuilder.buildPastureRyegrass();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#pasture#P-01", result.getPk());
        assertEquals("F001", result.getFarmId());
        assertEquals("P-01", result.getId());
        assertEquals("Potrero 1", result.getName());
        assertEquals("RYEGRASS", result.getSpecies());
        assertEquals("DISPONIBLE", result.getStatus());
        assertEquals("NINGUNO", result.getSubstatus());
        assertEquals(0.4, result.getAreaHa());
        assertEquals(5, result.getCurrentHeightCm());
        assertEquals("2025-10-06", result.getEstablishmentDate());
        assertEquals("2025-10-01", result.getLastUseAt());
    }

    @Test
    void buildPastureRyegrass_hasCorrectGSIValues() {
        // Act
        Pasture result = PastureBuilder.buildPastureRyegrass();

        // Assert
        assertEquals("farm#F001#species#RYEGRASS", result.getGsi1pk());
        assertEquals(0, result.getGsi1sk());
        assertEquals("farm#F001#blocked#false", result.getGsi2pk());
        assertEquals(0, result.getGsi2sk());
    }

    @Test
    void buildPastureRyegrassFromJson_returnsValidPasture() {
        // Act
        Pasture result = PastureBuilder.buildPastureRyegrassFromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#pasture#P-01", result.getPk());
        assertEquals("P-01", result.getId());
        assertEquals("RYEGRASS", result.getSpecies());
        assertEquals("DISPONIBLE", result.getStatus());
        assertEquals(21, result.getCurrentHeightCm()); // Different from direct builder
    }

    @Test
    void buildPastureCuba22FromJson_returnsValidPasture() {
        // Act
        Pasture result = PastureBuilder.buildPastureCuba22FromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#pasture#P-05", result.getPk());
        assertEquals("P-05", result.getId());
        assertEquals("Cuba 22 - Colinda con el Camino", result.getName());
        assertEquals("CUBA22", result.getSpecies());
        assertEquals("MANTENIMIENTO", result.getStatus());
        assertEquals("ESTABLECIMIENTO", result.getSubstatus());
        assertEquals(0.1, result.getAreaHa());
        assertEquals("Establecimiento/arraigo", result.getBlockReason());
    }

    @Test
    void buildPastureKikuyoFromJson_returnsValidPasture() {
        // Act
        Pasture result = PastureBuilder.buildPastureKikuyoFromJson();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#pasture#P-08", result.getPk());
        assertEquals("P-08", result.getId());
        assertEquals("Potrero 8 - Con agua en el lindero", result.getName());
        assertEquals("KIKUYO", result.getSpecies());
        assertEquals("EN_DESCANSO", result.getStatus());
        assertEquals("NINGUNO", result.getSubstatus());
        assertEquals(0.4, result.getAreaHa());
        assertEquals(5, result.getCurrentHeightCm());
    }

    @Test
    void allBuilders_returnDifferentPastures() {
        // Act
        Pasture ryegrass = PastureBuilder.buildPastureRyegrass();
        Pasture cuba22 = PastureBuilder.buildPastureCuba22FromJson();
        Pasture kikuyo = PastureBuilder.buildPastureKikuyoFromJson();

        // Assert
        assertNotEquals(ryegrass.getId(), cuba22.getId());
        assertNotEquals(ryegrass.getId(), kikuyo.getId());
        assertNotEquals(cuba22.getId(), kikuyo.getId());
        
        assertNotEquals(ryegrass.getSpecies(), cuba22.getSpecies());
        assertNotEquals(ryegrass.getSpecies(), kikuyo.getSpecies());
        assertNotEquals(cuba22.getSpecies(), kikuyo.getSpecies());
    }
}
