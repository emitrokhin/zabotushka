package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Telegram User object
 */
public record User(
        Long id,
        @JsonProperty("is_bot") Boolean isBot,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("language_code") String languageCode
) {
}
