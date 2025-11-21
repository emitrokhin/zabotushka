package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Результат сравнения числовых значений
 */
public enum ComparisonResult {
    GREATER("greater"),
    LESS("less"),
    EQUAL("equal"),
    NOT_FOUND("not-found");

    private final String value;

    ComparisonResult(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ComparisonResult compare(double actual, double expected) {
        if (actual > expected) {
            return GREATER;
        } else if (actual < expected) {
            return LESS;
        } else {
            return EQUAL;
        }
    }
}
