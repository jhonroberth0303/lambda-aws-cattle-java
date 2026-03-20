package com.cattle.dtos.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una citación de documento en la respuesta de Knowledge Base.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitationDTO {
    
    /**
     * Texto relevante extraído del documento fuente.
     */
    private String text;
    
    /**
     * URI del documento fuente (S3 o referencia interna).
     */
    private String documentUri;
    
    /**
     * Número de página donde se encontró la información (opcional).
     */
    private Integer page;

    /**
     * Categoría del documento fuente.
     */
    private String categoria;
}
