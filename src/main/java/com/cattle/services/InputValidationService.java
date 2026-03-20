package com.cattle.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio de validación y sanitización de input del usuario.
 * Previene ataques de inyección (SQL, NoSQL, XSS, Prompt Injection).
 */
@Slf4j
@Service
public class InputValidationService {

    private static final int MAX_LENGTH = 1000;
    private static final int MIN_LENGTH = 1;
    
    // Patrón de caracteres permitidos: letras (incluyendo acentos), números, espacios y puntuación básica
    private static final Pattern ALLOWED_CHARS_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9áéíóúüñÁÉÍÓÚÜÑ\\s.,;:¿?¡!()\\-_'\"@#%&*+=/<>°]+$"
    );
    
    // Palabras clave de SQL/NoSQL que podrían indicar inyección
    private static final List<String> SQL_INJECTION_KEYWORDS = Arrays.asList(
            "DROP", "DELETE", "INSERT", "UPDATE", "SELECT", "UNION",
            "ALTER", "TRUNCATE", "EXEC", "EXECUTE", "CREATE", "GRANT",
            "--", ";--", "/*", "*/", "@@", "@", "xp_"
    );
    
    // Palabras clave de NoSQL/DynamoDB injection
    private static final List<String> NOSQL_INJECTION_KEYWORDS = Arrays.asList(
            "$where", "$gt", "$lt", "$ne", "$regex", "$exists",
            "dynamodb:", "aws:", "arn:", "lambda:"
    );
    
    // Patrones de prompt injection para LLMs
    private static final List<String> PROMPT_INJECTION_PATTERNS = Arrays.asList(
            "ignore previous instructions",
            "ignore all previous",
            "disregard above",
            "forget your instructions",
            "new instructions:",
            "system prompt:",
            "you are now",
            "pretend you are"
    );

    /**
     * Sanitiza el input del usuario removiendo caracteres peligrosos.
     * 
     * @param input Texto del usuario
     * @return Texto sanitizado
     * @throws IllegalArgumentException si el input es null, vacío o demasiado largo
     */
    public String sanitize(String input) {
        if (input == null) {
            throw new IllegalArgumentException("El mensaje no puede ser nulo");
        }
        
        String trimmed = input.trim();
        
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("El mensaje excede la longitud máxima de %d caracteres", MAX_LENGTH)
            );
        }
        
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("El mensaje es demasiado corto");
        }
        
        // Remover caracteres de control y no imprimibles
        String sanitized = removeControlCharacters(trimmed);
        
        // Escapar caracteres especiales para prevenir XSS
        sanitized = escapeHtmlCharacters(sanitized);
        
        // Detectar y bloquear patrones maliciosos
        if (containsMaliciousPatterns(sanitized)) {
            log.warn("Detected malicious pattern in input: {}", sanitized.substring(0, Math.min(50, sanitized.length())));
            throw new IllegalArgumentException("El mensaje contiene patrones no permitidos");
        }
        
        return sanitized;
    }

    /**
     * Valida la longitud del input.
     * 
     * @param input Texto a validar
     * @throws IllegalArgumentException si la longitud excede el máximo
     */
    public void validateLength(String input) {
        if (input != null && input.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("El mensaje excede la longitud máxima de %d caracteres", MAX_LENGTH)
            );
        }
    }

    /**
     * Verifica si el input contiene patrones maliciosos.
     * 
     * @param input Texto a verificar
     * @return true si contiene patrones maliciosos
     */
    public boolean containsMaliciousPatterns(String input) {
        if (input == null) {
            return false;
        }
        
        String upperInput = input.toUpperCase();
        String lowerInput = input.toLowerCase();
        
        // Verificar SQL injection keywords
        for (String keyword : SQL_INJECTION_KEYWORDS) {
            if (upperInput.contains(keyword.toUpperCase())) {
                // Verificar si es un uso legítimo (ej: "¿Puedo seleccionar una vaca?")
                // Solo bloquear si parece un comando real
                if (looksLikeSqlCommand(input, keyword)) {
                    log.warn("SQL injection keyword detected: {}", keyword);
                    return true;
                }
            }
        }
        
        // Verificar NoSQL injection keywords
        for (String keyword : NOSQL_INJECTION_KEYWORDS) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                log.warn("NoSQL injection keyword detected: {}", keyword);
                return true;
            }
        }
        
        // Verificar prompt injection patterns
        for (String pattern : PROMPT_INJECTION_PATTERNS) {
            if (lowerInput.contains(pattern.toLowerCase())) {
                log.warn("Prompt injection pattern detected: {}", pattern);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Remueve caracteres de control y no imprimibles.
     */
    private String removeControlCharacters(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            // Permitir caracteres imprimibles, espacios y saltos de línea
            if (!Character.isISOControl(c) || c == '\n' || c == '\r' || c == '\t') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escapa caracteres HTML para prevenir XSS.
     */
    private String escapeHtmlCharacters(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        // No escapamos comillas para permitir uso normal en español
    }

    /**
     * Verifica si el uso de una palabra clave parece un comando SQL real.
     * Evita falsos positivos como "quiero seleccionar una vaca".
     */
    private boolean looksLikeSqlCommand(String input, String keyword) {
        String upperInput = input.toUpperCase();
        int index = upperInput.indexOf(keyword.toUpperCase());
        
        if (index == -1) {
            return false;
        }
        
        // Si la palabra está al inicio seguida de espacio y asterisco o palabra SQL, es sospechoso
        if (keyword.equalsIgnoreCase("SELECT") || keyword.equalsIgnoreCase("DROP") 
            || keyword.equalsIgnoreCase("DELETE") || keyword.equalsIgnoreCase("INSERT")) {
            
            // Verificar si hay patrones típicos de SQL después del keyword
            String afterKeyword = upperInput.substring(index + keyword.length()).trim();
            if (afterKeyword.startsWith("*") || afterKeyword.startsWith("FROM") 
                || afterKeyword.startsWith("TABLE") || afterKeyword.startsWith("INTO")) {
                return true;
            }
        }
        
        // Verificar patrones de comentarios SQL
        if (input.contains("--") || input.contains("/*") || input.contains("*/")) {
            return true;
        }
        
        return false;
    }
}
