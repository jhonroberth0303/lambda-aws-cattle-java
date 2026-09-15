package com.cattle.enums.profiles;

import java.util.EnumSet;
import java.util.Set;

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
    INACTIVE;

    private static final Set<LifecycleStatus> INACTIVE_STATUSES =
            EnumSet.of(SOLD, DEAD, CULLED, TRANSFERRED, INACTIVE);

    /** ¿Este estado impide operar el bovino (nuevos eventos, estado productivo)? */
    public boolean isInactive() {
        return INACTIVE_STATUSES.contains(this);
    }
}
