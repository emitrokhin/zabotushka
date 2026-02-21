package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to decline a chat join request
 */
public record DeclineChatJoinRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("user_id") long userId
) {
    public static DeclineChatJoinRequest of(long chatId, long userId) {
        return new DeclineChatJoinRequest(chatId, userId);
    }
}
