package ru.mitrohinayulya.zabotushka.service.vk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.VkBotApi;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformMessageService;

import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class VkMessageService implements PlatformMessageService {

    private static final Logger log = LoggerFactory.getLogger(VkMessageService.class);

    @Inject
    @RestClient
    VkBotApi botApi;

    // ThreadLocalRandom is safe here because randomId is used
    // only as a deduplication identifier, not as a security token.
    @Override
    @SuppressWarnings("java:S2245")
    public void sendMessage(long userId, String text) {
        try {
            var randomId = ThreadLocalRandom.current().nextInt();
            botApi.sendMessage(userId, text, randomId);
            log.info("VK message sent successfully: userId={}", userId);
        } catch (Exception e) {
            log.error("Error sending VK message: userId={}", userId, e);
        }
    }
}
