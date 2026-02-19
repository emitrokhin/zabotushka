package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Объект MaxUpdate представляет различные типы событий, произошедших в чате Max.
public record MaxUpdate(
        @JsonProperty("update_type") String updateType,
        Long timestamp,
        @JsonProperty("chat_id") Long chatId,
        MaxUser user,
        @JsonProperty("inviter_id") Long inviterId,
        @JsonProperty("is_channel") Boolean isChannel
) {
}