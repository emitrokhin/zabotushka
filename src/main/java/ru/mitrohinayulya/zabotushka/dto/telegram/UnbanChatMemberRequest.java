package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Запрос для разбана участника чата
public record UnbanChatMemberRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("user_id") long userId,
        @JsonProperty("only_if_banned") Boolean onlyIfBanned
) {
    public static UnbanChatMemberRequest of(long chatId, long userId) {
        return new UnbanChatMemberRequest(chatId, userId, false);
    }
}
