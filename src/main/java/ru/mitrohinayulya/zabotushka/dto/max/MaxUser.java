package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Пользователь или бот в Max
/// Объект содержит общую информацию о пользователе или боте без аватара
public record MaxUser(
        @JsonProperty("user_id") long userId,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("is_bot") Boolean isBot,
        @JsonProperty("last_activity_time") long lastActivityTime
) {
}