package ru.mitrohinayulya.zabotushka.dto.vk;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Payload of a VK `group_leave` event.
public record VkGroupLeaveObject(
        @JsonProperty("user_id") long userId,
        boolean self
) {
}
