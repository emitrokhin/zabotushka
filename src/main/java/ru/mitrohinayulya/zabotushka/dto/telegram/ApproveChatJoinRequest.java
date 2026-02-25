package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Request to approve a chat join request
public record ApproveChatJoinRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("user_id") long userId
) {
    public static ApproveChatJoinRequest of(long chatId, long userId) {
        return new ApproveChatJoinRequest(chatId, userId);
    }
}
