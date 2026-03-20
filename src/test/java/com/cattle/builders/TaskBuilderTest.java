package com.cattle.builders;

import com.cattle.entities.Task;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TaskBuilder
 * Fase 9 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: Builders 22% → 80%
 * Tests: 8
 */
@Tag("unit")
@Tag("builder")
class TaskBuilderTest {

    @Test
    void build_withRequiredFields_createsTask() {
        // Arrange & Act
        Task result = TaskBuilder.create()
                .pk("farm#F001#date#2026-01-25")
                .sk("task#T-001")
                .taskId("T-001")
                .dueDate("2026-01-25")
                .kind("MEASUREMENT")
                .pastureId("P-01")
                .build();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#date#2026-01-25", result.getPk());
        assertEquals("task#T-001", result.getSk());
        assertEquals("T-001", result.getTaskId());
        assertEquals("2026-01-25", result.getDueDate());
        assertEquals("MEASUREMENT", result.getKind());
        assertEquals("P-01", result.getPastureId());
    }

    @Test
    void build_withAllFields_createsCompleteTask() {
        // Arrange & Act
        Task result = TaskBuilder.create()
                .pk("farm#F001#date#2026-01-25")
                .sk("task#T-001")
                .gsi1pk("farm#F001#pasture#P-01")
                .gsi1sk("2026-01-25")
                .taskId("T-001")
                .dueDate("2026-01-25")
                .kind("FERTILIZATION")
                .pastureId("P-01")
                .status("PENDIENTE")
                .build();

        // Assert
        assertNotNull(result);
        assertEquals("farm#F001#pasture#P-01", result.getGsi1pk());
        assertEquals("2026-01-25", result.getGsi1sk());
        assertEquals("PENDIENTE", result.getStatus());
    }

    @Test
    void build_withoutGsi1pk_generatesDefault() {
        // Arrange & Act
        Task result = TaskBuilder.create()
                .pk("farm#F001#date#2026-01-25")
                .sk("task#T-001")
                .taskId("T-001")
                .dueDate("2026-01-25")
                .kind("MEASUREMENT")
                .pastureId("P-05")
                .build();

        // Assert
        assertEquals("farm#UNKNOWN#pasture#P-05", result.getGsi1pk());
    }

    @Test
    void build_withoutGsi1sk_usesDefaultFromDueDate() {
        // Arrange & Act
        Task result = TaskBuilder.create()
                .pk("farm#F001#date#2026-01-25")
                .sk("task#T-001")
                .taskId("T-001")
                .dueDate("2026-01-25")
                .kind("MEASUREMENT")
                .pastureId("P-01")
                .build();

        // Assert
        assertEquals("2026-01-25", result.getGsi1sk());
    }

    @Test
    void defaults_setsStatusToPendiente() {
        // Arrange & Act
        Task result = TaskBuilder.create()
                .pk("farm#F001#date#2026-01-25")
                .sk("task#T-001")
                .taskId("T-001")
                .dueDate("2026-01-25")
                .kind("MEASUREMENT")
                .pastureId("P-01")
                .defaults()
                .build();

        // Assert
        assertEquals("PENDIENTE", result.getStatus());
    }

    @Test
    void defaults_doesNotOverrideExistingStatus() {
        // Arrange & Act
        Task result = TaskBuilder.create()
                .pk("farm#F001#date#2026-01-25")
                .sk("task#T-001")
                .taskId("T-001")
                .dueDate("2026-01-25")
                .kind("MEASUREMENT")
                .pastureId("P-01")
                .status("COMPLETADA")
                .defaults()
                .build();

        // Assert
        assertEquals("COMPLETADA", result.getStatus());
    }

    @Test
    void build_withoutPk_throwsException() {
        // Arrange
        TaskBuilder builder = TaskBuilder.create()
                .sk("task#T-001")
                .taskId("T-001")
                .dueDate("2026-01-25")
                .kind("MEASUREMENT")
                .pastureId("P-01");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(exception.getMessage().contains("pk"));
    }

    @Test
    void build_withoutTaskId_throwsException() {
        // Arrange
        TaskBuilder builder = TaskBuilder.create()
                .pk("farm#F001#date#2026-01-25")
                .sk("task#T-001")
                .dueDate("2026-01-25")
                .kind("MEASUREMENT")
                .pastureId("P-01");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(exception.getMessage().contains("taskId"));
    }
}
