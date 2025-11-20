package ru.mitrohinayulya.zabotushka.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "greenway")
public interface GreenwayConfig {
    String username();
    String password();
}