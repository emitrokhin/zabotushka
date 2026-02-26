package ru.mitrohinayulya.zabotushka.service.vk;

import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.VkBotApi;
import ru.mitrohinayulya.zabotushka.service.platform.MessengerWebhookRegistrar;

@Startup
@ApplicationScoped
public class VkWebhookRegistrar implements MessengerWebhookRegistrar {

    private static final Logger log = LoggerFactory.getLogger(VkWebhookRegistrar.class);

    @Inject
    @RestClient
    VkBotApi botApi;

    @ConfigProperty(name = "app.host")
    String hostUrl;

    @ConfigProperty(name = "app.vk.group-id")
    long groupId;

    @ConfigProperty(name = "app.vk.webhook.path")
    String webhookPath;

    @ConfigProperty(name = "app.vk.webhook.secret")
    String webhookSecret;

    @ConfigProperty(name = "app.vk.webhook.enabled", defaultValue = "true")
    boolean webhookEnabled;

    private volatile int registeredServerId;

    @PostConstruct
    @Override
    public void registerWebhook() {
        if (!webhookEnabled) {
            log.info("VK webhook registration is disabled");
            return;
        }

        try {
            var fullWebhookUrl = hostUrl + webhookPath;
            log.info("Registering VK callback server: url={}, groupId={}", fullWebhookUrl, groupId);

            var addResponse = botApi.addCallbackServer(groupId, fullWebhookUrl, "zabotushka", webhookSecret);
            registeredServerId = addResponse.response().serverId();
            log.info("VK callback server added: serverId={}", registeredServerId);

            botApi.setCallbackSettings(groupId, registeredServerId, 1, 1);
            log.info("VK callback settings applied: groupJoin=1, groupLeave=1");
        } catch (Exception e) {
            log.error("Error during VK webhook registration", e);
        }
    }

    @Shutdown
    @Override
    public void unregisterWebhook() {
        if (!webhookEnabled || registeredServerId == 0) {
            log.info("VK webhook unregistration is skipped: enabled={}, serverId={}", webhookEnabled, registeredServerId);
            return;
        }

        try {
            log.info("Unregistering VK callback server: serverId={}, groupId={}", registeredServerId, groupId);
            botApi.deleteCallbackServer(groupId, registeredServerId);
            log.info("VK callback server deleted: serverId={}", registeredServerId);
        } catch (Exception e) {
            log.error("Error during VK webhook unregistration", e);
        }
    }
}
