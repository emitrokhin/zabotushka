package ru.mitrohinayulya.zabotushka.service.greenway;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Form;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayApi;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayLoginApi;
import ru.mitrohinayulya.zabotushka.config.GreenwayConfig;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.exception.GreenwayAuthenticationException;

/// Manages the Greenway authentication session lifecycle:
/// startup login, cookie extraction, JWT exchange, and token refresh.
@Startup
@ApplicationScoped
public class GreenwaySessionManager {

    private static final Logger log = LoggerFactory.getLogger(GreenwaySessionManager.class);

    @Inject
    GreenwayConfig config;

    @Inject
    @RestClient
    MyGreenwayLoginApi loginApi;

    @Inject
    @RestClient
    MyGreenwayApi apiClient;

    @Inject
    GreenwayTokenStore tokenStore;

    @ConfigProperty(name = "greenway.init.enabled", defaultValue = "true")
    boolean initEnabled;

    @PostConstruct
    void initialize() {
        if (!initEnabled) {
            log.info("Greenway service initialization disabled");
            return;
        }

        log.info("Initializing Greenway session at startup");
        var sessionId = login();
        createSession(sessionId);
        log.info("Greenway session initialized successfully");
    }

    private String login() {
        var id = config.id().orElseThrow(() ->
                new IllegalStateException("Greenway ID is not configured"));
        var password = config.password().orElseThrow(() ->
                new IllegalStateException("Greenway password is not configured"));

        log.info("Logging in to MyGreenway as user: {}", id);

        var form = new Form()
                .param("type", "auth")
                .param("action", "login")
                .param("REMEMBER", "0")
                .param("NAME", id)
                .param("PASSWORD", password);

        try (var response = loginApi.login(form)) {
            var cookies = response.getCookies();
            if (cookies == null || cookies.isEmpty()) {
                log.error("No cookies received from login");
                throw new GreenwayAuthenticationException("No cookies received from login. Check credentials.");
            }

            log.debug("All cookies received: {}", cookies);

            var sessionCookie = cookies.get("_a_");
            if (sessionCookie == null) {
                log.error("Session cookie _a_ not found in response");
                throw new GreenwayAuthenticationException(
                        "Session cookie not found in response. Credentials may be incorrect.");
            }

            log.debug("Session cookie found: {}", sessionCookie);

            var sessionId = sessionCookie.getValue();
            log.info("Login successful, session_id extracted: {}", sessionId);
            return sessionId;
        }
    }

    private void createSession(String sessionId) {
        log.info("Creating MyGreenway session with session_id: {}", sessionId);

        var form = new Form().param("session_id", sessionId);
        var response = apiClient.createSession(form);

        if (response != null && response.code() == null) {
            tokenStore.setAccessToken(response.accessToken());
            tokenStore.setRefreshToken(response.refreshToken());
            log.info("Session created successfully");
        } else {
            var code = response != null ? response.code() : "unknown";
            var detail = response != null ? response.detail() : "No response received";
            log.error("Failed to create session: code={}, detail={}", code, detail);
            throw new GreenwayApiException("Failed to create session", code, detail);
        }
    }

    public void refreshToken() {
        var currentRefreshToken = tokenStore.getRefreshToken();
        if (currentRefreshToken == null) {
            throw new IllegalStateException("Refresh token is not available");
        }

        log.info("Refreshing MyGreenway token");

        var form = new Form().param("refresh", currentRefreshToken);
        var response = apiClient.refreshToken(form);

        if (response != null && response.code() == null) {
            tokenStore.setAccessToken(response.accessToken());
            tokenStore.setRefreshToken(response.refreshToken());
            log.info("Token refreshed successfully");
        } else {
            var code = response != null ? response.code() : "unknown";
            var detail = response != null ? response.detail() : "No response received";
            log.error("Failed to refresh token: code={}, detail={}", code, detail);
            tokenStore.clear();
            throw new GreenwayApiException("Failed to refresh token", code, detail);
        }
    }
}
