package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Telegram Update object
/// This object represents an incoming update
public record Update(
        @JsonProperty("update_id") Long updateId,
        @JsonProperty("chat_join_request") ChatJoinRequest chatJoinRequest
) {
}
