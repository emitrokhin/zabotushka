package ru.mitrohinayulya.zabotushka.client;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import ru.mitrohinayulya.zaboutshka.config.GreenwayConfig;
import ru.mitrohinayulya.zaboutshka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zaboutshka.dto.greenway.RefreshTokenResponse;
import ru.mitrohinayulya.zaboutshka.exception.GreenwayApiException;
import ru.mitrohinayulya.zaboutshka.exception.GreenwayAuthenticationException;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * HTTP клиент для работы с MyGreenway API
 */
@ApplicationScoped
@Slf4j
public class MyGreenwayClient {

    private static final String BASE_URL = "https://pyapi.greenwaystart.com/pyapi/v1";
    private static final String GREENWAY_URL = "https://greenwaystart.com";

    @Inject
    GreenwayConfig config;

    private final MyGreenwayLoginApi loginApi;
    private final MyGreenwayApi apiClient;

    private final AtomicReference<String> accessToken = new AtomicReference<>();
    private final AtomicReference<String> refreshToken = new AtomicReference<>();

    public MyGreenwayClient() {
        // Создаем REST Client для логина
        this.loginApi = RestClientBuilder.newBuilder()
                .baseUri(URI.create(GREENWAY_URL))
                .build(MyGreenwayLoginApi.class);

        // Создаем REST Client для API запросов
        this.apiClient = RestClientBuilder.newBuilder()
                .baseUri(URI.create(BASE_URL))
                .build(MyGreenwayApi.class);
    }

    /**
     * Авторизация в системе MyGreenway (получение session_id из cookies)
     */
    public String login() {
        log.info("Logging in to MyGreenway as user: {}", config.username());

        Form form = new Form()
                .param("type", "auth")
                .param("action", "login")
                .param("REMEMBER", "0")
                .param("NAME", config.username())
                .param("PASSWORD", config.password());

        try {
            Response response = loginApi.login(form);

            // Извлекаем session_id из cookies
            List<Object> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies == null || cookies.isEmpty()) {
                log.error("No cookies received from login");
                throw new GreenwayAuthenticationException("No cookies received from login. Check credentials.");
            }

            log.debug("All cookies received: {}", cookies);

            // Ищем куку _a_ (session_id)
            String sessionCookie = cookies.stream()
                    .map(Object::toString)
                    .filter(cookie -> cookie.startsWith("_a_="))
                    .findFirst()
                    .orElseThrow(() -> new GreenwayAuthenticationException(
                            "Session cookie not found in response. Credentials may be incorrect."));

            log.debug("Session cookie found: {}", sessionCookie);

            // Извлекаем значение session_id
            String sessionId = sessionCookie.split(";")[0].substring(4);
            log.info("Login successful, session_id extracted: {}", sessionId);
            return sessionId;

        } catch (GreenwayAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed", e);
            throw new GreenwayAuthenticationException("Failed to login to MyGreenway: " + e.getMessage(), e);
        }
    }

    /**
     * Создание сессии (получение JWT токенов)
     */
    public RefreshTokenResponse createSession(String sessionId) {
        log.info("Creating MyGreenway session with session_id: {}", sessionId);

        Form form = new Form().param("session_id", sessionId);

        try {
            RefreshTokenResponse response = apiClient.createSession(form);

            if (response != null && response.code() == null) {
                this.accessToken.set(response.accessToken());
                this.refreshToken.set(response.refreshToken());
                log.info("Session created successfully");
                log.debug("Access token: {}...",
                        response.accessToken().substring(0, Math.min(20, response.accessToken().length())));
                return response;
            } else {
                String code = response != null ? response.code() : "unknown";
                String detail = response != null ? response.detail() : "No response received";
                log.error("Failed to create session: code={}, detail={}", code, detail);
                throw new GreenwayApiException("Failed to create session", code, detail);
            }
        } catch (GreenwayApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating session", e);
            throw new GreenwayApiException("Error creating session: " + e.getMessage(), e);
        }
    }

    /**
     * Обновление JWT токенов
     */
    public RefreshTokenResponse refreshToken() {
        String currentRefreshToken = refreshToken.get();
        if (currentRefreshToken == null) {
            throw new IllegalStateException("Refresh token is not available");
        }

        log.info("Refreshing MyGreenway token");

        Form form = new Form().param("refresh", currentRefreshToken);

        try {
            RefreshTokenResponse response = apiClient.refreshToken(form);

            if (response != null && response.code() == null) {
                this.accessToken.set(response.accessToken());
                this.refreshToken.set(response.refreshToken());
                log.info("Token refreshed successfully");
                return response;
            } else {
                String code = response != null ? response.code() : "unknown";
                String detail = response != null ? response.detail() : "No response received";
                log.error("Failed to refresh token: code={}, detail={}", code, detail);
                this.accessToken.set(null);
                this.refreshToken.set(null);
                throw new GreenwayApiException("Failed to refresh token", code, detail);
            }
        } catch (GreenwayApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            this.accessToken.set(null);
            this.refreshToken.set(null);
            throw new GreenwayApiException("Error refreshing token: " + e.getMessage(), e);
        }
    }

    /**
     * Получение списка партнеров
     */
    public PartnerListResponse getPartnerList(long partnerId, int previousPeriod) {
        return getPartnerList(partnerId, previousPeriod, false);
    }

    private PartnerListResponse getPartnerList(long partnerId, int previousPeriod, boolean isRetry) {
        String currentAccessToken = accessToken.get();
        if (currentAccessToken == null) {
            throw new IllegalStateException("Access token is not available. Please login first.");
        }

        log.debug("Fetching partner list for partnerId={}, period={}, isRetry={}",
                partnerId, previousPeriod, isRetry);

        try {
            PartnerListResponse response = apiClient.getPartnerList(
                    "Bearer " + currentAccessToken,
                    partnerId,
                    previousPeriod > 0 ? previousPeriod : null
            );

            if (response != null && response.code() != null) {
                log.error("Failed to get partner list: code={}, detail={}",
                        response.code(), response.detail());
                throw new GreenwayApiException("Failed to get partner list",
                        response.code(), response.detail());
            }

            return response;
        } catch (GreenwayApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching partner list for partnerId={}", partnerId, e);

            if (!isRetry) {
                log.info("Attempting to refresh token and retry request");
                try {
                    refreshToken();
                    return getPartnerList(partnerId, previousPeriod, true);
                } catch (GreenwayApiException refreshError) {
                    log.error("Failed to refresh token during retry", refreshError);
                    throw new GreenwayApiException(
                            "Error fetching partner list and failed to refresh token: " + e.getMessage(),
                            refreshError);
                }
            }

            throw new GreenwayApiException("Error fetching partner list: " + e.getMessage(), e);
        }
    }
}