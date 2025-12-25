package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request to set webhook for Telegram bot
 */
public record SetWebhookRequest(
        String url,
        @JsonProperty("allowed_updates") List<String> allowedUpdates,
        @JsonProperty("secret_token") String secretToken
) {
    public static SetWebhookRequest forChatJoinRequests(String webhookUrl, String secretToken) {
        return new SetWebhookRequest(webhookUrl, List.of("chat_join_request"), secretToken);
    }
}
