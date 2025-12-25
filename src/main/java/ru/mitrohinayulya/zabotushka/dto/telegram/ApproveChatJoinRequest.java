package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to approve a chat join request
 */
public record ApproveChatJoinRequest(
        @JsonProperty("chat_id") Long chatId,
        @JsonProperty("user_id") Long userId
) {
    public static ApproveChatJoinRequest of(Long chatId, Long userId) {
        return new ApproveChatJoinRequest(chatId, userId);
    }
}
