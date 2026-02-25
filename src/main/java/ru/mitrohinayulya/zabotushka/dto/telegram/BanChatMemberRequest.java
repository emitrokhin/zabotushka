package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Request to remove/ban a chat member
public record BanChatMemberRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("user_id") long userId,
        @JsonProperty("revoke_messages") Boolean revokeMessages
) {
    public static BanChatMemberRequest of(long chatId, long userId) {
        return new BanChatMemberRequest(chatId, userId, false);
    }
}
