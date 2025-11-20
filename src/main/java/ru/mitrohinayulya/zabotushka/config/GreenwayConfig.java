package ru.mitrohinayulya.zabotushka.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.greenway")
public interface GreenwayConfig {
    String id();
    String password();
}