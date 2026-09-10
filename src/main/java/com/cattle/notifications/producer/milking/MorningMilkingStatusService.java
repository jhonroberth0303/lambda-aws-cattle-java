package com.cattle.notifications.producer.milking;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.CowWithLactationsDTO;
import com.cattle.entities.MilkingRecord;
import com.cattle.enums.LogType;
import com.cattle.processor.MilkingProcessor;
import com.cattle.services.MilkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador entre el dominio de ordeño y las notificaciones: responde
 * "¿cuántas vacas lactantes no tienen registrado el ordeño AM de esta fecha?".
 * <p>
 * Encapsula el conocimiento de claves y turnos del módulo de ordeño para que el
 * productor no dependa de esos detalles.
 */
@Service
@RequiredArgsConstructor
public class MorningMilkingStatusService {

    private static final String SHIFT_AM = "AM";
    private static final String BOVINE_PK_PREFIX = "BOVINE#";

    private final MilkingProcessor milkingProcessor;
    private final MilkingService milkingService;
    private final LambdaContext lambdaContext;

    public MorningMilkingStatus statusFor(String siteId, LocalDate date) {
        List<CowWithLactationsDTO> cows = milkingProcessor.getCowsWithLactations(siteId).orElseGet(List::of);
        String isoDate = date.toString();

        int pending = 0;
        for (CowWithLactationsDTO cow : cows) {
            if (!hasMorningRecord(cow.getBovineId(), isoDate)) {
                pending++;
            }
        }

        lambdaContext.logInfo(LogType.SERVICE, "Morning milking status. siteId=" + siteId + ", date=" + isoDate
                + ", lactatingCows=" + cows.size() + ", pendingCows=" + pending);
        return new MorningMilkingStatus(siteId, cows.size(), pending);
    }

    private boolean hasMorningRecord(Integer bovineId, String isoDate) {
        if (bovineId == null) {
            return false;
        }
        return milkingService.getMilkingByPk(BOVINE_PK_PREFIX + bovineId).orElseGet(List::of).stream()
                .anyMatch(record -> isoDate.equals(record.getDate()) && isMorning(record));
    }

    private boolean isMorning(MilkingRecord record) {
        return record.getShift() != null && SHIFT_AM.equalsIgnoreCase(record.getShift().trim());
    }
}
