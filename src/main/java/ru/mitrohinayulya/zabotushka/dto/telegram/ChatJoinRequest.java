package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Telegram ChatJoinRequest object
 * Represents a join request sent to a chat
 */
public record ChatJoinRequest(
        Chat chat,
        User from,
        @JsonProperty("user_chat_id") Long userChatId,
        Long date,
        String bio,
        @JsonProperty("invite_link") InviteLink inviteLink
) {
}
