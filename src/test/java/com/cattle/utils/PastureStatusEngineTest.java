package com.cattle.utils;

import com.cattle.builders.PastureBuilder;
import com.cattle.builders.PlanBuilder;
import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import com.cattle.enums.PastureStatus;
import com.cattle.enums.PastureSubstatus;
import com.cattle.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class PastureStatusEngineTest {

    private PastureStatusEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PastureStatusEngine();
    }

    // Test para evento OPEN
    @Test
    void applyEvent_open_potreroNoBloqueado() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_DESCANSO.name());
        pasture.setSubstatus(PastureSubstatus.NINGUNO.name());
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();
        PastureEvent ev = new OpenEvent("Roberth", "lot1", 5);

        EntityPatch patch = engine.applyEvent(pasture, plan, ev);

        assertEquals(PastureStatus.EN_USO.name(), patch.set().get("status"));
    }

    // Test para evento OPEN con potrero bloqueado
    @Test
    void applyEvent_open_potreroBloqueado_lanzaExcepcion() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_DESCANSO.name());
        pasture.setSubstatus(PastureSubstatus.FERTILIZACION.name());
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();
        PastureEvent ev = new OpenEvent("Roberth", "lot1", 5);

        assertThrows(IllegalStateException.class, ()
                -> engine.applyEvent(pasture, plan, ev));
    }

    // Test para evento CLOSE
    @Test
    void applyEvent_close() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_USO.name());
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();
        PastureEvent ev = new CloseEvent("Roberth", "lot1", 5, 5);

        EntityPatch patch = engine.applyEvent(pasture, plan, ev);

        assertEquals(PastureStatus.EN_DESCANSO.name(), patch.set().get("status"));
        assertNotNull(patch.set().get("lastUseAtIso"));
    }

    // Test para evento MAINTENANCE_SET
    @Test
    void applyEvent_maintenanceSet() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_DESCANSO.name());
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();
        PastureEvent ev = new MaintenanceSetEvent("Roberth", PastureSubstatus.FERTILIZACION, "2025-12-31");

        EntityPatch patch = engine.applyEvent(pasture, plan, ev);

        assertEquals(PastureStatus.MANTENIMIENTO.name(), patch.set().get("status"));
        assertEquals(PastureSubstatus.FERTILIZACION.name(), patch.set().get("substatus"));
        assertEquals("2025-12-31", patch.set().get("holdUntil"));
        assertEquals("farm#" + pasture.getFarmId() + "#blocked#true", patch.set().get("gsi2pk"));
        assertNotNull(patch.set().get("gsi2sk"));
    }

    // Test para evento MAINTENANCE_CLEAR
    @Test
    void applyEvent_maintenanceClear() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.MANTENIMIENTO.name());
        pasture.setSubstatus(PastureSubstatus.FERTILIZACION.name());
        pasture.setHoldUntil("2024-12-31");
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();
        PastureEvent ev = new MaintenanceClearEvent("Roberth");

        EntityPatch patch = engine.applyEvent(pasture, plan, ev);

        assertEquals(PastureSubstatus.NINGUNO.name(), patch.set().get("substatus"));
        assertEquals(
                (EtaCalculator.etaOpenDays(pasture, plan) <= 0 ? PastureStatus.DISPONIBLE.name() : PastureStatus.EN_DESCANSO.name()),
                patch.set().get("status")
        );
        assertFalse(patch.set().containsKey("gsi2pk"));
        assertFalse(patch.set().containsKey("gsi2sk"));
    }

    @Test
    void autoUpdateStatusTickByHoldUntil_mantenimientoVencido() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        // Fecha fija en el pasado (no caducará)
        pasture.setHoldUntil("2020-01-01");
        pasture.setFarmId("F001");
        pasture.setStatus(PastureStatus.MANTENIMIENTO.name());
        pasture.setSubstatus(PastureSubstatus.FERTILIZACION.name());
        pasture.setCurrentHeightCm(5);
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();

        EntityPatch patch = engine.autoUpdateStatusTickByHoldUntil(pasture, plan);

        // Con altura de 5cm, el potrero está listo y pasa a DISPONIBLE
        assertEquals(PastureStatus.DISPONIBLE.name(), patch.set().get("status"));
        assertEquals(PastureSubstatus.NINGUNO.name(), patch.set().get("substatus"));
        assertEquals("farm#F001#blocked#false", patch.set().get("gsi2pk"));
        assertNotNull(patch.set().get("status"));
    }

    @Test
    void autoUpdateStatusTickByHoldUntil_mantenimientoVencido_AndEtaMenorCero() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        // Fecha fija en el pasado (no caducará)
        pasture.setHoldUntil("2020-01-01");
        pasture.setFarmId("F001");
        pasture.setStatus(PastureStatus.MANTENIMIENTO.name());
        pasture.setSubstatus(PastureSubstatus.FERTILIZACION.name());
        pasture.setCurrentHeightCm(20);
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();

        EntityPatch patch = engine.autoUpdateStatusTickByHoldUntil(pasture, plan);

        assertEquals(PastureStatus.DISPONIBLE.name(), patch.set().get("status"));
        assertEquals(PastureSubstatus.NINGUNO.name(), patch.set().get("substatus"));
        assertEquals("farm#F001#blocked#false", patch.set().get("gsi2pk"));
        assertNotNull(patch.set().get("status"));
    }

    @Test
    void autoUpdateStatusTickByHoldUntil_noActualizaSiNoMantenimiento() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_USO.name());
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();

        EntityPatch patch = engine.autoUpdateStatusTickByHoldUntil(pasture, plan);

        assertTrue(patch.isEmpty());
    }

    @Test
    void isBlockedEffective_bloqueadoPorSubstatus() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setSubstatus(PastureSubstatus.FERTILIZACION.name());
        pasture.setHoldUntil(null);

        boolean result = engine.isBlockedEffective(pasture);

        assertTrue(result);
    }

    @Test
    void isBlockedEffective_bloqueadoPorHoldUntil() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setSubstatus(PastureSubstatus.NINGUNO.name());
        pasture.setHoldUntil(LocalDate.now().plusDays(5).toString());

        boolean result = engine.isBlockedEffective(pasture);

        assertTrue(result);
    }

    @Test
    void isBlockedEffective_bloqueadoPorHoldUntilFuturo() {
        String futureDate = LocalDate.now().plusDays(2).toString();
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setSubstatus(PastureSubstatus.NINGUNO.name());
        pasture.setHoldUntil(futureDate);

        boolean result = engine.isBlockedEffective(pasture);

        assertTrue(result);
    }

    @Test
    void isBlockedEffective_noBloqueado() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setSubstatus(PastureSubstatus.NINGUNO.name());
        pasture.setHoldUntil(LocalDate.now().minusDays(2).toString());

        boolean result = engine.isBlockedEffective(pasture);

        assertFalse(result);
    }

    @Test
    void availableAt_etaMenorQueCero_sinHoldUntil() {
        Instant now = Instant.now();

        Instant result = engine.availableAt(-2, null);

        assertTrue(!result.isBefore(now));
    }

    @Test
    void availableAt_etaPositivo_sinHoldUntil() {
        Instant now = Instant.now();
        int eta = 3;

        Instant result = engine.availableAt(eta, null);

        Instant esperado = now.plus(Duration.ofDays(eta));
        assertTrue(Math.abs(result.getEpochSecond() - esperado.getEpochSecond()) < 2);
    }

    @Test
    void availableAt_etaPositivo_conHoldUntilFuturo() {
        int eta = 2;
        String holdUntil = LocalDate.now().plusDays(5).toString();

        Instant result = engine.availableAt(eta, holdUntil);

        Instant esperado = LocalDate.parse(holdUntil).atStartOfDay(ZoneOffset.UTC).toInstant();
        assertEquals(esperado, result);
    }

    @Test
    void availableAt_etaPositivo_conHoldUntilPasado() {
        int eta = 2;
        String holdUntil = LocalDate.now().minusDays(1).toString();
        Instant now = Instant.now();
        Instant esperado = now.plus(Duration.ofDays(eta));

        Instant result = engine.availableAt(eta, holdUntil);

        assertTrue(Math.abs(result.getEpochSecond() - esperado.getEpochSecond()) < 2);
    }

    @Test
    void availableAt_holdUntilInvalido() {
        int eta = 1;
        String holdUntil = "fecha-invalida";
        Instant now = Instant.now();
        Instant esperado = now.plus(Duration.ofDays(eta));

        Instant result = engine.availableAt(eta, holdUntil);

        assertTrue(Math.abs(result.getEpochSecond() - esperado.getEpochSecond()) < 2);
    }

    @Test
    void deriveEffectiveStatus_enUso() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_USO.name());

        PastureStatus result = engine.deriveEffectiveStatus(pasture, 5);

        assertEquals(PastureStatus.EN_USO, result);
    }

    @Test
    void deriveEffectiveStatus_bloqueado() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_DESCANSO.name());
        pasture.setSubstatus(PastureSubstatus.FERTILIZACION.name());

        PastureStatus result = engine.deriveEffectiveStatus(pasture, 5);

        assertEquals(PastureStatus.MANTENIMIENTO, result);
    }

    @Test
    void deriveEffectiveStatus_disponible() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_DESCANSO.name());
        pasture.setSubstatus(PastureSubstatus.NINGUNO.name());
        pasture.setHoldUntil(null);

        PastureStatus result = engine.deriveEffectiveStatus(pasture, 0);

        assertEquals(PastureStatus.DISPONIBLE, result);
    }

    @Test
    void deriveEffectiveStatus_enDescanso() {
        Pasture pasture = PastureBuilder.buildPastureRyegrassFromJson();
        pasture.setStatus(PastureStatus.EN_DESCANSO.name());
        pasture.setSubstatus(PastureSubstatus.NINGUNO.name());
        pasture.setHoldUntil(null);

        PastureStatus result = engine.deriveEffectiveStatus(pasture, 3);

        assertEquals(PastureStatus.EN_DESCANSO, result);
    }
}