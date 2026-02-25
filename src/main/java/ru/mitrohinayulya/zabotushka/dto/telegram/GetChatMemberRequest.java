package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Request to get information about a chat member
public record GetChatMemberRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("user_id") long userId
) {
    public static GetChatMemberRequest of(long chatId, long userId) {
        return new GetChatMemberRequest(chatId, userId);
    }
}
