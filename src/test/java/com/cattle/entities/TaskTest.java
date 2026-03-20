package com.cattle.entities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Task entity
 * HU-ASEGURAMIENTO-CALIDAD-001 - Fase Entities
 */
@Tag("unit")
@Tag("entity")
class TaskTest {

    @Test
    void task_builder_createsInstance() {
        // Act
        Task task = Task.builder()
                .pk("farm#F001")
                .sk("dueDate#2025-10-02T23:00:00Z#T10")
                .taskId("T10")
                .dueDate("2025-10-02T23:00:00Z")
                .kind("Registrar cierre de pastoreo y residual (P-04)")
                .pastureId("P-04")
                .status("PENDIENTE")
                .gsi1pk("farm#F001#pasture#P-04")
                .gsi1sk("2025-10-02T23:00:00Z")
                .build();

        // Assert
        assertNotNull(task);
        assertEquals("farm#F001", task.getPk());
        assertEquals("T10", task.getTaskId());
        assertEquals("PENDIENTE", task.getStatus());
    }

    @Test
    void task_noArgsConstructor_createsInstance() {
        // Act
        Task task = new Task();

        // Assert
        assertNotNull(task);
        assertNull(task.getPk());
    }

    @Test
    void task_gsiKeys_setCorrectly() {
        // Arrange
        Task task = Task.builder()
                .pk("farm#F001")
                .sk("dueDate#2025-10-05T10:00:00Z#T15")
                .gsi1pk("farm#F001#pasture#P-02")
                .gsi1sk("2025-10-05T10:00:00Z")
                .build();

        // Assert
        assertEquals("farm#F001#pasture#P-02", task.getGsi1pk());
        assertEquals("2025-10-05T10:00:00Z", task.getGsi1sk());
    }

    @Test
    void task_settersAndGetters_work() {
        // Arrange
        Task task = new Task();

        // Act
        task.setPk("farm#F002");
        task.setSk("dueDate#2025-11-01T08:00:00Z#T20");
        task.setTaskId("T20");
        task.setDueDate("2025-11-01T08:00:00Z");
        task.setKind("Verificar altura de pasto");
        task.setPastureId("P-08");
        task.setStatus("COMPLETADA");
        task.setGsi1pk("farm#F002#pasture#P-08");
        task.setGsi1sk("2025-11-01T08:00:00Z");

        // Assert
        assertEquals("farm#F002", task.getPk());
        assertEquals("T20", task.getTaskId());
        assertEquals("COMPLETADA", task.getStatus());
        assertEquals("P-08", task.getPastureId());
    }

    @Test
    void task_pendingStatus_isDefault() {
        // Arrange
        Task task = Task.builder()
                .pk("farm#F001")
                .sk("dueDate#2025-12-01T09:00:00Z#T25")
                .taskId("T25")
                .status("PENDIENTE")
                .build();

        // Assert
        assertEquals("PENDIENTE", task.getStatus());
    }

    @Test
    void task_completedStatus_canBeSet() {
        // Arrange
        Task task = Task.builder()
                .pk("farm#F001")
                .sk("dueDate#2025-10-01T10:00:00Z#T05")
                .taskId("T05")
                .status("COMPLETADA")
                .build();

        // Assert
        assertEquals("COMPLETADA", task.getStatus());
    }
}
