package ru.mitrohinayulya.zabotushka.dto.max;

import java.util.List;

/// Result of a chat members request, including last activity time information
public record MaxGetChatMemberResponse(
        List<MaxChatMember> members
) {
}
