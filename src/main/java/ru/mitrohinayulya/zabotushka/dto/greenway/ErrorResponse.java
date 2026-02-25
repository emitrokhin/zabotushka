package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Error response
/// @param error Error description
public record ErrorResponse(
    @JsonProperty("error")
    String error
) {
    public static ErrorResponse of(String error) {
        return new ErrorResponse(error);
    }
}
