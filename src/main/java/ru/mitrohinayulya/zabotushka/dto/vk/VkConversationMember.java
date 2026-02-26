package ru.mitrohinayulya.zabotushka.dto.vk;

import com.fasterxml.jackson.annotation.JsonProperty;

/// A single member entry from VK `messages.getConversationMembers` response.
public record VkConversationMember(
        @JsonProperty("member_id") long memberId
) {
}
