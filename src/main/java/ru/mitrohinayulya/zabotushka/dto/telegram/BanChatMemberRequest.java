package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Запрос для удаления/бана участника чата
 */
public record BanChatMemberRequest(
        @JsonProperty("chat_id") Long chatId,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("revoke_messages") Boolean revokeMessages
) {
    public static BanChatMemberRequest of(Long chatId, Long userId) {
        return new BanChatMemberRequest(chatId, userId, false);
    }
}
