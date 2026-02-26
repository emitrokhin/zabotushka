package ru.mitrohinayulya.zabotushka.dto.vk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/// Envelope for all VK Callback API events.
/// The `object` field shape varies by `type` — callers must
/// inspect `type` before deserializing `object`.
public record VkUpdate(
        String type,
        @JsonProperty("group_id") long groupId,
        String secret,
        JsonNode object
) {
}
