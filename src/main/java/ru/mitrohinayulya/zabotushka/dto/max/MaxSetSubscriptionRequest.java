package ru.mitrohinayulya.zabotushka.dto.max;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MaxSetSubscriptionRequest(
        String url,
        @JsonProperty("update_types") List<String> updateTypes,
        String secret
) {
    public static MaxSetSubscriptionRequest forAllUpdateTypes(String webhookUrl, String secretToken) {
        return new MaxSetSubscriptionRequest(
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

    public static MaxSetSubscriptionRequest forUserUpdates(String webhookUrl, String secretToken) {
        return new MaxSetSubscriptionRequest(
                webhookUrl,
                List.of("user_added", "user_removed"),
                secretToken);
    }
}