package com.cattle.tasks.planner;

/**
 * Estado del bovino que el planner necesita y que no sale de los eventos.
 *
 * @param female    solo las hembras generan tareas (G5)
 * @param active    un bovino inactivo no genera tareas y cierra las abiertas (G4)
 * @param lactating lactancia activa hoy; decide si R2 programa SECAR (P4)
 */
public record BovineReproductiveContext(String bovineId, String farmId, boolean female, boolean active,
                                        boolean lactating) {
}
