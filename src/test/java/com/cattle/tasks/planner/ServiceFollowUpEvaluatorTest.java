package com.cattle.tasks.planner;

import com.cattle.tasks.FollowUpColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@Tag("fast")
@DisplayName("ServiceFollowUpEvaluator Tests")
class ServiceFollowUpEvaluatorTest {

    private static final ReproductiveTaskSettings SETTINGS = ReproductiveTaskSettings.defaults();
    private static final LocalDate SERVICE = LocalDate.parse("2026-10-01");

    @ParameterizedTest(name = "día {0} → {1}")
    @CsvSource({"0,GREEN", "10,GREEN", "41,GREEN", "42,ORANGE", "45,ORANGE", "62,ORANGE", "63,RED", "70,RED"})
    @DisplayName("CA20: fronteras del semáforo por días desde el servicio")
    void colorByDays(int days, FollowUpColor expected) {
        ServiceFollowUpEvaluator.Evaluation evaluation =
                ServiceFollowUpEvaluator.evaluate(SERVICE, 1, SERVICE.plusDays(days), SETTINGS);

        assertThat(evaluation.color()).isEqualTo(expected);
        assertThat(evaluation.daysSinceService()).isEqualTo(days);
        assertThat(evaluation.repeatBreeder()).isFalse();
    }

    @Test
    @DisplayName("CA21: repetidora queda en rojo aunque esté en día 0")
    void repeatBreeder_isRedOnDayZero() {
        ServiceFollowUpEvaluator.Evaluation evaluation =
                ServiceFollowUpEvaluator.evaluate(SERVICE, 3, SERVICE, SETTINGS);

        assertThat(evaluation.color()).isEqualTo(FollowUpColor.RED);
        assertThat(evaluation.repeatBreeder()).isTrue();
    }
}
