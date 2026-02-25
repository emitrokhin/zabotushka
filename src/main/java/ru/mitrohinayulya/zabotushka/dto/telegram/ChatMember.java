package ru.mitrohinayulya.zabotushka.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Представляет участника чата в Telegram
public record ChatMember(
        @JsonProperty("status") String status,
        @JsonProperty("user") User user
) {
    /// Проверяет, является ли пользователь членом группы
    public boolean isMember() {
        return "member".equals(status)
                || "administrator".equals(status)
                || "creator".equals(status);
    }

    /// Проверяет, является ли пользователь администратором
    public boolean isAdmin() {
        return "administrator".equals(status) || "creator".equals(status);
    }
}
