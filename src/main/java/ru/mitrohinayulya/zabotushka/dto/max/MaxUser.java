package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

/// A user or bot in Max
/// Contains general information about a user or bot without avatar data
public record MaxUser(
        @JsonProperty("user_id") long userId,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("is_bot") Boolean isBot,
        @JsonProperty("last_activity_time") long lastActivityTime
) {
}