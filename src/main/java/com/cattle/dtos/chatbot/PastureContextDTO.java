package com.cattle.dtos.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de contexto de potrero para el chatbot.
 * Contiene datos de potreros para construcción de contexto en Bedrock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastureContextDTO {
    
    /**
     * ID del potrero
     */
    private String pastureId;
    
    /**
     * Nombre del potrero
     */
    private String name;
    
    /**
     * Estado: DISPONIBLE, EN_USO, EN_RECUPERACION, MANTENIMIENTO
     */
    private String status;
    
    /**
     * Área en hectáreas
     */
    private Double areaHa;
    
    /**
     * Tipo de pasto
     */
    private String grassType;
    
    /**
     * Capacidad de carga (bovinos)
     */
    private Integer capacity;
    
    /**
     * Número de bovinos actuales
     */
    private Integer currentBovines;
    
    /**
     * Días desde última rotación
     */
    private Integer daysSinceLastRotation;
    
    /**
     * Calidad del pasto: excellent, good, fair, poor
     */
    private String grassQuality;
}
