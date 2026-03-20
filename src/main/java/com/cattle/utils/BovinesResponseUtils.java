package com.cattle.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class BovinesResponseUtils {

    public static String getAge(String bornDate) {
        LocalDate nacimiento = LocalDate.parse(bornDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate hoy = LocalDate.now();

        Period periodo = Period.between(nacimiento, hoy);

        return String.format("%da, %dm, %dd", periodo.getYears(), periodo.getMonths(), periodo.getDays());
    }
}
