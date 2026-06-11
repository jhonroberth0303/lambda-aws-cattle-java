package com.cattle.utils;

import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import com.cattle.enums.PlanType;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

public class EtaCalculator {

    /**
     * Calcula etaOpenDays para cualquier plan.
     * growthRateCmPerDay: tasa de crecimiento estimada (cm/día). Si es <=0, se ignora la ruta por altura.
     * now: fecha actual (UTC) para derivaciones como daysSinceLastCut o daysAfterSowing si se proveen fechas.
     */
    public static int etaOpenDays(Pasture p, Plan plan) {
        Objects.requireNonNull(plan, "plan requerido");

        switch (PlanType.valueOf(plan.getPlanType())) {
            case PlanType.GRAZING:
                return etaGrazing(p, plan);
            case PlanType.CUT:
                return etaCut(p, plan);
            case PlanType.ANNUAL_SILAGE:
                return etaSilage(p, plan);
            default:
                return 0;
        }
    }

    /** ETA para pastoreo: min( por_días, por_altura ) */
    private static int etaGrazing(Pasture pasture, Plan plan) {

        int etaPorDias = Integer.MAX_VALUE;
        if (plan.getRules().getRestDaysMin() != null && pasture.getLastUseAt() != null) {
            LocalDate lastUse = Dates.parseDate(pasture.getLastUseAt());
            if (lastUse != null) {
                int daysRest = (int) Dates.daysBetween(lastUse, LocalDate.now(ZoneOffset.UTC));
                etaPorDias = Math.max(0, plan.getRules().getRestDaysMin() - daysRest);
            }
        }

        int etaPorAltura = Integer.MAX_VALUE;
        if (plan.getRules().getEntryHeightCm() != null && pasture.getCurrentHeightCm() != null && plan.getGrowthRateCmPerDay() > 0.0) {
            int gap = Math.max(0, plan.getRules().getEntryHeightCm() - pasture.getCurrentHeightCm());
            etaPorAltura = (int) Math.ceil(gap / plan.getGrowthRateCmPerDay());
        }

        int eta = Math.min(etaPorDias, etaPorAltura);
        return eta == Integer.MAX_VALUE ? 0 : eta;
    }

    /** ETA para corte: min( por_intervalo, por_altura ) */
    private static int etaCut(Pasture pasture, Plan plan) {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        int etaIntervalo = Integer.MAX_VALUE;
        if (plan.getRules().getCutIntervalDays() != null && pasture.getLastUseAt() != null) {
            LocalDate last = Dates.parseDate(pasture.getLastUseAt());
            if (last != null) {
                int daysSinceLastCut = (int) Dates.daysBetween(last, now);
                etaIntervalo = Math.max(0, plan.getRules().getCutIntervalDays() - daysSinceLastCut);
            }
        }

        int etaAltura = Integer.MAX_VALUE;
        if (plan.getRules().getEntryHeightCm() != null && pasture.getCurrentHeightCm() != null && plan.getGrowthRateCmPerDay() > 0.0) {
            int gap = Math.max(0, plan.getRules().getEntryHeightCm() - pasture.getCurrentHeightCm());
            etaAltura = (int) Math.ceil(gap / plan.getGrowthRateCmPerDay());
        }

        int eta = Math.min(etaIntervalo, etaAltura);
        if (eta == Integer.MAX_VALUE) eta = 0;
        return eta;
    }

    /** ETA para ensilaje (maíz): por días a cosecha */
    private static int etaSilage(Pasture p, Plan plan) {
        Integer harvestDays = plan.getRules().getHarvestDaysAfterSowing();
        if (harvestDays == null || p.getEstablishmentDate() == null) return 0;

        LocalDate sow = Dates.parseDate(p.getEstablishmentDate());
        if (sow == null) return 0;

        int daysSinceSowing = (int) Dates.daysBetween(sow, LocalDate.now(ZoneOffset.UTC));
        return Math.max(0, harvestDays - daysSinceSowing);
    }
}