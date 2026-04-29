package com.cattle.services;

import com.cattle.dtos.chatbot.MilkingContextDTO;
import com.cattle.entities.MilkingRecord;
import com.cattle.exceptions.RepositoryException;
import com.cattle.repository.MilkingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de consultas de lactancia para el chatbot.
 * Proporciona métodos especializados para obtener estadísticas de producción de leche.
 */
@Service
@Slf4j
public class MilkingQueryService {
    
    @Autowired
    private MilkingRepository milkingRepository;
    
    /**
     * Obtiene la producción promedio mensual de una finca
     */
    public Double getMonthlyAverageProduction(String farmId) throws RepositoryException {
        log.info("Getting monthly average production for farmId: {}", farmId);
        
        // Calcular fechas del último mes
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        
        String pk = "FARM#" + farmId;
        String skStart = "MILKING#" + startDate.format(DateTimeFormatter.ISO_DATE);
        String skEnd = "MILKING#" + endDate.format(DateTimeFormatter.ISO_DATE);
        
        Optional<List<MilkingRecord>> milkingsOpt = milkingRepository.getMilkingBetweenDates(pk, skStart, skEnd);
        
        if (milkingsOpt.isEmpty() || milkingsOpt.get().isEmpty()) {
            log.info("No milking records found for farmId: {}", farmId);
            return 0.0;
        }
        
        List<MilkingRecord> milkingRecords = milkingsOpt.get();
        double totalLiters = milkingRecords.stream()
                .filter(m -> m.getLiters() != null)
                .mapToDouble(MilkingRecord::getLiters)
                .sum();
        
        return milkingRecords.isEmpty() ? 0.0 : totalLiters / milkingRecords.size();
    }
    
    /**
     * Obtiene la producción promedio semanal de una finca
     */
    public Double getWeeklyAverageProduction(String farmId) throws RepositoryException {
        log.info("Getting weekly average production for farmId: {}", farmId);
        
        // Calcular fechas de la última semana
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(1);
        
        String pk = "FARM#" + farmId;
        String skStart = "MILKING#" + startDate.format(DateTimeFormatter.ISO_DATE);
        String skEnd = "MILKING#" + endDate.format(DateTimeFormatter.ISO_DATE);
        
        Optional<List<MilkingRecord>> milkingsOpt = milkingRepository.getMilkingBetweenDates(pk, skStart, skEnd);
        
        if (milkingsOpt.isEmpty() || milkingsOpt.get().isEmpty()) {
            log.info("No milking records found for farmId: {}", farmId);
            return 0.0;
        }
        
        List<MilkingRecord> milkingRecords = milkingsOpt.get();
        double totalLiters = milkingRecords.stream()
                .filter(m -> m.getLiters() != null)
                .mapToDouble(MilkingRecord::getLiters)
                .sum();
        
        return milkingRecords.isEmpty() ? 0.0 : totalLiters / milkingRecords.size();
    }
    
    /**
     * Obtiene el bovino con mayor producción
     */
    public MilkingContextDTO getTopProducerBovine(String farmId) throws RepositoryException {
        log.info("Getting top producer bovine for farmId: {}", farmId);
        
        // Calcular fechas del último mes
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        
        String pk = "FARM#" + farmId;
        String skStart = "MILKING#" + startDate.format(DateTimeFormatter.ISO_DATE);
        String skEnd = "MILKING#" + endDate.format(DateTimeFormatter.ISO_DATE);
        
        Optional<List<MilkingRecord>> milkingsOpt = milkingRepository.getMilkingBetweenDates(pk, skStart, skEnd);
        
        if (milkingsOpt.isEmpty() || milkingsOpt.get().isEmpty()) {
            log.info("No milking records found for farmId: {}", farmId);
            return null;
        }
        
        // Agrupar por bovineId y sumar producción
        Map<Integer, Double> productionByBovine = milkingsOpt.get().stream()
                .filter(m -> m.getBovineId() != null && m.getLiters() != null)
                .collect(Collectors.groupingBy(
                        MilkingRecord::getBovineId,
                        Collectors.summingDouble(MilkingRecord::getLiters)
                ));
        
        if (productionByBovine.isEmpty()) {
            return null;
        }
        
        // Encontrar el bovino con mayor producción
        Map.Entry<Integer, Double> topProducer = productionByBovine.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        if (topProducer == null) {
            return null;
        }
        
        // Calcular promedio de la finca para comparación
        double averageProduction = getMonthlyAverageProduction(farmId);
        
        // Obtener el registro más reciente del bovino top
        MilkingRecord recentMilkingRecord = milkingsOpt.get().stream()
                .filter(m -> m.getBovineId().equals(topProducer.getKey()))
                .max(Comparator.comparing(MilkingRecord::getDate))
                .orElse(null);
        
        if (recentMilkingRecord == null) {
            return null;
        }
        
        return toMilkingContextDTO(recentMilkingRecord, averageProduction);
    }
    
