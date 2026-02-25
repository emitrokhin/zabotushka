package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/// Request to authorize a MyGreenway partner via Telegram
/// @param telegramId user ID in Telegram
/// @param greenwayId partner ID in the Greenway system
/// @param regDate Partner registration date in DD.MM.YYYY format
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
