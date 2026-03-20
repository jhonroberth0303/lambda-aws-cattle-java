package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.enums.LogType;
import com.cattle.enums.QueryIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de detección de intención basado en análisis de keywords y expresiones regulares.
 * Analiza el mensaje del usuario para determinar la intención y extraer entidades relevantes.
 */
@Service
@Slf4j
public class IntentDetectionService {

    private final LambdaContext lambdaContext;
    
    // Patterns para detección de intenciones
    private static final Pattern COUNT_PATTERN = Pattern.compile("(cuánto|cuánta|cuántas|cuántos|cantidad|número|total)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MILKING_PATTERN = Pattern.compile("(producción|leche|ordeño|ordeñ|litros|lactancia|lactando)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASTURE_PATTERN = Pattern.compile("(potrero|potreros|pasto|pastura|disponible|rotación|rotacion)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DETAIL_PATTERN = Pattern.compile("(detalle|información|info|datos|mostrar|ver) (bovino|vaca|toro|del|de la) (\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIST_ALL_PATTERN = Pattern.compile("\\b(lista|listar|muestra|muéstrame|todos|todas|detalle) (los|las|de|todos|todas)? (bovino|bovinos|animal|animales|vaca|vacas|ganado)\\b", Pattern.CASE_INSENSITIVE);

    // Patterns para género
    private static final Pattern GENDER_MALE = Pattern.compile("\\b(macho|machos)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern GENDER_FEMALE = Pattern.compile("\\b(hembra|hembras)\\b", Pattern.CASE_INSENSITIVE);

    public IntentDetectionService(LambdaContext lambdaContext) {
        this.lambdaContext = lambdaContext;
    }

    /**
     * Detecta la intención del usuario y extrae entidades del mensaje
     */
    public IntentContext detectIntent(String userMessage) {
        lambdaContext.logInfo(LogType.SERVICE, "Detecting intent for message: " + userMessage);
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return createDefaultIntent();
        }
        
        String message = userMessage.toLowerCase().trim();
        
        // Clasificar intención principal
        QueryIntent intent = classifyIntent(message);
        
        // Extraer entidades
        String gender = extractGender(message);
        Map<String, String> filters = extractFilters(message);
        
        // Calcular confianza
        Double confidence = calculateConfidence(message, intent, gender);
        
        IntentContext context = IntentContext.builder()
                .intent(intent)
                .gender(gender)
                .filters(filters)
                .confidenceScore(confidence)
                .build();

        lambdaContext.logInfo(LogType.SERVICE, "Intent detected: " + intent + " with confidence: " + confidence);
        return context;
    }
    
    /**
     * Clasifica la intención principal del mensaje
     */
    private QueryIntent classifyIntent(String message) {
        // Detectar si es detalle de bovino específico
        Matcher detailMatcher = DETAIL_PATTERN.matcher(message);
        if (detailMatcher.find()) {
            return QueryIntent.GET_BOVINE_DETAILS;
        }
        
        // Detectar si quiere listar todos los bovinos
        if (LIST_ALL_PATTERN.matcher(message).find()) {
            return QueryIntent.LIST_ALL_BOVINES;
        }
        
        // Detectar queries sobre potreros
        if (PASTURE_PATTERN.matcher(message).find()) {
            return QueryIntent.PASTURE_STATUS;
        }
        
        // Detectar queries sobre producción/lactancia
        if (MILKING_PATTERN.matcher(message).find()) {
            return QueryIntent.AGGREGATE_MILKING;
        }
        
        // Detectar conteos
        if (COUNT_PATTERN.matcher(message).find()) {
            // Determinar tipo de conteo
            if (extractGender(message) != null) {
                return QueryIntent.COUNT_BY_GENDER;
            } else {
                return QueryIntent.COUNT_BOVINES;
            }
        }
        
        // Fallback: query general
        return QueryIntent.GENERAL_QUERY;
    }

    /**
     * Extrae el género del mensaje
     */
    private String extractGender(String message) {
        if (GENDER_MALE.matcher(message).find()) return "male";
        if (GENDER_FEMALE.matcher(message).find()) return "female";
        return null;
    }

    /**
     * Extrae filtros adicionales del mensaje
     */
    private Map<String, String> extractFilters(String message) {
        Map<String, String> filters = new HashMap<>();
        
        // Extraer rangos de edad
        Pattern agePattern = Pattern.compile("(\\d+)\\s*(mes|meses|año|años)", Pattern.CASE_INSENSITIVE);
        Matcher ageMatcher = agePattern.matcher(message);
        if (ageMatcher.find()) {
            filters.put("age", ageMatcher.group(1));
            filters.put("ageUnit", ageMatcher.group(2));
        }
        
        // Extraer raza si se menciona
        Pattern breedPattern = Pattern.compile("\\b(holstein|jersey|normando|pardo suizo|brahman|angus)\\b", Pattern.CASE_INSENSITIVE);
        Matcher breedMatcher = breedPattern.matcher(message);
        if (breedMatcher.find()) {
            filters.put("breed", breedMatcher.group(1));
        }
        
        return filters;
    }
    
    /**
     * Calcula el score de confianza de la detección
     */
    private Double calculateConfidence(String message, QueryIntent intent, String gender) {
        double confidence = 0.0;
        
        // Confianza base según intención
        switch (intent) {
            case GET_BOVINE_DETAILS:
                confidence = 0.95; // Alta confianza si hay patrón de detalle
                break;
            case LIST_ALL_BOVINES:
                if (LIST_ALL_PATTERN.matcher(message).find()) {
                    confidence = 0.93; // Alta confianza para listar todos
                }
                break;
            case COUNT_BOVINES:
            case COUNT_BY_GENDER:
                if (COUNT_PATTERN.matcher(message).find()) {
                    confidence = 0.90;
                }
                break;
            case AGGREGATE_MILKING:
                if (MILKING_PATTERN.matcher(message).find()) {
                    confidence = 0.90;
                }
                break;
            case PASTURE_STATUS:
                if (PASTURE_PATTERN.matcher(message).find()) {
                    confidence = 0.90;
                }
                break;
            default:
                confidence = 0.60; // Confianza media para queries generales
        }
        
        // Incrementar confianza si se detectaron entidades específicas
        if (gender != null) confidence += 0.03;
        
        // Limitar entre 0 y 1
        return Math.min(1.0, confidence);
    }
    
    /**
     * Crea un contexto de intención por defecto
     */
    private IntentContext createDefaultIntent() {
        return IntentContext.builder()
                .intent(QueryIntent.GENERAL_QUERY)
                .confidenceScore(0.0)
                .filters(new HashMap<>())
                .build();
    }
}
