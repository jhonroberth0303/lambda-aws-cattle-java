package com.cattle.utils;

import com.cattle.builders.PastureBuilder;
import com.cattle.builders.PlanBuilder;
import com.cattle.entities.Pasture;
import com.cattle.entities.Plan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class EtaCalculatorTest {

    @Test
    void testEtaOpenDaysRyegrass() {
        Pasture pasture = PastureBuilder.buildPastureRyegrass();
        pasture.setLastUseAt(LocalDate.now(ZoneOffset.UTC).minusDays(10).toString());
        Plan plan = PlanBuilder.buildPlanRyegrassFromJson();

        int result = EtaCalculator.etaOpenDays(pasture, plan);

        assertEquals(25, result);
    }

    @Test
    void testEtaOpenDaysCuba22() {
        Pasture pasture = PastureBuilder.buildPastureCuba22FromJson();
        pasture.setLastUseAt(LocalDate.now(ZoneOffset.UTC).minusDays(10).toString());
        Plan plan = PlanBuilder.buildPlanCuba22FromJson();

        int result = EtaCalculator.etaOpenDays(pasture, plan);

        assertEquals(50, result);
    }


    @Test
    void testEtaOpenDaysKikuyo() {
        Pasture pasture = PastureBuilder.buildPastureKikuyoFromJson();
        pasture.setLastUseAt(LocalDate.now(ZoneOffset.UTC).minusDays(10).toString());
        Plan plan = PlanBuilder.buildPlanKikuyoFromJson();

        int result = EtaCalculator.etaOpenDays(pasture, plan);

        assertEquals(18, result);
    }

    // ==================== ANNUAL_SILAGE ====================

    private Plan silagePlan(Integer harvestDaysAfterSowing) {
        return Plan.builder()
                .planType("ANNUAL_SILAGE")
                .species("MAIZE")
                .growthRateCmPerDay(0.0)
                .rules(Plan.Rules.builder().harvestDaysAfterSowing(harvestDaysAfterSowing).build())
                .build();
    }

    private Pasture pastureWithEstablishment(String establishmentDate) {
        Pasture pasture = new Pasture();
        pasture.setEstablishmentDate(establishmentDate);
        return pasture;
    }

    @Test
    void etaSilage_remainingDaysUntilHarvest() {
        Pasture pasture = pastureWithEstablishment(LocalDate.now(ZoneOffset.UTC).minusDays(30).toString());

        int result = EtaCalculator.etaOpenDays(pasture, silagePlan(120));

        assertEquals(90, result);
    }

    @Test
    void etaSilage_pastHarvestDate_clampsToZero() {
        Pasture pasture = pastureWithEstablishment(LocalDate.now(ZoneOffset.UTC).minusDays(200).toString());

        assertEquals(0, EtaCalculator.etaOpenDays(pasture, silagePlan(120)));
    }

    @Test
    void etaSilage_missingHarvestDays_returnsZero() {
        Pasture pasture = pastureWithEstablishment(LocalDate.now(ZoneOffset.UTC).minusDays(10).toString());

        assertEquals(0, EtaCalculator.etaOpenDays(pasture, silagePlan(null)));
    }

    @Test
    void etaSilage_missingEstablishmentDate_returnsZero() {
        assertEquals(0, EtaCalculator.etaOpenDays(pastureWithEstablishment(null), silagePlan(120)));
    }

    @Test
    void etaSilage_unparseableEstablishmentDate_returnsZero() {
        assertEquals(0, EtaCalculator.etaOpenDays(pastureWithEstablishment("2026/01"), silagePlan(120)));
    }
}