package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramMessageBotApi;
import ru.mitrohinayulya.zabotushka.dto.telegram.SendMessageRequest;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformMessageService;

@ApplicationScoped
public class TelegramMessageService implements PlatformMessageService {

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageService.class);

    @Inject
    @RestClient
    TelegramMessageBotApi telegramMessageBotApi;

    @Override
    public void sendMessage(Long chatId, String text) {
        try {
            var request = SendMessageRequest.of(chatId, text);
            var response = telegramMessageBotApi.sendMessage(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Message sent successfully: chatId={}", chatId);
            } else {
                log.error("Failed to send message: chatId={}, description={}",
                        chatId, response.description());
            }
        } catch (Exception e) {
            log.error("Error sending message: chatId={}", chatId, e);
        }
    }
}
