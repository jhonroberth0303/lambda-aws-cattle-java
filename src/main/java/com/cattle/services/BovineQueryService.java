package com.cattle.services;

import com.cattle.dtos.chatbot.BovineContextDTO;
import com.cattle.dtos.chatbot.IntentContext;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.enums.QueryIntent;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.BovineRepository;
import com.cattle.services.chatbot.ChatbotContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de consultas de bovinos para el chatbot.
 * Proporciona métodos especializados para obtener estadísticas y datos de bovinos,
 * e implementa {@link ChatbotContextProvider} para las intenciones sobre bovinos.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BovineQueryService implements ChatbotContextProvider {

    private static final Set<QueryIntent> SUPPORTED_INTENTS = Set.of(
            QueryIntent.COUNT_BOVINES,
            QueryIntent.COUNT_BY_GENDER,
            QueryIntent.GET_BOVINE_DETAILS,
            QueryIntent.LIST_ALL_BOVINES
    );

    private final BovineRepository bovineRepository;

    // ============ ChatbotContextProvider ============

    @Override
    public Set<QueryIntent> supportedIntents() {
        return SUPPORTED_INTENTS;
    }

    @Override
    public String buildContext(IntentContext intent, String farmId) {
        return switch (intent.getIntent()) {
            case COUNT_BOVINES -> buildBovineCountContext(farmId);
            case COUNT_BY_GENDER -> buildGenderCountContext(farmId);
            case GET_BOVINE_DETAILS -> buildBovineDetailsContext(farmId);
            case LIST_ALL_BOVINES -> buildAllBovinesListContext(farmId);
            default -> "";
        };
    }

    /**
     * Construye contexto para conteo general de bovinos.
     */
    private String buildBovineCountContext(String farmId) {
        StringBuilder context = new StringBuilder();

        Long totalBovines = countAllBovines(farmId);
        Map<String, Long> byGender = countByGender(farmId);

        context.append("TOTAL DE BOVINOS: ").append(totalBovines).append("\n\n");

        context.append("\nPor Género:\n");
        byGender.forEach((gender, count) ->
                context.append("- ").append(translateGender(gender)).append(": ").append(count).append("\n"));

        return context.toString();
    }

    /**
     * Construye contexto para conteo por género.
     */
    private String buildGenderCountContext(String farmId) {
        StringBuilder context = new StringBuilder();

        Map<String, Long> byGender = countByGender(farmId);

        context.append("BOVINOS POR GÉNERO:\n");
        byGender.forEach((g, count) ->
                context.append("- ").append(translateGender(g)).append(": ").append(count).append("\n"));

        Long total = countAllBovines(farmId);
        context.append("\nTotal general: ").append(total).append("\n");

        return context.toString();
    }

    /**
     * Construye contexto para detalles de bovino específico.
     */
    private String buildBovineDetailsContext(String farmId) {
        // Por ahora retorna información general
        // En implementación completa, se extraería el ID del bovino del mensaje
        return "Para obtener detalles de un bovino específico, se requiere implementar extracción de ID.\n" +
               buildBovineCountContext(farmId);
    }

    /**
     * Construye contexto con lista detallada de todos los bovinos.
     */
    private String buildAllBovinesListContext(String farmId) {
        StringBuilder context = new StringBuilder();

        List<BovineContextDTO> allBovines = getAllBovinesDetails(farmId);

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
     * Traduce género a español legible.
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
     * Cuenta todos los bovinos de una finca
     */
    public Long countAllBovines(String farmId) throws RepositoryException {
        log.info("Counting all bovines for farmId: {}", farmId);
        return bovineRepository.countByFarmId(farmId);
    }
    
    /**
     * Cuenta bovinos por género
     * @return Mapa con género como clave y conteo como valor
     */
    public Map<String, Long> countByGender(String farmId) throws RepositoryException {
        log.info("Counting bovines by gender for farmId: {}", farmId);
        List<BovineIdentityItem> bovineIdentityItems = bovineRepository.findAllByFarmId(farmId);
        
        return bovineIdentityItems.stream()
                .filter(b -> b.getGender() != null)
                .collect(Collectors.groupingBy(
                        BovineIdentityItem::getGender,
                        Collectors.counting()
                ));
    }
    
    /**
     * Cuenta bovinos preñados
     */
    public Long countPregnantBovines(String farmId) throws RepositoryException {
        log.info("Counting pregnant bovines for farmId: {}", farmId);
        return (long) bovineRepository.findByFarmIdAndStatus(farmId, "PREGNANT").size();
    }
    
    /**
     * Obtiene distribución de edades de bovinos
     * @return Mapa con rango de edad como clave y conteo como valor
     */
    public Map<String, Integer> getAgeDistribution(String farmId) throws RepositoryException {
        log.info("Getting age distribution for farmId: {}", farmId);
        List<BovineIdentityItem> bovineIdentityItems = bovineRepository.findAllByFarmId(farmId);
        Map<String, Integer> distribution = new HashMap<>();
        
        for (BovineIdentityItem bovineIdentityItem : bovineIdentityItems) {
            if (bovineIdentityItem.getBornDate() != null) {
                int ageInMonths = calculateAgeInMonths(bovineIdentityItem.getBornDate());
                String ageRange = getAgeRange(ageInMonths);
                distribution.merge(ageRange, 1, Integer::sum);
            }
        }
        
        return distribution;
    }
    
    /**
     * Obtiene terneros próximos al destete (5-8 meses)
     */
    public List<BovineContextDTO> getCalvesForWeaning(String farmId) throws RepositoryException {
        log.info("Getting calves for weaning for farmId: {}", farmId);
        List<BovineIdentityItem> calves = bovineRepository.findByFarmIdAndCategory(farmId, "calf");
        
        return calves.stream()
                .filter(calf -> {
                    if (calf.getBornDate() == null) return false;
                    int ageInMonths = calculateAgeInMonths(calf.getBornDate());
                    return ageInMonths >= 5 && ageInMonths <= 8;
                })
                .map(this::toBovineContextDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene bovinos próximos al parto (menos de X días)
     */
    public List<BovineContextDTO> getBovinesNearCalving(String farmId, int daysThreshold) throws RepositoryException {
        log.info("Getting bovines near calving for farmId: {} with threshold: {} days", farmId, daysThreshold);
        List<BovineIdentityItem> pregnantBovineIdentityItems = bovineRepository.findByFarmIdAndStatus(farmId, "PREGNANT");
        
        // Nota: Aquí necesitaríamos la fecha estimada de parto, que no está en el modelo actual
        // Por ahora retornamos la lista de preñadas
        return pregnantBovineIdentityItems.stream()
                .map(this::toBovineContextDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene bovinos en lactancia
     */
    public List<BovineContextDTO> getLactatingBovines(String farmId) throws RepositoryException {
        log.info("Getting lactating bovines for farmId: {}", farmId);
        List<BovineIdentityItem> lactating = bovineRepository.findByFarmIdAndStatus(farmId, "LACTATING");
        
        return lactating.stream()
                .map(this::toBovineContextDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene lista detallada de todos los bovinos de la finca
     */
    public List<BovineContextDTO> getAllBovinesDetails(String farmId) throws RepositoryException {
        log.info("Getting all bovines details for farmId: {}", farmId);
        List<BovineIdentityItem> allBovineIdentityItems = bovineRepository.findAllByFarmId(farmId);
        
        return allBovineIdentityItems.stream()
                .map(this::toBovineContextDTO)
                .sorted(Comparator.comparing(BovineContextDTO::getBovineId))
                .collect(Collectors.toList());
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Calcula la edad en meses a partir de una fecha de nacimiento
     */
    private int calculateAgeInMonths(String bornDateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate bornDate = LocalDate.parse(bornDateStr, formatter);
            return Period.between(bornDate, LocalDate.now()).getYears() * 12 + 
                   Period.between(bornDate, LocalDate.now()).getMonths();
        } catch (Exception e) {
            log.error("Error parsing born date: {}", bornDateStr, e);
            return 0;
        }
    }
    
    /**
     * Clasifica la edad en rangos para estadísticas
     */
    private String getAgeRange(int ageInMonths) {
        if (ageInMonths < 6) return "0-5 meses";
        if (ageInMonths < 12) return "6-11 meses";
        if (ageInMonths < 24) return "12-23 meses";
        if (ageInMonths < 36) return "24-35 meses";
        return "36+ meses";
    }
    
    /**
     * Convierte Bovine a BovineContextDTO
     */
    private BovineContextDTO toBovineContextDTO(BovineIdentityItem bovineIdentityItem) {
        int ageInMonths = bovineIdentityItem.getBornDate() != null ? calculateAgeInMonths(bovineIdentityItem.getBornDate()) : 0;
        
        LocalDate bornDate = null;
        if (bovineIdentityItem.getBornDate() != null) {
            try {
                bornDate = LocalDate.parse(bovineIdentityItem.getBornDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.error("Error parsing born date: {}", bovineIdentityItem.getBornDate(), e);
            }
        }
        
        return BovineContextDTO.builder()
                .bovineId(String.valueOf(bovineIdentityItem.getBovineId()))
                .name(bovineIdentityItem.getName())
                .gender(bovineIdentityItem.getGender())
                .bornDate(bornDate)
                .ageInMonths(ageInMonths)
                .breed(bovineIdentityItem.getBreed())
                .build();
    }
}
