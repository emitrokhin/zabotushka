package ru.mitrohinayulya.zabotushka.dto.vk;

/// Generic VK API response wrapper.
/// All VK API responses have shape `{"response": <data>}` on success.
public record VkApiResponse<T>(T response) {

    /// Returns true if the VK API returned a standard success code (1).
    /// Applicable to void-like operations that return 1 on success.
    public boolean isSuccess() {
        return Integer.valueOf(1).equals(response);
    }
}
