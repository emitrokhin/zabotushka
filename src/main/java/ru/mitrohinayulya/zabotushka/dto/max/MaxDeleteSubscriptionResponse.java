package ru.mitrohinayulya.zabotushka.dto.max;

/// Unsubscribes the bot from receiving updates via Webhook. After calling this method, the bot stops
/// receiving event notifications, and long-polling delivery becomes available via the API.
public record MaxDeleteSubscriptionResponse(
        boolean success,
        String message
) {
}
