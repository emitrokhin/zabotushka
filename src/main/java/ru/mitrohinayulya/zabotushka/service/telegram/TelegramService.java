package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.dto.telegram.ChatJoinRequest;

/// Фасад для операций Telegram платформы.
/// @see TelegramWebhookRegistrar.
@ApplicationScoped
public class TelegramService {

    @Inject
    TelegramJoinRequestService joinRequestService;

    @Inject
    TelegramGroupAccessService moderationService;

    public void processChatJoinRequest(ChatJoinRequest chatJoinRequest) {
        joinRequestService.processChatJoinRequest(chatJoinRequest);
    }

    public boolean isMemberOfChat(long chatId, long userId) {
        return moderationService.isMemberOfChat(chatId, userId);
    }

    public void removeMemberFromChat(long chatId, long userId) {
        moderationService.removeMemberFromChat(chatId, userId);
    }

    public void checkAndRemoveIfNotQualified(long chatId, long userId, long greenwayId) {
        moderationService.checkAndRemoveIfNotQualified(chatId, userId, greenwayId);
    }
}
