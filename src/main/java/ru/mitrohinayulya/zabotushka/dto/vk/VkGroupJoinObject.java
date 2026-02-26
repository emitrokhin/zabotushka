package ru.mitrohinayulya.zabotushka.dto.vk;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Payload of a VK `group_join` event.
public record VkGroupJoinObject(
        @JsonProperty("user_id") long userId,
        @JsonProperty("join_type") String joinType
) {
}
