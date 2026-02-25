package ru.mitrohinayulya.zabotushka.config;

import io.smallrye.config.ConfigMapping;

/// Конфигурация для Basic Authentication
@ConfigMapping(prefix = "app.basic-auth")
public interface BasicAuthConfig {

    /// Имя пользователя для Basic Auth
    String username();

    /// Пароль для Basic Auth
    String password();
}
