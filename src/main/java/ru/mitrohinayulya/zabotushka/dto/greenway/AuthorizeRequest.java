package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на авторизацию партнера MyGreenway
 *
 * @param greenwayId ID партнера в системе Greenway
 * @param regDate    Дата регистрации партнера в формате YYYY-MM-DD
 */
public record AuthorizeRequest(
    @NotNull(message = "greenwayId is required")
    @JsonProperty("greenwayId")
    long greenwayId,

    @NotNull(message = "regDate is required")
    @JsonProperty("regDate")
    String regDate
) {
}
