package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Telegram Chat object
public record Chat(
        long id,
        String type,
        String title,
        String username,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName
) {
}
