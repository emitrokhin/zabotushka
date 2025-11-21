package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ на сравнение ЛО (Личный Оборот)
 */
public record CompareLOResponse(
        Long userId,
        Double lo,
        @JsonProperty("loComparisonResult")
        ComparisonResult comparisonResult,
        Integer period
) {
    public static CompareLOResponse of(Long userId, Double lo, ComparisonResult result, Integer period) {
        return new CompareLOResponse(userId, lo, result, period);
    }

    public static CompareLOResponse notFound(Integer period) {
        return new CompareLOResponse(0L, 0.0, ComparisonResult.NOT_FOUND, period);
    }
}
