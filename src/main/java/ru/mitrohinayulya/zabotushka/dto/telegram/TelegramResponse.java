package ru.mitrohinayulya.zabotushka.dto.telegram;

/// Generic Telegram API response
public record TelegramResponse<T>(
        Boolean ok,
        T result,
        String description
) {
}
