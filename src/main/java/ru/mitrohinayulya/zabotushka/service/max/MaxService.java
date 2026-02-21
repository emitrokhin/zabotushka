package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.dto.max.MaxUpdate;

/**
 * Фасад для операций Max платформы.
 * Webhook-регистрация управляется в {@link ru.mitrohinayulya.zabotushka.service.max.MaxWebhookRegistrar}.
 */
@ApplicationScoped
public class MaxService {

    @Inject
    MaxJoinRequestService joinRequestService;

    @Inject
    MaxGroupAccessService moderationService;

    public void processUserAddedUpdate(MaxUpdate update) {
        joinRequestService.processUserAddedUpdate(update);
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
