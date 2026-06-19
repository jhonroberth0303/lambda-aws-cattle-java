package com.cattle.events;

import com.cattle.enums.BovineEventType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class BovineEventTypeTest {

    @Test
    void values_containsAllExpectedTypes() {
        assertEquals(22, BovineEventType.values().length);
    }

    @Test
    void valueOf_resolvesAllDefinedTypes() {
        for (BovineEventType type : BovineEventType.values()) {
            assertDoesNotThrow(() -> BovineEventType.valueOf(type.name()),
                    "No se pudo resolver el tipo: " + type.name());
        }
    }

    @Test
    void valueOf_throwsForUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> BovineEventType.valueOf("BANO_GARRAPATICIDA"));
        assertThrows(IllegalArgumentException.class, () -> BovineEventType.valueOf("SERVICIO"));
        assertThrows(IllegalArgumentException.class, () -> BovineEventType.valueOf("UNKNOWN_TYPE"));
    }

    @Test
    void valueOf_resolvesSanidadTypes() {
        assertEquals(BovineEventType.VACUNACION, BovineEventType.valueOf("VACUNACION"));
        assertEquals(BovineEventType.TRATAMIENTO, BovineEventType.valueOf("TRATAMIENTO"));
        assertEquals(BovineEventType.BANO_GARRAPATAS, BovineEventType.valueOf("BANO_GARRAPATAS"));
        assertEquals(BovineEventType.DESPARASITACION, BovineEventType.valueOf("DESPARASITACION"));
    }

    @Test
    void valueOf_resolvesReproduccionTypes() {
        assertEquals(BovineEventType.MONTA, BovineEventType.valueOf("MONTA"));
        assertEquals(BovineEventType.INSEMINACION, BovineEventType.valueOf("INSEMINACION"));
        assertEquals(BovineEventType.DIAGNOSTICO_PRENEZ, BovineEventType.valueOf("DIAGNOSTICO_PRENEZ"));
        assertEquals(BovineEventType.PARTO, BovineEventType.valueOf("PARTO"));
        assertEquals(BovineEventType.ABORTO, BovineEventType.valueOf("ABORTO"));
        assertEquals(BovineEventType.DESTETE, BovineEventType.valueOf("DESTETE"));
    }

    @Test
    void valueOf_resolvesSalidaTypes() {
        assertEquals(BovineEventType.VENTA, BovineEventType.valueOf("VENTA"));
        assertEquals(BovineEventType.MUERTE, BovineEventType.valueOf("MUERTE"));
    }
}
