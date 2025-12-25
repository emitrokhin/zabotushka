package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramApi;
import ru.mitrohinayulya.zabotushka.dto.telegram.SetWebhookRequest;

/**
 * Сервис для работы с Telegram Bot API
 */
@Startup
@ApplicationScoped
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    @Inject
    @RestClient
    TelegramApi telegramApi;

    @ConfigProperty(name = "app.host")
    String hostUrl;

    @ConfigProperty(name = "app.telegram.webhook.path")
    String webhookPath;

    @ConfigProperty(name = "app.telegram.webhook.secret")
    String webhookSecret;

    @ConfigProperty(name = "app.telegram.webhook.enabled", defaultValue = "true")
    boolean webhookEnabled;

    /**
     * Регистрирует webhook в Telegram при старте приложения
     */
    @PostConstruct
    public void registerWebhook() {
        if (!webhookEnabled) {
            log.info("Telegram webhook registration is disabled");
            return;
        }

        if (hostUrl == null || hostUrl.isBlank()) {
            log.warn("Host URL is not configured, skipping webhook registration");
            return;
        }

        var fullWebhookUrl = hostUrl + webhookPath;

        try {
            log.info("Registering Telegram webhook: url={}", fullWebhookUrl);

            var request = SetWebhookRequest.forChatJoinRequests(fullWebhookUrl, webhookSecret);
            var response = telegramApi.setWebhook(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Telegram webhook registered successfully");
            } else {
                log.error("Failed to register Telegram webhook: {}", response.description());
            }
        } catch (Exception e) {
            log.error("Error during Telegram webhook registration", e);
        }
    }
}
