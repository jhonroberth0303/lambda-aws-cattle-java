package com.cattle.dtos.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para consultas a Knowledge Base.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de consulta a Knowledge Base")
public class KnowledgeRequestDTO {
    
    @NotBlank(message = "La pregunta es requerida")
    @Size(max = 1000, message = "La pregunta no puede exceder 1000 caracteres")
    @Schema(description = "Pregunta del usuario sobre conocimiento ganadero", 
            example = "¿Cuál es el protocolo de vacunación para terneros?", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String question;
    
    @Size(max = 100, message = "El ID de sesión no puede exceder 100 caracteres")
    @Schema(description = "ID de sesión para mantener contexto de conversación (opcional)", 
            example = "session-123-abc")
    private String sessionId;
}
