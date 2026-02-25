package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/// Chat member in Max. Contains information about a user or bot,
/// as well as chat membership data (role, permissions, join time).
public record MaxChatMember(
        @JsonProperty("user_id") long userId,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("is_bot") boolean isBot,
        @JsonProperty("last_activity_time") long lastActivityTime,
        String name,
        String description,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("full_avatar_url") String fullAvatarUrl,
        @JsonProperty("last_access_time") long lastAccessTime,
        @JsonProperty("is_owner") boolean isOwner,
        @JsonProperty("is_admin") boolean isAdmin,
        @JsonProperty("join_time") long joinTime,
        List<String> permissions,
        String alias
) {
}
