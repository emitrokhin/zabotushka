package ru.mitrohinayulya.zabotushka.config;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "app.greenway")
public interface GreenwayConfig {
    Optional<String> id();
    Optional<String> password();
}