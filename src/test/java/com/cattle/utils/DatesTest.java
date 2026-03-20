package com.cattle.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Dates utility
 * HU-002-pruebas-summary - Aumento de cobertura
 */
@Tag("unit")
@Tag("fast")
@Tag("utils")
@DisplayName("Dates Utility Tests")
class DatesTest {

    @Nested
    @DisplayName("parseDate Tests")
    class ParseDateTests {

        @Test
        @DisplayName("Debe parsear fecha ISO válida")
        void parseDate_validDate_returnsLocalDate() {
            // Act
            LocalDate result = Dates.parseDate("2024-03-15");

            // Assert
            assertNotNull(result);
            assertEquals(2024, result.getYear());
            assertEquals(3, result.getMonthValue());
            assertEquals(15, result.getDayOfMonth());
        }

        @Test
        @DisplayName("Debe retornar null para string null")
        void parseDate_nullInput_returnsNull() {
            // Act
            LocalDate result = Dates.parseDate(null);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Debe retornar null para string vacío")
        void parseDate_emptyString_returnsNull() {
            // Act
            LocalDate result = Dates.parseDate("");

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Debe retornar null para string en blanco")
        void parseDate_blankString_returnsNull() {
            // Act
            LocalDate result = Dates.parseDate("   ");

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Debe retornar null para formato con longitud incorrecta")
        void parseDate_wrongLength_returnsNull() {
            // Act
            LocalDate result = Dates.parseDate("24-03-15");

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("parseInstant Tests")
    class ParseInstantTests {

        @Test
        @DisplayName("Debe parsear fecha a Instant")
        void parseInstant_validDate_returnsInstant() {
            // Act
            Instant result = Dates.parseInstant("2024-03-15");

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Debe retornar null para string null")
        void parseInstant_nullInput_returnsNull() {
            // Act
            Instant result = Dates.parseInstant(null);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Debe retornar null para string vacío")
        void parseInstant_emptyString_returnsNull() {
            // Act
            Instant result = Dates.parseInstant("");

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Debe retornar null para formato con longitud incorrecta")
        void parseInstant_wrongLength_returnsNull() {
            // Act
            Instant result = Dates.parseInstant("24-03-15");

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("daysBetween Tests")
    class DaysBetweenTests {

        @Test
        @DisplayName("Debe calcular días entre dos fechas")
        void daysBetween_twoDates_returnsCorrectDays() {
            // Arrange
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 1, 11);

            // Act
            long result = Dates.daysBetween(from, to);

            // Assert
            assertEquals(10, result);
        }

        @Test
        @DisplayName("Debe retornar 0 para misma fecha")
        void daysBetween_sameDate_returnsZero() {
            // Arrange
            LocalDate date = LocalDate.of(2024, 3, 15);

            // Act
            long result = Dates.daysBetween(date, date);

            // Assert
            assertEquals(0, result);
        }

        @Test
        @DisplayName("Debe retornar negativo para fechas invertidas")
        void daysBetween_reversedDates_returnsNegative() {
            // Arrange
            LocalDate from = LocalDate.of(2024, 1, 11);
            LocalDate to = LocalDate.of(2024, 1, 1);

            // Act
            long result = Dates.daysBetween(from, to);

            // Assert
            assertEquals(-10, result);
        }
    }

    @Nested
    @DisplayName("isoDateTimeUTC Tests")
    class IsoDateTimeUtcTests {

        @Test
        @DisplayName("Debe formatear Instant a ISO string")
        void isoDateTimeUTC_validInstant_returnsIsoString() {
            // Arrange
            Instant instant = Instant.parse("2024-03-15T10:30:00Z");

            // Act
            String result = Dates.isoDateTimeUTC(instant);

            // Assert
            assertNotNull(result);
            assertEquals("2024-03-15T10:30:00Z", result);
        }

        @Test
        @DisplayName("Debe mantener precisión de milisegundos")
        void isoDateTimeUTC_withMillis_returnsIsoString() {
            // Arrange
            Instant instant = Instant.parse("2024-03-15T10:30:00.123Z");

            // Act
            String result = Dates.isoDateTimeUTC(instant);

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("123"));
        }
    }
}
