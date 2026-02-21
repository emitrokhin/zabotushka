package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.dto.max.MaxSendMessageRequest;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformMessageService;

@ApplicationScoped
public class MaxMessageService implements PlatformMessageService {

    private static final Logger log = LoggerFactory.getLogger(MaxMessageService.class);

    @Inject
    @RestClient
    MaxBotApi botApi;

    @Override
    public void sendMessage(long userId, String text) {
        var request = MaxSendMessageRequest.withText(text);
        try (var response = botApi.sendMessage(userId, request)) {
            if (response.getStatus() == 200) {
                log.info("Max message sent successfully: userId={}", userId);
            } else {
                log.error("Failed to send max message: userId={}, status={}", userId, response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error sending max message: userId={}", userId, e);
        }
    }
}
