package ru.mitrohinayulya.zabotushka.service.max;

import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.dto.max.MaxSetSubscriptionRequest;
import ru.mitrohinayulya.zabotushka.service.platform.MessengerWebhookRegistrar;

@Startup
@ApplicationScoped
public class MaxWebhookRegistrar implements MessengerWebhookRegistrar {

    private static final Logger log = LoggerFactory.getLogger(MaxWebhookRegistrar.class);

    @Inject
    @RestClient
    MaxBotApi botApi;

    @ConfigProperty(name = "app.host")
    String hostUrl;

    @ConfigProperty(name = "app.max.webhook.path")
    String webhookPath;

    @ConfigProperty(name = "app.max.webhook.secret")
    String webhookSecret;

    @ConfigProperty(name = "app.max.webhook.enabled", defaultValue = "true")
    boolean webhookEnabled;

    @PostConstruct
    @Override
    public void registerWebhook() {
        if (!webhookEnabled) {
            log.info("Max webhook registration is disabled");
            return;
        }

        var fullWebhookUrl = hostUrl + webhookPath;
        var request = MaxSetSubscriptionRequest.forAllUpdateTypes(fullWebhookUrl, webhookSecret);

        try (var response = botApi.setSubscription(request)) {
            log.info("Registering Max webhook: url={}", fullWebhookUrl);

            if (response.getStatus() == 200) {
                log.info("Max subscription registered successfully");
            } else {
                log.error("Failed to register Max subscription: status={}", response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error during Max subscription unregistration", e);
        }
    }

    @Shutdown
    @Override
    public void unregisterWebhook() {
        if (!webhookEnabled) {
            log.info("Max webhook unregistration is disabled");
            return;
        }

        if (hostUrl == null || hostUrl.isBlank()) {
            log.warn("Host URL is not configured, skipping webhook registration");
            return;
        }

        try {
            var fullWebhookUrl = hostUrl + webhookPath;
            log.info("Unregistering Max webhook: url={}", fullWebhookUrl);

            var response = botApi.deleteSubscription(fullWebhookUrl);

            if (response.success()) {
                log.info("Max subscription unregistered successfully");
            } else {
                log.error("Failed to unregister Max subscription: {}", response.message());
            }
        } catch (Exception e) {
            log.error("Error during Max subscription registration", e);
        }
    }
}
