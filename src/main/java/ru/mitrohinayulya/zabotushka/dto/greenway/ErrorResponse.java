package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ с ошибкой
 *
 * @param error Описание ошибки
 */
public record ErrorResponse(
    @JsonProperty("error")
    String error
) {
    public static ErrorResponse of(String error) {
        return new ErrorResponse(error);
    }

    public static ErrorResponse userAlreadyExists() {
        return new ErrorResponse("User with this telegramId already exists");
    }
}
