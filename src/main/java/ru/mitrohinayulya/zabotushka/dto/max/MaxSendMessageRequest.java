package ru.mitrohinayulya.zabotushka.dto.max;

/// Request to send a message via Max Bot API
public record MaxSendMessageRequest(
        String text
) {
    public static MaxSendMessageRequest withText(String text) {
        return new MaxSendMessageRequest(text);
    }
}
