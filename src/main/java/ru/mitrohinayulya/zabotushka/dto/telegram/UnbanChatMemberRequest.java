package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Request to unban a chat member
public record UnbanChatMemberRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("user_id") long userId,
        @JsonProperty("only_if_banned") Boolean onlyIfBanned
) {
    public static UnbanChatMemberRequest of(long chatId, long userId) {
        return new UnbanChatMemberRequest(chatId, userId, false);
    }
}
