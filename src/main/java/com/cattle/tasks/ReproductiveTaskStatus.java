package com.cattle.tasks;

/**
 * Estado de una tarea. Regla G3: HECHA y CANCELADA son finales; VENCIDA solo puede
 * pasar a HECHA (cumplimiento tardío) o CANCELADA; nada vuelve a PENDIENTE.
 */
public enum ReproductiveTaskStatus {
    PENDIENTE,
    HECHA,
    VENCIDA,
    CANCELADA;

    /** La tarea sigue en la agenda (índice disperso de abiertas). */
    public boolean isOpen() {
        return this == PENDIENTE || this == VENCIDA;
    }

    public boolean canTransitionTo(ReproductiveTaskStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case PENDIENTE -> true;
            case VENCIDA -> target == HECHA || target == CANCELADA;
            case HECHA, CANCELADA -> false;
        };
    }
}
