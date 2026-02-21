package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Объект MaxUpdate представляет различные типы событий, произошедших в чате Max.
public record MaxUpdate(
        @JsonProperty("update_type") String updateType,
        long timestamp,
        @JsonProperty("chat_id") long chatId,
        MaxUser user,
        @JsonProperty("inviter_id") long inviterId,
        @JsonProperty("is_channel") Boolean isChannel
) {
}