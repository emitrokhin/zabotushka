package ru.mitrohinayulya.zabotushka.service.vk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.vk.VkUpdate;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

/// Facade for VK platform operations.
/// @see VkWebhookRegistrar
@ApplicationScoped
public class VkService {

    private static final Logger log = LoggerFactory.getLogger(VkService.class);

    @Inject
    VkJoinRequestService joinRequestService;

    @Inject
    VkGroupAccessService moderationService;

    @Inject
    PlatformGroupMembershipService membershipService;

    public void processGroupJoin(VkUpdate update) {
        joinRequestService.processGroupJoin(update);
    }

    /// Handles voluntary group leave: removes the membership record from DB.
    /// The user has already left — no kick API call needed.
    @Transactional
    public void processGroupLeave(VkUpdate update) {
        var userId = update.object().get("user_id").asLong();
        log.info("VK user left group voluntarily: groupId={}, userId={}", update.groupId(), userId);
        membershipService.removeMembership(update.groupId(), userId, Platform.VK);
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
