package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to decline a chat join request
 */
public record DeclineChatJoinRequest(
        @JsonProperty("chat_id") Long chatId,
        @JsonProperty("user_id") Long userId
) {
    public static DeclineChatJoinRequest of(Long chatId, Long userId) {
        return new DeclineChatJoinRequest(chatId, userId);
    }
}
