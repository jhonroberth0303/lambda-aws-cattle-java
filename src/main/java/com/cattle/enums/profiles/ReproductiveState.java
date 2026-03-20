package com.cattle.enums.profiles;

/**
 * Estado reproductivo de una vaca adulta.
 * Representa la fase actual en el ciclo reproductivo.
 * 
 * Este estado es CALCULADO basándose en:
 * - ProfilePregnancy.status (ACTIVE/CLOSED)
 * - ProfilePregnancy.expectedDueDate
 * - ProfilePregnancy.calvingDate
 */
public enum ReproductiveState {
    /**
     * Sin preñez activa, lista para servir/inseminar.
     * Condición: No hay preñez activa O última preñez cerrada hace > 30 días
     */
    OPEN,
    
    /**
     * Preñez confirmada, en periodo de gestación normal.
     * Condición: Preñez ACTIVE con más de 21 días para el parto
     */
    PREGNANT,
    
    /**
     * Preparación para el parto (periodo preparto).
     * Condición: Preñez ACTIVE con 21 días o menos para el parto
     */
    PRE_PARTO,
    
    /**
     * Periodo postparto (recuperación y primeros días con cría).
     * Condición: Última preñez cerrada con calvingDate hace 30 días o menos
     */
    POST_PARTO
}
