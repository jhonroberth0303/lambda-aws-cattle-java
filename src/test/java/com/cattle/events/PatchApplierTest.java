package com.cattle.events;

import com.cattle.entities.Pasture;
import com.cattle.enums.PastureStatus;
import com.cattle.enums.PastureSubstatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PatchApplier
 * Fase 10 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Events 36% → 70%
 * Tests: 12
 */
@Tag("unit")
@Tag("events")
class PatchApplierTest {

    private Pasture pasture;

    @BeforeEach
    void setUp() {
        pasture = Pasture.builder()
                .pk("farm#F001#pasture#P-01")
                .farmId("F001")
                .id("P-01")
                .name("Potrero 1")
                .status("DISPONIBLE")
                .substatus("NINGUNO")
                .areaHa(10.0)
                .build();
    }

    // ==================== applyLocal - SET Operations ====================

    @Test
    void applyLocal_setStatus_updatesStatus() {
        // Arrange
        Map<String, Object> sets = new HashMap<>();
        sets.put("status", PastureStatus.EN_USO);
        EntityPatch patch = new EntityPatch(sets, List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals("EN_USO", pasture.getStatus());
    }

    @Test
    void applyLocal_setStatusAsString_updatesStatus() {
        // Arrange
        Map<String, Object> sets = new HashMap<>();
        sets.put("status", "EN_DESCANSO");
        EntityPatch patch = new EntityPatch(sets, List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals("EN_DESCANSO", pasture.getStatus());
    }

    @Test
    void applyLocal_setSubstatus_updatesSubstatus() {
        // Arrange
        Map<String, Object> sets = new HashMap<>();
        sets.put("substatus", PastureSubstatus.ESTABLECIMIENTO);
        EntityPatch patch = new EntityPatch(sets, List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals("ESTABLECIMIENTO", pasture.getSubstatus());
    }

    @Test
    void applyLocal_setHoldUntil_updatesHoldUntil() {
        // Arrange
        Map<String, Object> sets = new HashMap<>();
        sets.put("holdUntilIso", "2026-02-15");
        EntityPatch patch = new EntityPatch(sets, List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals("2026-02-15", pasture.getHoldUntil());
    }

    @Test
    void applyLocal_setLastUseAt_updatesLastUseAt() {
        // Arrange
        Map<String, Object> sets = new HashMap<>();
        sets.put("lastUseAtIso", "2026-01-20");
        EntityPatch patch = new EntityPatch(sets, List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals("2026-01-20", pasture.getLastUseAt());
    }

    @Test
    void applyLocal_setGsi2Fields_updatesGsi2() {
        // Arrange
        Map<String, Object> sets = new HashMap<>();
        sets.put("gsi2pk", "farm#F001#blocked#true");
        sets.put("gsi2sk", 1);
        EntityPatch patch = new EntityPatch(sets, List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals("farm#F001#blocked#true", pasture.getGsi2pk());
        assertEquals(1, pasture.getGsi2sk());
    }

    @Test
    void applyLocal_setMultipleFields_updatesAll() {
        // Arrange
        Map<String, Object> sets = new HashMap<>();
        sets.put("status", "MANTENIMIENTO");
        sets.put("substatus", "FERTILIZACION");
        sets.put("holdUntilIso", "2026-02-01");
        EntityPatch patch = new EntityPatch(sets, List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals("MANTENIMIENTO", pasture.getStatus());
        assertEquals("FERTILIZACION", pasture.getSubstatus());
        assertEquals("2026-02-01", pasture.getHoldUntil());
    }

    // ==================== applyLocal - REMOVE Operations ====================

    @Test
    void applyLocal_removeHoldUntil_setsToNull() {
        // Arrange
        pasture.setHoldUntil("2026-02-15");
        EntityPatch patch = new EntityPatch(Map.of(), List.of("holdUntilIso"));

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertNull(pasture.getHoldUntil());
    }

    @Test
    void applyLocal_removeGsi2Fields_setsToNull() {
        // Arrange
        pasture.setGsi2pk("farm#F001#blocked#true");
        pasture.setGsi2sk(1);
        EntityPatch patch = new EntityPatch(Map.of(), List.of("gsi2pk", "gsi2sk"));

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertNull(pasture.getGsi2pk());
        assertNull(pasture.getGsi2sk());
    }

    // ==================== applyLocal - Edge Cases ====================

    @Test
    void applyLocal_nullPatch_doesNothing() {
        // Arrange
        String originalStatus = pasture.getStatus();

        // Act
        PatchApplier.applyLocal(pasture, null);

        // Assert
        assertEquals(originalStatus, pasture.getStatus());
    }

    @Test
    void applyLocal_emptyPatch_doesNothing() {
        // Arrange
        String originalStatus = pasture.getStatus();
        EntityPatch patch = new EntityPatch(Map.of(), List.of());

        // Act
        PatchApplier.applyLocal(pasture, patch);

        // Assert
        assertEquals(originalStatus, pasture.getStatus());
    }

    @Test
    void applyLocal_nullPasture_doesNotThrowException() {
        // Arrange
        EntityPatch patch = new EntityPatch(Map.of("status", "EN_USO"), List.of());

        // Act & Assert
        assertDoesNotThrow(() -> PatchApplier.applyLocal(null, patch));
    }
}
