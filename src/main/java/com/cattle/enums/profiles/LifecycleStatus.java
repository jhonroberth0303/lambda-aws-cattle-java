package com.cattle.enums.profiles;

public enum LifecycleStatus {

    /** Animal activo en la finca */
    OPEN,

    /** Vendido */
    SOLD,

    /** Muerto */
    DEAD,

    /** Descartado por decisión productiva */
    CULLED,

    /** Transferido a otra finca */
    TRANSFERRED,

    /** Existe pero no participa en operaciones */
    INACTIVE
}
