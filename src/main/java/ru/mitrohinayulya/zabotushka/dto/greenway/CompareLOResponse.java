package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Response for comparing LO (Personal Turnover)
public record CompareLOResponse(
        long userId,
        Double lo,
        @JsonProperty("loComparisonResult")
        ComparisonResult comparisonResult,
        Integer period
) {
    public static CompareLOResponse of(long userId, Double lo, ComparisonResult result, Integer period) {
        return new CompareLOResponse(userId, lo, result, period);
    }

    public static CompareLOResponse notFound(Integer period) {
        return new CompareLOResponse(0L, 0.0, ComparisonResult.NOT_FOUND, period);
    }
}
