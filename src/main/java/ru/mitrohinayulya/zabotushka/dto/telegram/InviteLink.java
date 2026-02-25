package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Telegram ChatInviteLink object
public record InviteLink(
        @JsonProperty("invite_link") String inviteLink,
        User creator,
        @JsonProperty("creates_join_request") Boolean createsJoinRequest,
        @JsonProperty("is_primary") Boolean isPrimary,
        @JsonProperty("is_revoked") Boolean isRevoked
) {
}
