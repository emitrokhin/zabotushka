package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

/// MaxUpdate represents various types of events that occurred in a Max chat.
public record MaxUpdate(
        @JsonProperty("update_type") String updateType,
        long timestamp,
        @JsonProperty("chat_id") long chatId,
        MaxUser user,
        @JsonProperty("inviter_id") long inviterId,
        @JsonProperty("is_channel") Boolean isChannel
) {
}