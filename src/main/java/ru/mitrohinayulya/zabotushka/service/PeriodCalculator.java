package ru.mitrohinayulya.zabotushka.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/// Period calculator for MyGreenway
@ApplicationScoped
public class PeriodCalculator {

    private static final int BASE_PERIOD = 74; // February 2023
    private static final LocalDate START_DATE = LocalDate.of(2023, 2, 7);

    /// Calculates the previous period number relative to the current date
    /// @return previous period number
    public int calculatePreviousPeriod() {
        return calculatePreviousPeriod(LocalDate.now());
    }

    /// Calculates the previous period number relative to the given date
    /// @param currentDate date for calculation
    /// @return previous period number
    public int calculatePreviousPeriod(LocalDate currentDate) {
        long monthsBetween = ChronoUnit.MONTHS.between(START_DATE, currentDate);
        return BASE_PERIOD + (int) monthsBetween - 1;
    }
}
