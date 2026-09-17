package com.cattle.services;

import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.dtos.chatbot.PastureContextDTO;
import com.cattle.entities.Pasture;
import com.cattle.enums.QueryIntent;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.PastureRepository;
import com.cattle.services.chatbot.ChatbotContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de consultas de potreros para el chatbot.
 * Proporciona métodos especializados para obtener información de potreros y rotación,
 * e implementa {@link ChatbotContextProvider} para PASTURE_STATUS.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PastureQueryService implements ChatbotContextProvider {

    private static final Set<QueryIntent> SUPPORTED_INTENTS = Set.of(QueryIntent.PASTURE_STATUS);

    private final PastureRepository pastureRepository;

    // ============ ChatbotContextProvider ============

    @Override
    public Set<QueryIntent> supportedIntents() {
        return SUPPORTED_INTENTS;
    }

    @Override
    public String buildContext(IntentContext intent, String farmId) {
        return buildPastureContext(farmId);
    }

    /**
     * Construye contexto para información de potreros.
     */
    private String buildPastureContext(String farmId) {
        StringBuilder context = new StringBuilder();

        List<PastureContextDTO> available = getAvailablePastures(farmId);
        List<PastureContextDTO> inUse = getPasturesInUse(farmId);
        Double totalHaInUse = getTotalHectaresInUse(farmId);
        Double totalHaAvailable = getTotalAvailableHectares(farmId);
        Map<String, Integer> byStatus = getPastureCountByStatus(farmId);

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
     * Obtiene potreros disponibles de una finca
     */
    public List<PastureContextDTO> getAvailablePastures(String farmId) throws RepositoryException {
        log.info("Getting available pastures for farmId: {}", farmId);
        
        Optional<List<Pasture>> pasturesOpt = pastureRepository.findPastures2(farmId);
        
        if (pasturesOpt.isEmpty()) {
            log.info("No pastures found for farmId: {}", farmId);
            return new ArrayList<>();
        }
        
        return pasturesOpt.get().stream()
                .filter(p -> "DISPONIBLE".equalsIgnoreCase(p.getStatus()) || 
                           "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                .map(this::toPastureContextDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene potreros en uso de una finca
     */
    public List<PastureContextDTO> getPasturesInUse(String farmId) throws RepositoryException {
        log.info("Getting pastures in use for farmId: {}", farmId);
        
        Optional<List<Pasture>> pasturesOpt = pastureRepository.findPastures2(farmId);
        
        if (pasturesOpt.isEmpty()) {
            log.info("No pastures found for farmId: {}", farmId);
            return new ArrayList<>();
        }
        
        return pasturesOpt.get().stream()
                .filter(p -> "EN_USO".equalsIgnoreCase(p.getStatus()) || 
                           "IN_USE".equalsIgnoreCase(p.getStatus()) ||
                           "OCUPADO".equalsIgnoreCase(p.getStatus()))
                .map(this::toPastureContextDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Calcula el total de hectáreas en uso
     */
    public Double getTotalHectaresInUse(String farmId) throws RepositoryException {
        log.info("Getting total hectares in use for farmId: {}", farmId);
        
        List<PastureContextDTO> pasturesInUse = getPasturesInUse(farmId);
        
        return pasturesInUse.stream()
                .filter(p -> p.getAreaHa() != null)
                .mapToDouble(PastureContextDTO::getAreaHa)
                .sum();
    }
    
    /**
     * Calcula el total de hectáreas disponibles
     */
    public Double getTotalAvailableHectares(String farmId) throws RepositoryException {
        log.info("Getting total available hectares for farmId: {}", farmId);
        
        List<PastureContextDTO> availablePastures = getAvailablePastures(farmId);
        
        return availablePastures.stream()
                .filter(p -> p.getAreaHa() != null)
                .mapToDouble(PastureContextDTO::getAreaHa)
                .sum();
    }
    
    /**
     * Obtiene conteo de potreros por estado
     */
    public Map<String, Integer> getPastureCountByStatus(String farmId) throws RepositoryException {
        log.info("Getting pasture count by status for farmId: {}", farmId);
        
        Optional<List<Pasture>> pasturesOpt = pastureRepository.findPastures2(farmId);
        
        if (pasturesOpt.isEmpty()) {
            log.info("No pastures found for farmId: {}", farmId);
            return new HashMap<>();
        }
        
        return pasturesOpt.get().stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(
                        p -> normalizeStatus(p.getStatus()),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }
    
    /**
     * Obtiene potreros que necesitan rotación (más de X días en uso)
     */
    public List<PastureContextDTO> getPasturesNeedingRotation(String farmId, int daysThreshold) throws RepositoryException {
        log.info("Getting pastures needing rotation for farmId: {} (threshold: {} days)", farmId, daysThreshold);
        
        List<PastureContextDTO> pasturesInUse = getPasturesInUse(farmId);
        
        return pasturesInUse.stream()
                .filter(p -> {
                    Integer daysSinceRotation = p.getDaysSinceLastRotation();
                    return daysSinceRotation != null && daysSinceRotation >= daysThreshold;
                })
                .sorted(Comparator.comparing(PastureContextDTO::getDaysSinceLastRotation).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los potreros de una finca con su información de contexto
     */
    public List<PastureContextDTO> getAllPastures(String farmId) throws RepositoryException {
        log.info("Getting all pastures for farmId: {}", farmId);
        
        Optional<List<Pasture>> pasturesOpt = pastureRepository.findPastures2(farmId);
        
        if (pasturesOpt.isEmpty()) {
            log.info("No pastures found for farmId: {}", farmId);
            return new ArrayList<>();
        }
        
        return pasturesOpt.get().stream()
                .map(this::toPastureContextDTO)
                .collect(Collectors.toList());
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Normaliza el status del potrero a valores estándar
     */
    private String normalizeStatus(String status) {
        if (status == null) return "UNKNOWN";
        
        String upper = status.toUpperCase();
        if (upper.contains("DISPONIBLE") || upper.contains("AVAILABLE")) {
            return "DISPONIBLE";
        } else if (upper.contains("USO") || upper.contains("USE") || upper.contains("OCUPADO")) {
            return "EN_USO";
        } else if (upper.contains("RECUPERACION") || upper.contains("RECOVERY")) {
            return "EN_RECUPERACION";
        } else if (upper.contains("MANTENIMIENTO") || upper.contains("MAINTENANCE")) {
            return "MANTENIMIENTO";
        }
        return status;
    }
    
    /**
     * Calcula días desde última rotación
     */
    private Integer calculateDaysSinceLastRotation(String lastUseAt) {
        if (lastUseAt == null || lastUseAt.isEmpty()) {
            return null;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate lastUse = LocalDate.parse(lastUseAt, formatter);
            return (int) ChronoUnit.DAYS.between(lastUse, LocalDate.now());
        } catch (Exception e) {
            log.error("Error parsing lastUseAt date: {}", lastUseAt, e);
            return null;
        }
    }
    
    /**
     * Determina la calidad del pasto basándose en altura y días de uso
     */
    private String determineGrassQuality(Integer heightCm, Integer daysSinceRotation) {
        if (heightCm == null) {
            return "unknown";
        }
        
        // Lógica simple de calidad basada en altura
        if (heightCm >= 25) {
            return "excellent";
        } else if (heightCm >= 20) {
            return "good";
        } else if (heightCm >= 15) {
            return "fair";
        } else {
            return "poor";
        }
    }
    
    /**
     * Convierte Pasture a PastureContextDTO
     */
    private PastureContextDTO toPastureContextDTO(Pasture pasture) {
        Integer daysSinceLastRotation = calculateDaysSinceLastRotation(pasture.getLastUseAt());
        String grassQuality = determineGrassQuality(pasture.getCurrentHeightCm(), daysSinceLastRotation);
        
        return PastureContextDTO.builder()
                .pastureId(pasture.getId())
                .name(pasture.getName())
                .status(normalizeStatus(pasture.getStatus()))
                .areaHa(pasture.getAreaHa())
                .grassType(pasture.getSpecies())
                .capacity(null) // No hay campo de capacidad en entity
                .currentBovines(null) // No hay campo de bovinos actuales en entity
                .daysSinceLastRotation(daysSinceLastRotation)
                .grassQuality(grassQuality)
                .build();
    }
}
