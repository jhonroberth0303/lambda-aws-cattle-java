package com.cattle.services;

import com.cattle.dtos.chatbot.BovineContextDTO;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.dtos.chatbot.MilkingContextDTO;
import com.cattle.dtos.chatbot.PastureContextDTO;
import com.cattle.enums.QueryIntent;
import com.cattle.exceptions.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio constructor de contexto para enriquecer prompts de Bedrock.
 * Orquesta llamadas a los servicios de consulta y formatea datos para el modelo de IA.
 */
@Service
@Slf4j
public class ContextBuilderService {
    
    @Autowired
    private BovineQueryService bovineQueryService;
    
    @Autowired
    private MilkingQueryService milkingQueryService;
    
    @Autowired
    private PastureQueryService pastureQueryService;
    
    private static final int MAX_CONTEXT_LENGTH = 2000;
    
    /**
     * Construye el contexto completo basado en la intención detectada
     */
    public String buildContext(IntentContext intent, String farmId) {
        log.info("Building context for intent: {} and farmId: {}", intent.getIntent(), farmId);
        
        try {
            StringBuilder context = new StringBuilder();
            context.append("=== CONTEXTO DE LA FINCA ===\n\n");
            
            switch (intent.getIntent()) {
                case COUNT_BOVINES:
                    context.append(buildBovineCountContext(farmId));
                    break;

                case COUNT_BY_GENDER:
                    context.append(buildGenderCountContext(intent, farmId));
                    break;
                    
                case GET_BOVINE_DETAILS:
                    context.append(buildBovineDetailsContext(intent, farmId));
                    break;
                
                case LIST_ALL_BOVINES:
                    context.append(buildAllBovinesListContext(farmId));
                    break;
                    
                case AGGREGATE_MILKING:
                    context.append(buildMilkingContext(farmId));
                    break;
                    
                case PASTURE_STATUS:
                    context.append(buildPastureContext(farmId));
                    break;
                    
                case GENERAL_QUERY:
                default:
                    context.append(buildGeneralContext(farmId));
                    break;
            }
            
            String finalContext = truncateContext(context.toString());
            log.info("Context built successfully, length: {}", finalContext.length());
            return finalContext;
            
        } catch (Exception e) {
            log.error("Error building context for intent: {}", intent.getIntent(), e);
            return "Error al construir el contexto. Por favor intenta nuevamente.";
        }
    }
    
    /**
     * Construye contexto para conteo general de bovinos
     */
    private String buildBovineCountContext(String farmId) throws RepositoryException {
        StringBuilder context = new StringBuilder();
        
        Long totalBovines = bovineQueryService.countAllBovines(farmId);
        Map<String, Long> byGender = bovineQueryService.countByGender(farmId);
        
        context.append("TOTAL DE BOVINOS: ").append(totalBovines).append("\n\n");

        context.append("\nPor Género:\n");
        byGender.forEach((gender, count) -> 
            context.append("- ").append(translateGender(gender)).append(": ").append(count).append("\n"));

        return context.toString();
    }
    
    /**
     * Construye contexto para conteo por género
     */
    private String buildGenderCountContext(IntentContext intent, String farmId) throws RepositoryException {
        StringBuilder context = new StringBuilder();
        
        Map<String, Long> byGender = bovineQueryService.countByGender(farmId);
        String gender = intent.getGender();
        
        context.append("BOVINOS POR GÉNERO:\n");
        byGender.forEach((g, count) -> 
            context.append("- ").append(translateGender(g)).append(": ").append(count).append("\n"));
        
        Long total = bovineQueryService.countAllBovines(farmId);
        context.append("\nTotal general: ").append(total).append("\n");
        
        return context.toString();
    }
    
    /**
     * Construye contexto para detalles de bovino específico
     */
    private String buildBovineDetailsContext(IntentContext intent, String farmId) throws RepositoryException {
        // Por ahora retorna información general
        // En implementación completa, se extraería el ID del bovino del mensaje
        return "Para obtener detalles de un bovino específico, se requiere implementar extracción de ID.\n" +
               buildBovineCountContext(farmId);
    }
    
    /**
     * Construye contexto con lista detallada de todos los bovinos
     */
    private String buildAllBovinesListContext(String farmId) throws RepositoryException {
        StringBuilder context = new StringBuilder();
        
        List<BovineContextDTO> allBovines = bovineQueryService.getAllBovinesDetails(farmId);
        
        context.append("LISTA COMPLETA DE BOVINOS:\n");
        context.append("Total de animales: ").append(allBovines.size()).append("\n\n");
        
        if (allBovines.isEmpty()) {
            context.append("No se encontraron bovinos registrados en la finca.\n");
            return context.toString();
        }
        
        // Presentar bovinos en lista simple, sin agrupar ni mostrar categoría o estado
        for (BovineContextDTO bovine : allBovines) {
            context.append("• ID: ").append(bovine.getBovineId());
            if (bovine.getName() != null) {
                context.append(" - Nombre: ").append(bovine.getName());
            }
            context.append(" - Género: ").append(translateGender(bovine.getGender()));
            if (bovine.getBreed() != null) {
                context.append(" - Raza: ").append(bovine.getBreed());
            }
            if (bovine.getAgeInMonths() != null && bovine.getAgeInMonths() > 0) {
                context.append(" - Edad: ").append(bovine.getAgeInMonths()).append(" meses");
            }
            context.append("\n");
        }

        return context.toString();
    }
    
