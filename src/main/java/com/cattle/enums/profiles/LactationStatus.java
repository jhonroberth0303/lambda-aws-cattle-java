package com.cattle.enums.profiles;

/**
 * Estados del ciclo de lactancia de un bovino.
 * 
 * LACTATING - Vaca produciendo leche activamente
 * DRY       - Periodo de secado (preparación para próximo parto)
 * CLOSED    - Lactancia finalizada/cerrada
 */
public enum LactationStatus {
    LACTATING,
    DRY,
    CLOSED
}
