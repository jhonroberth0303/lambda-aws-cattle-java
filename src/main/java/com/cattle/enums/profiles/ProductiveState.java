package com.cattle.enums.profiles;

/**
 * Estado productivo combinado de una vaca adulta.
 * Combina el estado reproductivo con el estado de lactancia.
 * 
 * Este estado es CALCULADO y representa la situación operativa
 * más relevante para el manejo diario del hato.
 */
public enum ProductiveState {
    /**
     * Sin preñez activa, sin lactancia.
     * Vaca "vacía" lista para nuevo ciclo.
     */
    OPEN,
    
    /**
     * Sin preñez, lactando activamente.
     * Típico en vacas recién paridas aún no servidas.
     */
    OPEN_LACTATING,
    
    /**
     * Preñada sin lactancia activa.
     * Novillas preñadas o vacas que no lactaron.
     */
    PREGNANT,
    
    /**
     * Preñada y lactando simultáneamente.
     * Situación común en los primeros 7 meses de preñez.
     */
    PREGNANT_LACTATING,
    
    /**
     * Preñada en periodo de secado.
     * Preparación para el próximo parto (~60 días antes).
     */
    PREGNANT_DRY,
    
    /**
     * Próxima al parto (≤21 días).
     * Requiere vigilancia especial.
     */
    PRE_PARTO,
    
    /**
     * Recién parida (≤30 días postparto).
     * Periodo de recuperación y establecimiento de lactancia.
     */
    POST_PARTO,
    
    /**
     * Seca sin preñez activa.
     * Estado anómalo que requiere atención.
     */
    DRY
}
