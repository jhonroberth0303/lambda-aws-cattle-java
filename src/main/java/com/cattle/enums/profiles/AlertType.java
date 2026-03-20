package com.cattle.enums.profiles;

/**
 * Tipos de alertas para el manejo de bovinos.
 * Las alertas son temporales y calculadas basándose en fechas y estados.
 */
public enum AlertType {
    /**
     * Alerta de parto inminente.
     * Condición: Faltan ≤21 días para la fecha esperada de parto.
     * Urgencia: ALTA
     */
    PREPARTUM,
    
    /**
     * Periodo postparto activo.
     * Condición: Han pasado ≤15 días desde el parto.
     * Urgencia: MEDIA
     */
    POSTPARTUM,
    
    /**
     * Debe iniciarse el secado pronto.
     * Condición: Lactando + faltan ≤60 días para el parto.
     * Urgencia: MEDIA
     */
    DRY_OFF_SOON,
    
    /**
     * Parto atrasado.
     * Condición: La fecha esperada de parto ya pasó.
     * Urgencia: CRÍTICA
     */
    OVERDUE,
    
    /**
     * Vigilar posible celo.
     * Condición: OPEN + días postparto ≥45 días.
     * Urgencia: BAJA
     */
    HEAT_WATCH
}
