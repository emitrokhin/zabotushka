package ru.mitrohinayulya.zabotushka.config;

import io.smallrye.config.ConfigMapping;

/// Configuration for Basic Authentication
@ConfigMapping(prefix = "app.basic-auth")
public interface BasicAuthConfig {

    /// Username for Basic Auth
    String username();

    /// Password for Basic Auth
    String password();
}
