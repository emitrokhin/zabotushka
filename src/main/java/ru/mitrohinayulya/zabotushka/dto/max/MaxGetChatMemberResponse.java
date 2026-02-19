package ru.mitrohinayulya.zabotushka.dto.max;

import java.util.List;

/// Результат запроса участников чата с информацией о времени последней активности
public record MaxGetChatMemberResponse(
        List<MaxChatMember> members
) {
}
