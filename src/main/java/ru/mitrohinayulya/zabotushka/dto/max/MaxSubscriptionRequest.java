package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MaxSubscriptionRequest(
        String url,
        @JsonProperty("update_types") List<String> updateTypes,
        String secret
) {
    public static MaxSubscriptionRequest forAllUpdateTypes(String webhookUrl, String secretToken) {
        return new MaxSubscriptionRequest(
                webhookUrl,
                List.of(
                        "user_added",
                        "bot_added",
                        "bot_removed",
                        "message_callback",
                        "message_removed",
                        "message_created",
                        "message_edited",
                        "bot_started",
                        "chat_title_changed",
                        "message_chat_created",
                        "user_removed"),
                secretToken);
    }
}