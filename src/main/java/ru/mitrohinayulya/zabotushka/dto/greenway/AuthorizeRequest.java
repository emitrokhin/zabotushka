package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на авторизацию партнера MyGreenway
 *
 * @param telegramId ID пользователя в Telegram
 * @param greenwayId ID партнера в системе Greenway
 * @param regDate    Дата регистрации партнера в формате YYYY-MM-DD
 */
public record AuthorizeRequest(
    @NotNull(message = "telegramId is required")
    @JsonProperty("telegramId")
    Long telegramId,

    @NotNull(message = "greenwayId is required")
    @JsonProperty("greenwayId")
    Long greenwayId,

    @NotNull(message = "regDate is required")
    @JsonProperty("regDate")
    String regDate
) {
}
