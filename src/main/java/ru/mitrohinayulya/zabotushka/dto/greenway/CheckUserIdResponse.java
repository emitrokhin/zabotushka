package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonInclude;

/// Response for checking partner existence
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CheckUserIdResponse(
        Long userId
) {
    public static CheckUserIdResponse of(long userId) {
        return new CheckUserIdResponse(userId);
    }

    public static CheckUserIdResponse notFound() {
        return new CheckUserIdResponse(null);
    }
}
