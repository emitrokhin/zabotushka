package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ на запрос создания сессии или обновления токена
 *
 * @param accessToken  JWT access токен
 * @param refreshToken JWT refresh токен
 * @param code         Код ошибки (если есть)
 * @param detail       Детали ошибки (если есть)
 */
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
