package ru.mitrohinayulya.zabotushka.service.telegram;

import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.dto.telegram.SetWebhookRequest;
import ru.mitrohinayulya.zabotushka.service.platform.MessengerWebhookRegistrar;

@Startup
@ApplicationScoped
public class TelegramWebhookRegistrar implements MessengerWebhookRegistrar {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookRegistrar.class);

    @Inject
    @RestClient
    TelegramAccessBotApi telegramAccessBotApi;

    @ConfigProperty(name = "app.host")
    String hostUrl;

    @ConfigProperty(name = "app.telegram.webhook.path")
    String webhookPath;

    @ConfigProperty(name = "app.telegram.webhook.secret")
    String webhookSecret;

    @ConfigProperty(name = "app.telegram.webhook.enabled", defaultValue = "true")
    boolean webhookEnabled;

    @PostConstruct
    @Override
    public void registerWebhook() {
        if (!webhookEnabled) {
            log.info("Telegram webhook registration is disabled");
            return;
        }

        var fullWebhookUrl = hostUrl + webhookPath;

        try {
            log.info("Registering Telegram webhook: url={}", fullWebhookUrl);

            var request = SetWebhookRequest.forChatJoinRequests(fullWebhookUrl, webhookSecret);
            var response = telegramAccessBotApi.setWebhook(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Telegram webhook registered successfully");
            } else {
                log.error("Failed to register Telegram webhook: {}", response.description());
            }
        } catch (Exception e) {
            log.error("Error during Telegram webhook registration", e);
        }
    }

    @Shutdown
    @Override
    public void unregisterWebhook() {
        log.warn("Telegram webhook unregistration is not supported yet");
    }
}
