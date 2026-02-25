package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Response for session creation or token refresh requests
/// @param accessToken JWT access token
/// @param refreshToken JWT refresh token
/// @param code Error code (if any)
/// @param detail Error details (if any)
public record RefreshTokenResponse(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("code")
    String code,

    @JsonProperty("detail")
    String detail
) { }
