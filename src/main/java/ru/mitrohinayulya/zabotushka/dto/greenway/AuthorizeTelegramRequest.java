package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Запрос на авторизацию партнера MyGreenway через Telegram
 *
 * @param telegramId ID пользователя в Telegram
 * @param greenwayId ID партнера в системе Greenway
 * @param regDate    Дата регистрации партнера в формате DD.MM.YYYY
 */
public record AuthorizeTelegramRequest(
    @JsonProperty("telegramId")
    long telegramId,

    @JsonProperty("greenwayId")
    long greenwayId,

    @NotNull(message = "regDate is required")
    @Pattern(
        regexp = "^(0[1-9]|[12]\\d|3[01])\\.(0[1-9]|1[0-2])\\.\\d{4}$",
        message = "regDate must be in DD.MM.YYYY format (e.g., 29.12.2025)"
    )
    @JsonProperty("regDate")
    String regDate
) {
}
