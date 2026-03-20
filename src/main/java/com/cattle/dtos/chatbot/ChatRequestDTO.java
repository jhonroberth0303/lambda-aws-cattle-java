package com.cattle.dtos.chatbot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para el chatbot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de mensaje al chatbot")
public class ChatRequestDTO {
    
    @NotBlank(message = "El mensaje es requerido")
    @Size(max = 1000, message = "El mensaje no puede exceder 1000 caracteres")
    @Schema(description = "Mensaje del usuario", example = "¿Cuántos bovinos tengo registrados?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userMessage;
    
    @Size(max = 100, message = "El ID de conversación no puede exceder 100 caracteres")
    @Schema(description = "ID de la conversación (para mantener contexto)", example = "conv-123-abc")
    private String conversationId;
}
