package com.cattle.services;

import com.cattle.enums.profiles.AlertType;
import com.cattle.enums.profiles.ProductiveState;
import com.cattle.enums.profiles.ReproductiveState;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para calcular estados productivos y alertas de bovinos.
 * 
 * Responsabilidades:
 * - Calcular ReproductiveState basado en datos de preñez
 * - Calcular ProductiveState combinando preñez + lactancia
 * - Generar alertas basadas en fechas y umbrales configurables
 * 
 * Principios:
 * - Lógica de cálculo centralizada (Single Responsibility)
 * - Umbrales configurables para flexibilidad
 * - Métodos puros sin efectos secundarios
 */
@Service
public class ProductiveStateCalculator {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    
    // Umbrales configurables (en días)
    private static final int PREPARTUM_THRESHOLD_DAYS = 21;
    private static final int POSTPARTUM_THRESHOLD_DAYS = 30;
    private static final int POSTPARTUM_ALERT_DAYS = 15;
    private static final int DRY_OFF_THRESHOLD_DAYS = 60;
    private static final int HEAT_WATCH_THRESHOLD_DAYS = 45;

    /**
     * Resultado del cálculo de estados productivos.
     * Inmutable para garantizar thread-safety.
     */
    public record ProductiveStateResult(
        ReproductiveState reproductiveState,
        ProductiveState productiveState,
        List<AlertType> alerts,
        Integer daysUntilDue,
        Integer daysInLactation,
        Integer daysSinceCalving
    ) {
        public List<String> alertsAsStrings() {
            return alerts.stream().map(AlertType::name).toList();
        }
    }

    /**
     * Calcula el estado reproductivo basado en datos de preñez.
     *
     * @param isPregnant Si hay preñez activa
     * @param pregnancyStatus Estado de la preñez (ACTIVE, CLOSED)
     * @param expectedDueDate Fecha esperada de parto (ISO format)
     * @param calvingDate Fecha del último parto (ISO format)
     * @param today Fecha actual para cálculos
     * @return Estado reproductivo calculado
     */
    public ReproductiveState calculateReproductiveState(
            Boolean isPregnant,
            String pregnancyStatus,
            String expectedDueDate,
            String calvingDate,
            LocalDate today) {
        
        // Sin preñez activa
        if (isPregnant == null || !isPregnant || !"ACTIVE".equalsIgnoreCase(pregnancyStatus)) {
            // Verificar si es postparto reciente
            if (calvingDate != null && !calvingDate.isEmpty()) {
                long daysSinceCalving = daysBetween(calvingDate, today);
                if (daysSinceCalving >= 0 && daysSinceCalving <= POSTPARTUM_THRESHOLD_DAYS) {
                    return ReproductiveState.POST_PARTO;
                }
            }
            return ReproductiveState.OPEN;
        }
        
        // Preñez activa - calcular proximidad al parto
        if (expectedDueDate != null && !expectedDueDate.isEmpty()) {
            long daysUntilDue = daysUntil(expectedDueDate, today);
            if (daysUntilDue <= PREPARTUM_THRESHOLD_DAYS) {
                return ReproductiveState.PRE_PARTO;
            }
        }
        
        return ReproductiveState.PREGNANT;
    }

    /**
     * Calcula el estado productivo combinando preñez y lactancia.
     *
     * @param reproductiveState Estado reproductivo previamente calculado
     * @param lactationStatus Estado de lactancia (LACTATING, DRY, CLOSED)
     * @return Estado productivo combinado
     */
    public ProductiveState calculateProductiveState(
            ReproductiveState reproductiveState,
            String lactationStatus) {
        
        boolean isLactating = "LACTATING".equalsIgnoreCase(lactationStatus);
        boolean isDry = "DRY".equalsIgnoreCase(lactationStatus);
        
        return switch (reproductiveState) {
            case PRE_PARTO -> ProductiveState.PRE_PARTO;
            case POST_PARTO -> ProductiveState.POST_PARTO;
            case PREGNANT -> {
                if (isLactating) yield ProductiveState.PREGNANT_LACTATING;
                if (isDry) yield ProductiveState.PREGNANT_DRY;
                yield ProductiveState.PREGNANT;
            }
            case OPEN -> {
                if (isLactating) yield ProductiveState.OPEN_LACTATING;
                if (isDry) yield ProductiveState.DRY;
                yield ProductiveState.OPEN;
            }
        };
    }

