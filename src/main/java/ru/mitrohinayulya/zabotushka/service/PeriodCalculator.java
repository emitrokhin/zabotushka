package ru.mitrohinayulya.zabotushka.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/// Калькулятор периодов для MyGreenway
@ApplicationScoped
public class PeriodCalculator {

    private static final int BASE_PERIOD = 74; // февраль 2023
    private static final LocalDate START_DATE = LocalDate.of(2023, 2, 7);

    /// Вычисляет номер предыдущего периода относительно текущей даты
    /// @return номер предыдущего периода
    public int calculatePreviousPeriod() {
        return calculatePreviousPeriod(LocalDate.now());
    }

    /// Вычисляет номер предыдущего периода относительно указанной даты
    /// @param currentDate дата для расчета
    /// @return номер предыдущего периода
    public int calculatePreviousPeriod(LocalDate currentDate) {
        long monthsBetween = ChronoUnit.MONTHS.between(START_DATE, currentDate);
        return BASE_PERIOD + (int) monthsBetween - 1;
    }
}
