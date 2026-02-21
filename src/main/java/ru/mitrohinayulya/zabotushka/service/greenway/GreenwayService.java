package ru.mitrohinayulya.zabotushka.service.greenway;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.NewCookie;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayApi;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayLoginApi;
import ru.mitrohinayulya.zabotushka.config.GreenwayConfig;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.exception.GreenwayAuthenticationException;
import ru.mitrohinayulya.zabotushka.service.PeriodCalculator;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Сервис для работы с MyGreenway API
 */
@Startup
@ApplicationScoped
public class GreenwayService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayService.class);

    @Inject
    GreenwayConfig config;

    @Inject
    @RestClient
    MyGreenwayLoginApi loginApi;

    @Inject
    @RestClient
    MyGreenwayApi apiClient;

    @Inject
    PeriodCalculator periodCalculator;

    @ConfigProperty(name = "greenway.init.enabled", defaultValue = "true")
    boolean initEnabled;

    private final AtomicReference<String> accessToken = new AtomicReference<>();
    private final AtomicReference<String> refreshToken = new AtomicReference<>();

    /**
     * Инициализация сервиса при старте приложения
     * Выполняет вход и создает сессию для получения JWT токенов
     */
    @PostConstruct
    void initialize() {
        if (!initEnabled) {
            log.info("Greenway service initialization disabled");
            return;
        }

        log.info("Initializing Greenway service at startup");
        var sessionId = login();
        createSession(sessionId);
        log.info("Greenway service initialized successfully");
    }

    /**
     * Авторизация в системе MyGreenway (получение session_id из cookies)
     */
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
            // Извлекаем session_id из cookies
            var cookies = response.getCookies();
            if (cookies == null || cookies.isEmpty()) {
                log.error("No cookies received from login");
                throw new GreenwayAuthenticationException("No cookies received from login. Check credentials.");
            }

            log.debug("All cookies received: {}", cookies);

            // Ищем куку _a_ (session_id). Response#getCookies конвертирует Set-Cookie в NewCookie map.
            NewCookie sessionCookie = cookies.get("_a_");
            if (sessionCookie == null) {
                log.error("Session cookie _a_ not found in response");
                throw new GreenwayAuthenticationException(
                        "Session cookie not found in response. Credentials may be incorrect.");
            }

            log.debug("Session cookie found: {}", sessionCookie);

            // Извлекаем значение session_id
            var sessionId = sessionCookie.getValue();
            log.info("Login successful, session_id extracted: {}", sessionId);
            return sessionId;
        }
    }

    /**
     * Создание сессии (получение JWT токенов)
     */
    private void createSession(String sessionId) {
        log.info("Creating MyGreenway session with session_id: {}", sessionId);

        var form = new Form().param("session_id", sessionId);

        var response = apiClient.createSession(form);

        if (response != null && response.code() == null) {
            var token = response.accessToken();
            this.accessToken.set(token);
            this.refreshToken.set(response.refreshToken());
            log.info("Session created successfully");
        } else {
            var code = response != null ? response.code() : "unknown";
            var detail = response != null ? response.detail() : "No response received";
            log.error("Failed to create session: code={}, detail={}", code, detail);
            throw new GreenwayApiException("Failed to create session", code, detail);
        }
    }

    /**
     * Обновление JWT токенов
     */
    public void refreshToken() {
        var currentRefreshToken = refreshToken.get();
        if (currentRefreshToken == null) {
            throw new IllegalStateException("Refresh token is not available");
        }

        log.info("Refreshing MyGreenway token");

        var form = new Form().param("refresh", currentRefreshToken);

        var response = apiClient.refreshToken(form);

        if (response != null && response.code() == null) {
            this.accessToken.set(response.accessToken());
            this.refreshToken.set(response.refreshToken());
            log.info("Token refreshed successfully");
        } else {
            var code = response != null ? response.code() : "unknown";
            var detail = response != null ? response.detail() : "No response received";
            log.error("Failed to refresh token: code={}, detail={}", code, detail);
            accessToken.set(null);
            refreshToken.set(null);
            throw new GreenwayApiException("Failed to refresh token", code, detail);
        }
    }

    /**
     * Получение списка партнеров
     */
    public PartnerListResponse getPartnerList(long partnerId, int previousPeriod) {
        return getPartnerList(partnerId, previousPeriod, false);
    }

    private PartnerListResponse getPartnerList(long partnerId, int previousPeriod, boolean isRetry) {
        var currentAccessToken = accessToken.get();

        if (currentAccessToken == null) {
            throw new IllegalStateException("Access token is not available. Please login first.");
        }

        log.debug("Fetching partner list for partnerId={}, period={}, isRetry={}",
                partnerId, previousPeriod, isRetry);

        try {
            var response = apiClient.getPartnerList(
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
        } catch (Exception e) {
            log.error("Error fetching partner list for partnerId={}", partnerId, e);

            if (!isRetry) {
                log.info("Attempting to refresh token and retry request");
                try {
                    refreshToken();
                    return getPartnerList(partnerId, previousPeriod, true);
                } catch (Exception refreshError) {
                    log.error("Failed to refresh token during retry", refreshError);
                    throw new GreenwayApiException(
                            "Error fetching partner list and failed to refresh token: " + e.getMessage(),
                            refreshError);
                }
            }

            throw new GreenwayApiException("Error fetching partner list: " + e.getMessage(), e);
        }
    }

    /**
     * Получает предыдущий период
     */
    public int getPreviousPeriod() {
        return periodCalculator.calculatePreviousPeriod();
    }

    /**
     * Находит партнера по ID в списке партнеров
     */
    public java.util.Optional<ru.mitrohinayulya.zabotushka.dto.greenway.Partner> findPartnerById(
            ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse response,
            long partnerId) {

        if (response == null || response.partners() == null || response.partners().isEmpty()) {
            return java.util.Optional.empty();
        }

        return response.partners().stream()
                .filter(partner -> partner.number() != null)
                .filter(partner -> partner.number().longValue() == partnerId)
                .findFirst();
    }
}
