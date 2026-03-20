package com.cattle.dtos.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de contexto de lactancia para el chatbot.
 * Contiene datos de producción de leche para construcción de contexto en Bedrock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilkingContextDTO {
    
    /**
     * ID del registro de lactancia
     */
    private String milkingId;
    
    /**
     * ID del bovino
     */
    private String bovineId;
    
    /**
     * Nombre del bovino
     */
    private String bovineName;
    
    /**
     * Fecha de ordeño
     */
    private LocalDate milkingDate;
    
    /**
     * Hora de ordeño
     */
    private LocalTime milkingTime;
    
    /**
     * Turno: morning, afternoon, evening
     */
    private String shift;
    
    /**
     * Cantidad de leche en litros
     */
    private Double litersMilked;
    
    /**
     * Producción promedio del rebaño para comparación
     */
    private Double averageProduction;
}