    /**
     * Genera lista de alertas activas basadas en fechas y estados.
     *
     * @param isPregnant Si hay preñez activa
     * @param expectedDueDate Fecha esperada de parto
     * @param calvingDate Fecha del último parto
     * @param lactationStatus Estado de lactancia
     * @param reproductiveState Estado reproductivo
     * @param today Fecha actual
     * @return Lista de alertas activas
     */
    public List<AlertType> calculateAlerts(
            Boolean isPregnant,
            String expectedDueDate,
            String calvingDate,
            String lactationStatus,
            ReproductiveState reproductiveState,
            LocalDate today) {
        
        List<AlertType> alerts = new ArrayList<>();
        
        // OVERDUE - Parto atrasado
        if (Boolean.TRUE.equals(isPregnant) && expectedDueDate != null) {
            long daysUntilDue = daysUntil(expectedDueDate, today);
            if (daysUntilDue < 0) {
                alerts.add(AlertType.OVERDUE);
            } else if (daysUntilDue <= PREPARTUM_THRESHOLD_DAYS) {
                alerts.add(AlertType.PREPARTUM);
            }
        }
        
        // DRY_OFF_SOON - Debe secarse
        if ("LACTATING".equalsIgnoreCase(lactationStatus) && 
            Boolean.TRUE.equals(isPregnant) && 
            expectedDueDate != null) {
            long daysUntilDue = daysUntil(expectedDueDate, today);
            if (daysUntilDue > 0 && daysUntilDue <= DRY_OFF_THRESHOLD_DAYS) {
                alerts.add(AlertType.DRY_OFF_SOON);
            }
        }
        
        // POSTPARTUM - Periodo postparto
        if (calvingDate != null && !calvingDate.isEmpty()) {
            long daysSinceCalving = daysBetween(calvingDate, today);
            if (daysSinceCalving >= 0 && daysSinceCalving <= POSTPARTUM_ALERT_DAYS) {
                alerts.add(AlertType.POSTPARTUM);
            }
        }
        
        // HEAT_WATCH - Vigilar celo
        if (reproductiveState == ReproductiveState.OPEN && calvingDate != null) {
            long daysSinceCalving = daysBetween(calvingDate, today);
            if (daysSinceCalving >= HEAT_WATCH_THRESHOLD_DAYS) {
                alerts.add(AlertType.HEAT_WATCH);
            }
        }
        
        return alerts;
    }

    /**
     * Calcula todos los estados y alertas en una sola operación.
     * Método principal para usar desde BovineSummaryService.
     */
    public ProductiveStateResult calculate(
            Boolean isPregnant,
            String pregnancyStatus,
            String expectedDueDate,
            String calvingDate,
            String lactationStatus,
            String lactationStartDate,
            LocalDate today) {
        
        // Determinar la fecha del último parto:
        // Preferir lactationStartDate (inicio de lactancia = fecha del parto)
        // ya que cuando hay nueva preñez, el calvingDate puede ser de una preñez diferente
        String effectiveCalvingDate = (lactationStartDate != null && !lactationStartDate.isEmpty()) 
            ? lactationStartDate 
            : calvingDate;
        
        // Calcular estados
        ReproductiveState reproductiveState = calculateReproductiveState(
            isPregnant, pregnancyStatus, expectedDueDate, effectiveCalvingDate, today);
        
        ProductiveState productiveState = calculateProductiveState(
            reproductiveState, lactationStatus);
        
        // Calcular alertas
        List<AlertType> alerts = calculateAlerts(
            isPregnant, expectedDueDate, effectiveCalvingDate, 
            lactationStatus, reproductiveState, today);
        
        // Calcular días
        Integer daysUntilDue = null;
        if (expectedDueDate != null && !expectedDueDate.isEmpty() && Boolean.TRUE.equals(isPregnant)) {
            daysUntilDue = (int) daysUntil(expectedDueDate, today);
        }
        
        Integer daysInLactation = null;
        if (lactationStartDate != null && !lactationStartDate.isEmpty() && 
            "LACTATING".equalsIgnoreCase(lactationStatus)) {
            daysInLactation = (int) daysBetween(lactationStartDate, today);
        }
        
        // daysSinceCalving: usar effectiveCalvingDate (ya prioriza lactationStartDate sobre calvingDate)
        Integer daysSinceCalving = null;
        if (effectiveCalvingDate != null && !effectiveCalvingDate.isEmpty()) {
            daysSinceCalving = (int) daysBetween(effectiveCalvingDate, today);
        }
        
        return new ProductiveStateResult(
            reproductiveState,
            productiveState,
            alerts,
            daysUntilDue,
            daysInLactation,
            daysSinceCalving
        );
    }

    // ========== Utilidades de fecha ==========

    private long daysUntil(String dateString, LocalDate today) {
        try {
            LocalDate target = LocalDate.parse(dateString, ISO_DATE);
            return ChronoUnit.DAYS.between(today, target);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private long daysBetween(String fromDateString, LocalDate today) {
        try {
            LocalDate from = LocalDate.parse(fromDateString, ISO_DATE);
            return ChronoUnit.DAYS.between(from, today);
        } catch (Exception e) {
            return -1;
        }
    }
}
