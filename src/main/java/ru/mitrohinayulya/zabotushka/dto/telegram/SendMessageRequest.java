package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Request to send a message
public record SendMessageRequest(
        @JsonProperty("chat_id") long chatId,
        String text
) {
    public static SendMessageRequest of(long chatId, String text) {
        return new SendMessageRequest(chatId, text);
    }
}
