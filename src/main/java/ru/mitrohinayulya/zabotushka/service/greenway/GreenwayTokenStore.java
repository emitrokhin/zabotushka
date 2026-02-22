package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class GreenwayTokenStore {

    private final AtomicReference<String> accessToken = new AtomicReference<>();
    private final AtomicReference<String> refreshToken = new AtomicReference<>();

    public String getAccessToken() {
        return accessToken.get();
    }

    public void setAccessToken(String token) {
        accessToken.set(token);
    }

    public String getRefreshToken() {
        return refreshToken.get();
    }

    public void setRefreshToken(String token) {
        refreshToken.set(token);
    }

    public void clear() {
        accessToken.set(null);
        refreshToken.set(null);
    }
}
