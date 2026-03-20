package com.cattle.services;

import com.cattle.enums.profiles.AlertType;
import com.cattle.enums.profiles.ProductiveState;
import com.cattle.enums.profiles.ReproductiveState;
import com.cattle.services.ProductiveStateCalculator.ProductiveStateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ProductiveStateCalculator.
 * Verifica el cálculo correcto de estados reproductivos, productivos y alertas.
 */
@DisplayName("ProductiveStateCalculator Tests")
class ProductiveStateCalculatorTest {

    private ProductiveStateCalculator calculator;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        calculator = new ProductiveStateCalculator();
        today = LocalDate.of(2026, 2, 3); // Fecha fija para tests reproducibles
    }

    // ==================== ReproductiveState Tests ====================

    @Nested
    @DisplayName("calculateReproductiveState()")
    class ReproductiveStateTests {

        @Test
        @DisplayName("Debe retornar OPEN cuando no hay preñez activa")
        void noPregnancy_returnsOpen() {
            ReproductiveState result = calculator.calculateReproductiveState(
                false, null, null, null, today
            );
            assertEquals(ReproductiveState.OPEN, result);
        }

        @Test
        @DisplayName("Debe retornar OPEN cuando isPregnant es null")
        void nullPregnancy_returnsOpen() {
            ReproductiveState result = calculator.calculateReproductiveState(
                null, "ACTIVE", "2026-05-01", null, today
            );
            assertEquals(ReproductiveState.OPEN, result);
        }

        @Test
        @DisplayName("Debe retornar PREGNANT cuando hay preñez activa con más de 21 días")
        void activePregnancy_moreThan21Days_returnsPregnant() {
            String dueDate = today.plusDays(60).toString(); // 60 días adelante
            ReproductiveState result = calculator.calculateReproductiveState(
                true, "ACTIVE", dueDate, null, today
            );
            assertEquals(ReproductiveState.PREGNANT, result);
        }

        @Test
        @DisplayName("Debe retornar PRE_PARTO cuando faltan 21 días o menos")
        void activePregnancy_21DaysOrLess_returnsPreParto() {
            String dueDate = today.plusDays(21).toString();
            ReproductiveState result = calculator.calculateReproductiveState(
                true, "ACTIVE", dueDate, null, today
            );
            assertEquals(ReproductiveState.PRE_PARTO, result);
        }

        @Test
        @DisplayName("Debe retornar PRE_PARTO cuando faltan 5 días")
        void activePregnancy_5Days_returnsPreParto() {
            String dueDate = today.plusDays(5).toString();
            ReproductiveState result = calculator.calculateReproductiveState(
                true, "ACTIVE", dueDate, null, today
            );
            assertEquals(ReproductiveState.PRE_PARTO, result);
        }

        @Test
        @DisplayName("Debe retornar POST_PARTO cuando parió hace 30 días o menos")
        void recentCalving_returnsPostParto() {
            String calvingDate = today.minusDays(15).toString();
            ReproductiveState result = calculator.calculateReproductiveState(
                false, "CLOSED", null, calvingDate, today
            );
            assertEquals(ReproductiveState.POST_PARTO, result);
        }

        @Test
        @DisplayName("Debe retornar OPEN cuando parió hace más de 30 días")
        void oldCalving_returnsOpen() {
            String calvingDate = today.minusDays(45).toString();
            ReproductiveState result = calculator.calculateReproductiveState(
                false, "CLOSED", null, calvingDate, today
            );
            assertEquals(ReproductiveState.OPEN, result);
        }
    }

    // ==================== ProductiveState Tests ====================

    @Nested
    @DisplayName("calculateProductiveState()")
    class ProductiveStateTests {

        @Test
        @DisplayName("OPEN + sin lactancia = OPEN")
        void open_noLactation_returnsOpen() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.OPEN, null
            );
            assertEquals(ProductiveState.OPEN, result);
        }

        @Test
        @DisplayName("OPEN + LACTATING = OPEN_LACTATING")
        void open_lactating_returnsOpenLactating() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.OPEN, "LACTATING"
            );
            assertEquals(ProductiveState.OPEN_LACTATING, result);
        }

        @Test
        @DisplayName("OPEN + DRY = DRY")
        void open_dry_returnsDry() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.OPEN, "DRY"
            );
            assertEquals(ProductiveState.DRY, result);
        }

        @Test
        @DisplayName("PREGNANT + sin lactancia = PREGNANT")
        void pregnant_noLactation_returnsPregnant() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.PREGNANT, null
            );
            assertEquals(ProductiveState.PREGNANT, result);
        }

        @Test
        @DisplayName("PREGNANT + LACTATING = PREGNANT_LACTATING")
        void pregnant_lactating_returnsPregnantLactating() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.PREGNANT, "LACTATING"
            );
            assertEquals(ProductiveState.PREGNANT_LACTATING, result);
        }

        @Test
        @DisplayName("PREGNANT + DRY = PREGNANT_DRY")
        void pregnant_dry_returnsPregnantDry() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.PREGNANT, "DRY"
            );
            assertEquals(ProductiveState.PREGNANT_DRY, result);
        }

        @Test
        @DisplayName("PRE_PARTO siempre retorna PRE_PARTO")
        void preParto_alwaysReturnsPreParto() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.PRE_PARTO, "LACTATING"
            );
            assertEquals(ProductiveState.PRE_PARTO, result);
        }

        @Test
        @DisplayName("POST_PARTO siempre retorna POST_PARTO")
        void postParto_alwaysReturnsPostParto() {
            ProductiveState result = calculator.calculateProductiveState(
                ReproductiveState.POST_PARTO, "LACTATING"
            );
            assertEquals(ProductiveState.POST_PARTO, result);
        }
    }

    // ==================== Alerts Tests ====================

    @Nested
    @DisplayName("calculateAlerts()")
    class AlertTests {

        @Test
        @DisplayName("Debe generar alerta OVERDUE cuando el parto está atrasado")
        void overdue_generatesOverdueAlert() {
            String pastDueDate = today.minusDays(3).toString();
            List<AlertType> alerts = calculator.calculateAlerts(
                true, pastDueDate, null, null, ReproductiveState.PREGNANT, today
            );
            assertTrue(alerts.contains(AlertType.OVERDUE));
        }

        @Test
        @DisplayName("Debe generar alerta PREPARTUM cuando faltan 21 días o menos")
        void prepartum_generatesPrepartumAlert() {
            String dueDate = today.plusDays(15).toString();
            List<AlertType> alerts = calculator.calculateAlerts(
                true, dueDate, null, null, ReproductiveState.PRE_PARTO, today
            );
            assertTrue(alerts.contains(AlertType.PREPARTUM));
        }

        @Test
        @DisplayName("Debe generar alerta DRY_OFF_SOON cuando está lactando y faltan 60 días o menos")
        void dryOffSoon_generatesDryOffAlert() {
            String dueDate = today.plusDays(45).toString();
            List<AlertType> alerts = calculator.calculateAlerts(
                true, dueDate, null, "LACTATING", ReproductiveState.PREGNANT, today
            );
            assertTrue(alerts.contains(AlertType.DRY_OFF_SOON));
        }

        @Test
        @DisplayName("No debe generar DRY_OFF_SOON cuando ya está seca")
        void alreadyDry_noDryOffAlert() {
            String dueDate = today.plusDays(45).toString();
            List<AlertType> alerts = calculator.calculateAlerts(
                true, dueDate, null, "DRY", ReproductiveState.PREGNANT, today
            );
            assertFalse(alerts.contains(AlertType.DRY_OFF_SOON));
        }

        @Test
        @DisplayName("Debe generar alerta POSTPARTUM en los primeros 15 días")
        void recentCalving_generatesPostpartumAlert() {
            String calvingDate = today.minusDays(10).toString();
            List<AlertType> alerts = calculator.calculateAlerts(
                false, null, calvingDate, "LACTATING", ReproductiveState.POST_PARTO, today
            );
            assertTrue(alerts.contains(AlertType.POSTPARTUM));
        }

        @Test
        @DisplayName("Debe generar alerta HEAT_WATCH cuando OPEN y +45 días postparto")
        void openAfter45Days_generatesHeatWatchAlert() {
            String calvingDate = today.minusDays(50).toString();
            List<AlertType> alerts = calculator.calculateAlerts(
                false, null, calvingDate, "LACTATING", ReproductiveState.OPEN, today
            );
            assertTrue(alerts.contains(AlertType.HEAT_WATCH));
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("calculate() - Integration")
    class CalculateIntegrationTests {

        @Test
        @DisplayName("Vaca preñada lactando cerca del parto genera resultado correcto")
        void pregnantLactatingNearDue_fullCalculation() {
            String dueDate = today.plusDays(50).toString();
            String lactationStart = today.minusDays(200).toString();

            ProductiveStateResult result = calculator.calculate(
                true, "ACTIVE", dueDate, null,
                "LACTATING", lactationStart, today
            );

            assertEquals(ReproductiveState.PREGNANT, result.reproductiveState());
            assertEquals(ProductiveState.PREGNANT_LACTATING, result.productiveState());
            assertEquals(50, result.daysUntilDue());
            assertEquals(200, result.daysInLactation());
            assertTrue(result.alerts().contains(AlertType.DRY_OFF_SOON));
        }

        @Test
        @DisplayName("Vaca postparto lactando genera resultado correcto")
        void postpartumLactating_fullCalculation() {
            String calvingDate = today.minusDays(10).toString();
            String lactationStart = today.minusDays(10).toString();

            ProductiveStateResult result = calculator.calculate(
                false, "CLOSED", null, calvingDate,
                "LACTATING", lactationStart, today
            );

            assertEquals(ReproductiveState.POST_PARTO, result.reproductiveState());
            assertEquals(ProductiveState.POST_PARTO, result.productiveState());
            assertNull(result.daysUntilDue());
            assertEquals(10, result.daysInLactation());
            assertEquals(10, result.daysSinceCalving());
            assertTrue(result.alerts().contains(AlertType.POSTPARTUM));
        }

        @Test
        @DisplayName("Vaca en pre-parto genera resultado correcto")
        void preParto_fullCalculation() {
            String dueDate = today.plusDays(10).toString();

            ProductiveStateResult result = calculator.calculate(
                true, "ACTIVE", dueDate, null,
                "DRY", null, today
            );

            assertEquals(ReproductiveState.PRE_PARTO, result.reproductiveState());
            assertEquals(ProductiveState.PRE_PARTO, result.productiveState());
            assertEquals(10, result.daysUntilDue());
            assertTrue(result.alerts().contains(AlertType.PREPARTUM));
        }

        @Test
        @DisplayName("alertsAsStrings retorna lista de strings")
        void alertsAsStrings_returnsStringList() {
            String dueDate = today.plusDays(10).toString();

            ProductiveStateResult result = calculator.calculate(
                true, "ACTIVE", dueDate, null,
                "LACTATING", null, today
            );

            List<String> alertStrings = result.alertsAsStrings();
            assertTrue(alertStrings.contains("PREPARTUM"));
            assertTrue(alertStrings.contains("DRY_OFF_SOON"));
        }
    }
}
