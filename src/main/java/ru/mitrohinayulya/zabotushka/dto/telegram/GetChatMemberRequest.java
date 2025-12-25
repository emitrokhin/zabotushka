package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Запрос для получения информации об участнике чата
 */
public record GetChatMemberRequest(
        @JsonProperty("chat_id") Long chatId,
        @JsonProperty("user_id") Long userId
) {
    public static GetChatMemberRequest of(Long chatId, Long userId) {
        return new GetChatMemberRequest(chatId, userId);
    }
}
