package com.cattle.enums.profiles;

/**
 * Universal life stage based on age.
 * 100% derivable from bornDate using configured age thresholds.
 */
public enum LifeStage {
    NEWBORN, // Recién nacido
    CALF,    // Ternero
    WEANED,  // Destetado
    GROWER,  // Crecimiento
    ADULT    // Adulto
}
