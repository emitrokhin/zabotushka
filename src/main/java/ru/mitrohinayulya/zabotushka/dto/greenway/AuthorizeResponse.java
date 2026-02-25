package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Ответ на запрос авторизации партнера MyGreenway
/// @param authorized Статус авторизации: "authorized" если дата регистрации совпадает, "not_authorized" если не совпадает
public record AuthorizeResponse(
    @JsonProperty("authorized")
    String authorized
) {
    /// Создает ответ с успешной авторизацией
    public static AuthorizeResponse createAuthorized() {
        return new AuthorizeResponse("authorized");
    }

    /// Создает ответ с неуспешной авторизацией
    public static AuthorizeResponse createNotAuthorized() {
        return new AuthorizeResponse("not_authorized");
    }
}