    /**
     * Construye contexto para producción de leche
     */
    private String buildMilkingContext(String farmId) throws RepositoryException {
        StringBuilder context = new StringBuilder();
        
        Double monthlyAvg = milkingQueryService.getMonthlyAverageProduction(farmId);
        Double weeklyAvg = milkingQueryService.getWeeklyAverageProduction(farmId);
        Map<String, Double> byShift = milkingQueryService.getProductionByShift(farmId);
        MilkingContextDTO topProducer = milkingQueryService.getTopProducerBovine(farmId);
        
        context.append("PRODUCCIÓN DE LECHE:\n\n");
        context.append("Promedio mensual: ").append(String.format("%.2f", monthlyAvg)).append(" litros\n");
        context.append("Promedio semanal: ").append(String.format("%.2f", weeklyAvg)).append(" litros\n\n");
        
        context.append("Producción por turno:\n");
        byShift.forEach((shift, liters) -> 
            context.append("- ").append(shift).append(": ").append(String.format("%.2f", liters)).append(" litros\n"));
        
        if (topProducer != null) {
            context.append("\nTop productor:\n");
            context.append("- Bovino: ").append(topProducer.getBovineName()).append("\n");
            context.append("- Producción: ").append(String.format("%.2f", topProducer.getLitersMilked())).append(" litros\n");
        }
        
        return context.toString();
    }
    
    /**
     * Construye contexto para información de potreros
     */
    private String buildPastureContext(String farmId) throws RepositoryException {
        StringBuilder context = new StringBuilder();
        
        List<PastureContextDTO> available = pastureQueryService.getAvailablePastures(farmId);
        List<PastureContextDTO> inUse = pastureQueryService.getPasturesInUse(farmId);
        Double totalHaInUse = pastureQueryService.getTotalHectaresInUse(farmId);
        Double totalHaAvailable = pastureQueryService.getTotalAvailableHectares(farmId);
        Map<String, Integer> byStatus = pastureQueryService.getPastureCountByStatus(farmId);
        
        context.append("ESTADO DE POTREROS:\n\n");
        context.append("Potreros disponibles: ").append(available.size()).append("\n");
        context.append("Hectáreas disponibles: ").append(String.format("%.2f", totalHaAvailable)).append(" ha\n\n");
        
        context.append("Potreros en uso: ").append(inUse.size()).append("\n");
        context.append("Hectáreas en uso: ").append(String.format("%.2f", totalHaInUse)).append(" ha\n\n");
        
        context.append("Distribución por estado:\n");
        byStatus.forEach((status, count) -> 
            context.append("- ").append(status).append(": ").append(count).append("\n"));
        
        return context.toString();
    }
    
    /**
     * Construye contexto general con datos de todas las áreas
     */
    private String buildGeneralContext(String farmId) throws RepositoryException {
        StringBuilder context = new StringBuilder();
        
        // Resumen de bovinos
        Long totalBovines = bovineQueryService.countAllBovines(farmId);
        context.append("RESUMEN GENERAL:\n\n");
        context.append("Total de bovinos: ").append(totalBovines).append("\n");
        
        // Resumen de producción
        Double monthlyAvg = milkingQueryService.getMonthlyAverageProduction(farmId);
        context.append("Producción promedio mensual: ").append(String.format("%.2f", monthlyAvg)).append(" litros\n");
        
        // Resumen de potreros
        List<PastureContextDTO> available = pastureQueryService.getAvailablePastures(farmId);
        context.append("Potreros disponibles: ").append(available.size()).append("\n");
        
        return context.toString();
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Traduce categoría a español legible
     */
    private String translateCategory(String category) {
        if (category == null) return "Desconocida";
        switch (category.toLowerCase()) {
            case "cow": return "Vacas";
            case "bull": return "Toros";
            case "calf": return "Terneros";
            case "heifer": return "Novillas";
            case "steer": return "Novillos";
            default: return category;
        }
    }
    
    /**
     * Traduce género a español legible
     */
    private String translateGender(String gender) {
        if (gender == null) return "Desconocido";
        switch (gender.toLowerCase()) {
            case "male": return "Machos";
            case "female": return "Hembras";
            default: return gender;
        }
    }
    
    /**
     * Traduce status a español legible
     */
    private String translateStatus(String status) {
        if (status == null) return "Desconocido";
        switch (status.toUpperCase()) {
            case "PREGNANT": return "Preñadas";
            case "LACTATING": return "Lactando";
            case "DRY": return "Secas";
            case "OPEN": return "Abiertas";
            default: return status;
        }
    }
    
    /**
     * Trunca el contexto si excede el límite máximo
     */
    private String truncateContext(String context) {
        if (context.length() <= MAX_CONTEXT_LENGTH) {
            return context;
        }
        
        log.warn("Context exceeds max length ({} > {}), truncating", context.length(), MAX_CONTEXT_LENGTH);
        return context.substring(0, MAX_CONTEXT_LENGTH - 50) + "\n\n[Contexto truncado...]";
    }
    
    /**
     * Construye el prompt enriquecido combinando el contexto con la pregunta del usuario
     */
    public String buildPrompt(String userMessage, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Eres un asistente virtual especializado en gestión ganadera. ");
        prompt.append("Tienes acceso a la siguiente información de la finca:\n\n");
        prompt.append(context);
        prompt.append("\n\n");
        prompt.append("Con base en esta información, responde la siguiente pregunta del usuario de manera clara, ");
        prompt.append("precisa y profesional. Usa lenguaje sencillo y datos específicos cuando estén disponibles.\n\n");
        prompt.append("Pregunta del usuario: ");
        prompt.append(userMessage);
        
        return prompt.toString();
    }
}
