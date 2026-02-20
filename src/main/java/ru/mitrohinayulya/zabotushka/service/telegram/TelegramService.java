package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.dto.telegram.ChatJoinRequest;

/**
 * Фасад для операций Telegram платформы.
 * Webhook-регистрация управляется в {@link ru.mitrohinayulya.zabotushka.service.telegram.TelegramWebhookRegistrar}.
 */
@ApplicationScoped
public class TelegramService {

    @Inject
    TelegramJoinRequestService joinRequestService;

    @Inject
    TelegramGroupAccessService moderationService;

    public void processChatJoinRequest(ChatJoinRequest chatJoinRequest) {
        joinRequestService.processChatJoinRequest(chatJoinRequest);
    }

    public boolean isMemberOfChat(Long chatId, Long userId) {
        return moderationService.isMemberOfChat(chatId, userId);
    }

    public void removeMemberFromChat(Long chatId, Long userId) {
        moderationService.removeMemberFromChat(chatId, userId);
    }

    public void checkAndRemoveIfNotQualified(Long chatId, Long userId, Long greenwayId) {
        moderationService.checkAndRemoveIfNotQualified(chatId, userId, greenwayId);
    }
}
