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
}