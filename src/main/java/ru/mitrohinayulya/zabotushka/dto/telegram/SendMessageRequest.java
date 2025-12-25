package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to send a message
 */
public record SendMessageRequest(
        @JsonProperty("chat_id") Long chatId,
        String text
) {
    public static SendMessageRequest of(Long chatId, String text) {
        return new SendMessageRequest(chatId, text);
    }
}
