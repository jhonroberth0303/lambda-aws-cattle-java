package com.cattle.tasks.planner;

import com.cattle.tasks.FollowUpColor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Color del semáforo post-servicio, calculado al leer para que avance cada día sin
 * depender del batch (CA20). La marca de repetidora tiene prioridad sobre los días (CA21).
 */
public final class ServiceFollowUpEvaluator {

    private ServiceFollowUpEvaluator() {
    }

    public record Evaluation(FollowUpColor color, long daysSinceService, boolean repeatBreeder) {
    }

    public static Evaluation evaluate(LocalDate lastServiceDate, int serviceNumber, LocalDate today,
                                      ReproductiveTaskSettings settings) {
        long days = Math.max(0, ChronoUnit.DAYS.between(lastServiceDate, today));
        boolean repeatBreeder = serviceNumber >= settings.repeatBreederServices();
        FollowUpColor color;
        if (repeatBreeder || days >= settings.followUpRedDays()) {
            color = FollowUpColor.RED;
        } else if (days >= settings.followUpOrangeDays()) {
            color = FollowUpColor.ORANGE;
        } else {
            color = FollowUpColor.GREEN;
        }
        return new Evaluation(color, days, repeatBreeder);
    }
}
