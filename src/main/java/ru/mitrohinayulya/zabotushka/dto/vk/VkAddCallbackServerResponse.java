package ru.mitrohinayulya.zabotushka.dto.vk;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Response from VK `groups.addCallbackServer` API.
public record VkAddCallbackServerResponse(
        @JsonProperty("server_id") int serverId
) {
}
