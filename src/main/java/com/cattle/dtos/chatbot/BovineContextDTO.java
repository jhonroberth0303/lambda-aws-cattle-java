package com.cattle.dtos.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de contexto de bovino para el chatbot.
 * Contiene datos simplificados de bovino para construcción de contexto en Bedrock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BovineContextDTO {
    
    /**
     * ID del bovino
     */
    private String bovineId;
    /**
     * Nombre/identificador del bovino
     */
    private String name;
    /**
     * Género: male, female
     */
    private String gender;

    /**
     * Fecha de nacimiento
     */
    private LocalDate bornDate;
    /**
     * Edad en meses (calculada)
     */
    private Integer ageInMonths;
    /**
     * Raza
     */
    private String breed;
}
