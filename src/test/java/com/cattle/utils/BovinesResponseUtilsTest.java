package com.cattle.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BovinesResponseUtils
 * HU-20260428-deuda-tecnica-summary - Alineacion de cobertura y trazabilidad
 */
@Tag("unit")
@Tag("fast")
@Tag("utils")
@DisplayName("BovinesResponseUtils Tests")
class BovinesResponseUtilsTest {

    @Test
    @DisplayName("Debe calcular edad correctamente para bovino de 2 años")
    void getAge_twoYearsOld_returnsCorrectAge() {
        // Arrange
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        String bornDate = twoYearsAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Act
        String age = BovinesResponseUtils.getAge(bornDate);

        // Assert
        assertTrue(age.startsWith("2a"));
        assertTrue(age.contains("0m"));
        assertTrue(age.contains("0d"));
    }

    @Test
    @DisplayName("Debe calcular edad correctamente para bovino de 6 meses")
    void getAge_sixMonthsOld_returnsCorrectAge() {
        // Arrange
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        String bornDate = sixMonthsAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Act
        String age = BovinesResponseUtils.getAge(bornDate);

        // Assert
        assertTrue(age.startsWith("0a"));
        assertTrue(age.contains("6m"));
    }

    @Test
    @DisplayName("Debe calcular edad correctamente para bovino nacido hoy")
    void getAge_bornToday_returnsZeroAge() {
        // Arrange
        String bornDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Act
        String age = BovinesResponseUtils.getAge(bornDate);

        // Assert
        assertEquals("0a, 0m, 0d", age);
    }

    @Test
    @DisplayName("Debe calcular edad con años, meses y días")
    void getAge_complexAge_returnsCorrectFormat() {
        // Arrange
        LocalDate bornDate = LocalDate.now().minusYears(3).minusMonths(5).minusDays(10);
        String bornDateStr = bornDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Act
        String age = BovinesResponseUtils.getAge(bornDateStr);

        // Assert
        assertNotNull(age);
        assertTrue(age.matches("\\d+a, \\d+m, \\d+d"));
    }

    @Test
    @DisplayName("Debe manejar fecha de bovino muy viejo")
    void getAge_veryOld_returnsCorrectAge() {
        // Arrange
        LocalDate tenYearsAgo = LocalDate.now().minusYears(10);
        String bornDate = tenYearsAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Act
        String age = BovinesResponseUtils.getAge(bornDate);

        // Assert
        assertTrue(age.startsWith("10a"));
    }
}
