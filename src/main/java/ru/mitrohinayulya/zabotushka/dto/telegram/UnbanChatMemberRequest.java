package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Запрос для разбана участника чата
 */
public record UnbanChatMemberRequest(
        @JsonProperty("chat_id") Long chatId,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("only_if_banned") Boolean onlyIfBanned
) {
    public static UnbanChatMemberRequest of(Long chatId, Long userId) {
        return new UnbanChatMemberRequest(chatId, userId, true);
    }
}
