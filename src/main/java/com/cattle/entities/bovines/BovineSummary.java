package com.cattle.entities.bovines;

import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.List;

/**
 * Entidad que representa el resumen consolidado de un bovino.
 * Almacena datos de identidad, ciclo de vida, estado reproductivo,
 * preñez y lactancia para consultas eficientes en tarjetas de presentación.
 * 
 * Incluye campos CALCULADOS para evitar lógica duplicada en frontend:
 * - reproductiveState: Estado reproductivo derivado
 * - productiveState: Estado productivo combinado (preñez + lactancia)
 * - alerts: Lista de alertas activas
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class BovineSummary extends BaseDdbItem {

    // ==== IDENTITY (de Bovine) ====
    private Integer bovineId;
    private String name;
    private String gender;
    private String breed;
    private String bornDate;
    private String farmId;

    // ==== LIFECYCLE (de ProfileLifecycle) ====
    private String category;   // CALF, HEIFER, COW, BULL, etc.
    private String status;     // OPEN, SOLD, DEAD
    private String lifeStage;  // NEWBORN, CALF, WEANED, GROWER, ADULT
    private Boolean enabled;

    // ==== REPRODUCTIVE (de ProfileReproductive) ====
    private String currentLactationId;   // LACT#01 o null
    private String currentPregnancyId;   // PREG#2025-07-06 o null

    // ==== PREGNANCY SNAPSHOT ====
    private Boolean isPregnant;
    private String pregnancyStatus;      // ACTIVE, CLOSED
    private String expectedDueDate;
    private String calvingDate;          // NUEVO: fecha del último parto

    // ==== LACTATION SNAPSHOT ====
    private Boolean isLactating;
    private String lactationStatus;      // LACTATING, DRY, CLOSED
    private String lactationNumber;
    private String lactationStartDate;

    // ==== CALCULATED STATES (calculados por ProductiveStateCalculator) ====
    private String reproductiveState;    // OPEN, PREGNANT, PRE_PARTO, POST_PARTO
    private String productiveState;      // OPEN, OPEN_LACTATING, PREGNANT, PREGNANT_LACTATING, etc.
    private List<String> alerts;         // ["PREPARTUM", "DRY_OFF_SOON"]
    private Integer daysUntilDue;        // Días hasta el parto (null si no aplica)
    private Integer daysInLactation;     // Días en lactancia (null si no aplica)
    private Integer daysSinceCalving;    // Días desde el parto (null si no aplica)

}
