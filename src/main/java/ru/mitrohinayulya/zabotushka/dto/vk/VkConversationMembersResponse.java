package ru.mitrohinayulya.zabotushka.dto.vk;

import java.util.List;

/// Response body of VK `messages.getConversationMembers` (extended=0).
public record VkConversationMembersResponse(
        List<VkConversationMember> items
) {
}
