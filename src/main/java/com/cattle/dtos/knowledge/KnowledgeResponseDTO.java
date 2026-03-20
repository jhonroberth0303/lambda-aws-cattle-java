package com.cattle.dtos.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para consultas a Knowledge Base.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeResponseDTO {
    
    /**
     * Respuesta generada por el modelo basada en los documentos de conocimiento.
     */
    private String answer;
    
    /**
     * Lista de citaciones de los documentos fuente consultados.
     */
    private List<CitationDTO> citations;
    
    /**
     * Lista de URIs de documentos fuente utilizados.
     */
    private List<String> sources;
    
    /**
     * Duración del procesamiento en milisegundos.
     */
    private Long durationMs;
    
    /**
     * Timestamp de la respuesta.
     */
    private LocalDateTime timestamp;
}
