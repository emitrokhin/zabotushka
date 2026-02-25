package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Response for comparing SGO (Structural Group Turnover)
public record CompareSGOResponse(
        long userId,
        Double sgo,
        @JsonProperty("sgoComparisonResult")
        ComparisonResult comparisonResult,
        Integer period
) {
    public static CompareSGOResponse of(long userId, Double sgo, ComparisonResult result, Integer period) {
        return new CompareSGOResponse(userId, sgo, result, period);
    }

    public static CompareSGOResponse notFound(Integer period) {
        return new CompareSGOResponse(0L, 0.0, ComparisonResult.NOT_FOUND, period);
    }
}
