package com.cattle.enums.profiles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para los enums de estados productivos.
 */
@Tag("unit")
@Tag("fast")
@DisplayName("Productive States Enums Tests")
class ProductiveStatesEnumsTest {

    @Nested
    @DisplayName("ReproductiveState Enum")
    class ReproductiveStateTests {

        @Test
        @DisplayName("Debe tener 4 valores")
        void shouldHave4Values() {
            assertEquals(4, ReproductiveState.values().length);
        }

        @Test
        @DisplayName("Debe contener OPEN, PREGNANT, PRE_PARTO, POST_PARTO")
        void shouldContainExpectedValues() {
            assertNotNull(ReproductiveState.valueOf("OPEN"));
            assertNotNull(ReproductiveState.valueOf("PREGNANT"));
            assertNotNull(ReproductiveState.valueOf("PRE_PARTO"));
            assertNotNull(ReproductiveState.valueOf("POST_PARTO"));
        }

        @Test
        @DisplayName("name() debe retornar el nombre correcto")
        void nameShouldReturnCorrectValue() {
            assertEquals("OPEN", ReproductiveState.OPEN.name());
            assertEquals("PREGNANT", ReproductiveState.PREGNANT.name());
            assertEquals("PRE_PARTO", ReproductiveState.PRE_PARTO.name());
            assertEquals("POST_PARTO", ReproductiveState.POST_PARTO.name());
        }
    }

    @Nested
    @DisplayName("ProductiveState Enum")
    class ProductiveStateTests {

        @Test
        @DisplayName("Debe tener 8 valores")
        void shouldHave8Values() {
            assertEquals(8, ProductiveState.values().length);
        }

        @Test
        @DisplayName("Debe contener todos los estados combinados")
        void shouldContainExpectedValues() {
            assertNotNull(ProductiveState.valueOf("OPEN"));
            assertNotNull(ProductiveState.valueOf("OPEN_LACTATING"));
            assertNotNull(ProductiveState.valueOf("PREGNANT"));
            assertNotNull(ProductiveState.valueOf("PREGNANT_LACTATING"));
            assertNotNull(ProductiveState.valueOf("PREGNANT_DRY"));
            assertNotNull(ProductiveState.valueOf("PRE_PARTO"));
            assertNotNull(ProductiveState.valueOf("POST_PARTO"));
            assertNotNull(ProductiveState.valueOf("DRY"));
        }
    }

    @Nested
    @DisplayName("LactationStatus Enum")
    class LactationStatusTests {

        @Test
        @DisplayName("Debe tener 3 valores")
        void shouldHave3Values() {
            assertEquals(3, LactationStatus.values().length);
        }

        @Test
        @DisplayName("Debe contener LACTATING, DRY, CLOSED")
        void shouldContainExpectedValues() {
            assertNotNull(LactationStatus.valueOf("LACTATING"));
            assertNotNull(LactationStatus.valueOf("DRY"));
            assertNotNull(LactationStatus.valueOf("CLOSED"));
        }
    }

    @Nested
    @DisplayName("AlertType Enum")
    class AlertTypeTests {

        @Test
        @DisplayName("Debe tener 5 tipos de alertas")
        void shouldHave5Values() {
            assertEquals(5, AlertType.values().length);
        }

        @Test
        @DisplayName("Debe contener todos los tipos de alerta")
        void shouldContainExpectedValues() {
            assertNotNull(AlertType.valueOf("PREPARTUM"));
            assertNotNull(AlertType.valueOf("POSTPARTUM"));
            assertNotNull(AlertType.valueOf("DRY_OFF_SOON"));
            assertNotNull(AlertType.valueOf("OVERDUE"));
            assertNotNull(AlertType.valueOf("HEAT_WATCH"));
        }
    }
}
