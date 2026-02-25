package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Represents a chat member in Telegram
public record ChatMember(
        @JsonProperty("status") String status,
        @JsonProperty("user") User user
) {
    /// Checks if the user is a member of the group
    public boolean isMember() {
        return "member".equals(status)
                || "administrator".equals(status)
                || "creator".equals(status);
    }

    /// Checks if the user is an administrator
    public boolean isAdmin() {
        return "administrator".equals(status) || "creator".equals(status);
    }
}
