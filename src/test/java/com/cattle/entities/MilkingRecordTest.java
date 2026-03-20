package com.cattle.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para FarmMilking entity
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Entities
 */
@Tag("unit")
@Tag("entity")
class MilkingRecordTest {

    @Test
    void farmMilking_builder_createsInstance() {
        // Act
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#123")
                .SK("LACTANCIA#2026-01-20#AM")
                .bovineId(123)
                .date("2026-01-20")
                .shift("AM")
                .liters(15.5)
                .status("completo")
                .observations("Normal")
                .recordedBy("user-001")
                .createdAt("2026-01-20T08:00:00Z")
                .build();

        // Assert
        assertNotNull(milkingRecord);
        assertEquals("BOVINE#123", milkingRecord.getPK());
        assertEquals(123, milkingRecord.getBovineId());
        assertEquals(15.5, milkingRecord.getLiters());
    }

    @Test
    void farmMilking_noArgsConstructor_createsInstance() {
        // Act
        MilkingRecord milkingRecord = new MilkingRecord();

        // Assert
        assertNotNull(milkingRecord);
        assertNull(milkingRecord.getPK());
    }

    @Test
    void farmMilking_settersAndGetters_work() {
        // Arrange
        MilkingRecord milkingRecord = new MilkingRecord();

        // Act
        milkingRecord.setPK("BOVINE#456");
        milkingRecord.setSK("LACTANCIA#2026-01-21#PM");
        milkingRecord.setBovineId(456);
        milkingRecord.setDate("2026-01-21");
        milkingRecord.setShift("PM");
        milkingRecord.setLiters(12.0);
        milkingRecord.setStatus("parcial");
        milkingRecord.setObservations("Ordeño parcial por enfermedad");
        milkingRecord.setRecordedBy("user-002");

        // Assert
        assertEquals("BOVINE#456", milkingRecord.getPK());
        assertEquals("PM", milkingRecord.getShift());
        assertEquals(12.0, milkingRecord.getLiters());
        assertEquals("parcial", milkingRecord.getStatus());
    }

    @Test
    void farmMilking_getPK_isPartitionKey() {
        // Arrange
        MilkingRecord milkingRecord = MilkingRecord.builder()
                .PK("BOVINE#789")
                .SK("LACTANCIA#2026-01-22#AM")
                .build();

        // Assert
        assertEquals("BOVINE#789", milkingRecord.getPK());
        assertEquals("LACTANCIA#2026-01-22#AM", milkingRecord.getSK());
    }
}
