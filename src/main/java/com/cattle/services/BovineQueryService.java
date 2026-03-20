package com.cattle.services;

import com.cattle.dtos.chatbot.BovineContextDTO;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.BovineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de consultas de bovinos para el chatbot.
 * Proporciona métodos especializados para obtener estadísticas y datos de bovinos.
 */
@Service
@Slf4j
public class BovineQueryService {
    
    @Autowired
    private BovineRepository bovineRepository;
    
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
