package com.cattle.enums;

/**
 * Enum que representa las intenciones detectadas en consultas del usuario.
 * Usado por IntentDetectionService para clasificar el tipo de query del chatbot.
 */
public enum QueryIntent {
    
    /**
     * Contar todos los bovinos: "¿Cuántos bovinos tengo?"
     */
    COUNT_BOVINES,

    /**
     * Contar por género: "¿Cuántos machos tengo?"
     */
    COUNT_BY_GENDER,
    
    /**
     * Obtener detalles de un bovino específico: "Detalles del bovino 123"
     */
    GET_BOVINE_DETAILS,
    
    /**
     * Listar todos los bovinos: "Muéstrame todos los bovinos", "Lista de animales"
     */
    LIST_ALL_BOVINES,
    
    /**
     * Agregación de lactancia: "Producción promedio"
     */
    AGGREGATE_MILKING,
    
    /**
     * Estado de potreros: "¿Qué potreros están disponibles?"
     */
    PASTURE_STATUS,
    
    /**
     * Query general no clasificada
     */
    GENERAL_QUERY
}