    /**
     * Obtiene la producción por turno (mañana/tarde)
     */
    public Map<String, Double> getProductionByShift(String farmId) throws RepositoryException {
        log.info("Getting production by shift for farmId: {}", farmId);
        
        // Calcular fechas del último mes
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        
        String pk = "FARM#" + farmId;
        String skStart = "MILKING#" + startDate.format(DateTimeFormatter.ISO_DATE);
        String skEnd = "MILKING#" + endDate.format(DateTimeFormatter.ISO_DATE);
        
        Optional<List<MilkingRecord>> milkingsOpt = milkingRepository.getMilkingBetweenDates(pk, skStart, skEnd);
        
        if (milkingsOpt.isEmpty() || milkingsOpt.get().isEmpty()) {
            log.info("No milking records found for farmId: {}", farmId);
            return new HashMap<>();
        }
        
        return milkingsOpt.get().stream()
                .filter(m -> m.getShift() != null && m.getLiters() != null)
                .collect(Collectors.groupingBy(
                        MilkingRecord::getShift,
                        Collectors.summingDouble(MilkingRecord::getLiters)
                ));
    }
    
    /**
     * Obtiene los registros de ordeño recientes
     */
    public List<MilkingContextDTO> getRecentMilkings(String farmId, int days) throws RepositoryException {
        log.info("Getting recent milkings for farmId: {} (last {} days)", farmId, days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        String pk = "FARM#" + farmId;
        String skStart = "MILKING#" + startDate.format(DateTimeFormatter.ISO_DATE);
        String skEnd = "MILKING#" + endDate.format(DateTimeFormatter.ISO_DATE);
        
        Optional<List<MilkingRecord>> milkingsOpt = milkingRepository.getMilkingBetweenDates(pk, skStart, skEnd);
        
        if (milkingsOpt.isEmpty() || milkingsOpt.get().isEmpty()) {
            log.info("No milking records found for farmId: {}", farmId);
            return new ArrayList<>();
        }
        
        // Calcular promedio para comparación
        double averageProduction = getMonthlyAverageProduction(farmId);
        
        return milkingsOpt.get().stream()
                .map(m -> toMilkingContextDTO(m, averageProduction))
            .sorted(Comparator.comparing(
                MilkingContextDTO::getMilkingDate,
                Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Convierte FarmMilking a MilkingContextDTO
     */
    private MilkingContextDTO toMilkingContextDTO(MilkingRecord milkingRecord, double averageProduction) {
        LocalDate milkingDate = null;
        LocalTime milkingTime = null;
        
        if (milkingRecord.getDate() != null) {
            try {
                milkingDate = LocalDate.parse(milkingRecord.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.error("Error parsing milking date: {}", milkingRecord.getDate(), e);
            }
        }
        
        // Asignar hora aproximada según turno
        if (milkingRecord.getShift() != null) {
            if ("AM".equalsIgnoreCase(milkingRecord.getShift())) {
                milkingTime = LocalTime.of(6, 0);
            } else if ("PM".equalsIgnoreCase(milkingRecord.getShift())) {
                milkingTime = LocalTime.of(18, 0);
            }
        }
        
        return MilkingContextDTO.builder()
                .milkingId(milkingRecord.getPK() + "#" + milkingRecord.getSK())
                .bovineId(String.valueOf(milkingRecord.getBovineId()))
                .bovineName("Bovino " + milkingRecord.getBovineId())
                .milkingDate(milkingDate)
                .milkingTime(milkingTime)
                .shift(milkingRecord.getShift())
                .litersMilked(milkingRecord.getLiters())
                .averageProduction(averageProduction)
                .build();
    }
}
