package com.cattle.entities;

import com.cattle.entities.bovines.BovineIdentityItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Bovine Entity
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Entities
 * 
 * Cobertura objetivo: entities 26% → 60%
 */
@Tag("unit")
@Tag("entities")
class BovineIdentityItemTest {

    // ==================== Builder Tests ====================

    @Test
    void builder_allFields_createsValidEntity() {
        // Arrange & Act
        BovineIdentityItem bovineIdentityItem = BovineIdentityItem.builder()
                .pk("BOVINE#123")
                .sk("IDENTITY")
                .gsi1pk("IDENTITY")
                .gsi1sk("BOVINE#123")
                .bovineId(123)
                .name("Estrella")
                .gender("female")
                .breed("Holstein")
                .bornDate("2020-05-15")
                .color("Black and White")
                .origin("born")
                .fatherId("father-001")
                .fatherNameSnapshot("Toro Mayor")
                .motherId("mother-001")
                .motherNameSnapshot("Vaca Lechera")
                .farmId("farm-001")
                .createdAt("2020-05-15T00:00:00Z")
                .updatedAt("2025-01-15T12:00:00Z")
                .build();

        // Assert
        assertEquals("BOVINE#123", bovineIdentityItem.getPk());
        assertEquals("IDENTITY", bovineIdentityItem.getSk());
        assertEquals(123, bovineIdentityItem.getBovineId());
        assertEquals("Estrella", bovineIdentityItem.getName());
        assertEquals("female", bovineIdentityItem.getGender());
        assertEquals("Holstein", bovineIdentityItem.getBreed());
        assertEquals("2020-05-15", bovineIdentityItem.getBornDate());
        assertEquals("Black and White", bovineIdentityItem.getColor());
        assertEquals("born", bovineIdentityItem.getOrigin());
        assertEquals("farm-001", bovineIdentityItem.getFarmId());
    }

    @Test
    void builder_minimalFields_createsValidEntity() {
        // Arrange & Act
        BovineIdentityItem bovineIdentityItem = BovineIdentityItem.builder()
                .pk("BOVINE#1")
                .sk("IDENTITY")
                .bovineId(1)
                .name("Test")
                .build();

        // Assert
        assertEquals("BOVINE#1", bovineIdentityItem.getPk());
        assertEquals("IDENTITY", bovineIdentityItem.getSk());
        assertEquals(1, bovineIdentityItem.getBovineId());
        assertNull(bovineIdentityItem.getGender());
        assertNull(bovineIdentityItem.getBreed());
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = new BovineIdentityItem();

        // Act
        bovineIdentityItem.setPk("BOVINE#999");
        bovineIdentityItem.setSk("IDENTITY");
        bovineIdentityItem.setGsi1pk("IDENTITY");
        bovineIdentityItem.setGsi1sk("BOVINE#999");
        bovineIdentityItem.setBovineId(999);
        bovineIdentityItem.setName("Updated Name");
        bovineIdentityItem.setGender("male");
        bovineIdentityItem.setBreed("Angus");
        bovineIdentityItem.setBornDate("2021-03-20");
        bovineIdentityItem.setColor("Brown");
        bovineIdentityItem.setOrigin("purchased");
        bovineIdentityItem.setFatherId("new-father");
        bovineIdentityItem.setFatherNameSnapshot("New Father");
        bovineIdentityItem.setMotherId("new-mother");
        bovineIdentityItem.setMotherNameSnapshot("New Mother");
        bovineIdentityItem.setFarmId("farm-002");
        bovineIdentityItem.setCreatedAt("2025-02-01T00:00:00Z");
        bovineIdentityItem.setUpdatedAt("2025-02-15T00:00:00Z");

        // Assert
        assertEquals("BOVINE#999", bovineIdentityItem.getPk());
        assertEquals("IDENTITY", bovineIdentityItem.getSk());
        assertEquals("IDENTITY", bovineIdentityItem.getGsi1pk());
        assertEquals("BOVINE#999", bovineIdentityItem.getGsi1sk());
        assertEquals(999, bovineIdentityItem.getBovineId());
        assertEquals("Updated Name", bovineIdentityItem.getName());
        assertEquals("male", bovineIdentityItem.getGender());
        assertEquals("Angus", bovineIdentityItem.getBreed());
        assertEquals("2021-03-20", bovineIdentityItem.getBornDate());
        assertEquals("Brown", bovineIdentityItem.getColor());
        assertEquals("purchased", bovineIdentityItem.getOrigin());
        assertEquals("new-father", bovineIdentityItem.getFatherId());
        assertEquals("New Father", bovineIdentityItem.getFatherNameSnapshot());
        assertEquals("new-mother", bovineIdentityItem.getMotherId());
        assertEquals("New Mother", bovineIdentityItem.getMotherNameSnapshot());
        assertEquals("farm-002", bovineIdentityItem.getFarmId());
    }

    @Test
    void noArgsConstructor_createsEmptyEntity() {
        // Act
        BovineIdentityItem bovineIdentityItem = new BovineIdentityItem();

        // Assert
        assertNull(bovineIdentityItem.getPk());
        assertNull(bovineIdentityItem.getSk());
        assertNull(bovineIdentityItem.getBovineId());
        assertNull(bovineIdentityItem.getName());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        // Act
        BovineIdentityItem bovineIdentityItem = new BovineIdentityItem(
            1, "FARM-001", "name", "female", "breed", null, "2020-01-01", "color", "born",
            "father", "fatherName", "mother", "motherName", "PURE"
        );
        bovineIdentityItem.setPk("pk");
        bovineIdentityItem.setSk("sk");
        bovineIdentityItem.setGsi1pk("gsi1pk");
        bovineIdentityItem.setGsi1sk("gsi1sk");
        bovineIdentityItem.setCreatedAt("createdAt");
        bovineIdentityItem.setUpdatedAt("updatedAt");

        // Assert
        assertEquals("pk", bovineIdentityItem.getPk());
        assertEquals("sk", bovineIdentityItem.getSk());
        assertEquals(1, bovineIdentityItem.getBovineId());
        assertEquals("name", bovineIdentityItem.getName());
        assertEquals("female", bovineIdentityItem.getGender());
        // ...otros asserts relevantes...
    }
}
