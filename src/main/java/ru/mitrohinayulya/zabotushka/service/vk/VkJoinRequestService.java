package ru.mitrohinayulya.zabotushka.service.vk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.VkChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.vk.VkUpdate;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedVkUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.platform.AbstractJoinRequestService;

import java.util.Optional;

@ApplicationScoped
public class VkJoinRequestService extends AbstractJoinRequestService<VkUpdate, AuthorizedVkUser> {

    @Inject
    AuthorizedVkUserService authorizedUserService;

    @Inject
    VkGroupAccessService moderationService;

    public void processGroupJoin(VkUpdate update) {
        process(update);
    }

    @Override
    protected long extractChatId(VkUpdate event) {
        return event.groupId();
    }

    @Override
    protected long extractUserId(VkUpdate event) {
        return event.object().get("user_id").asLong();
    }

    @Override
    protected String extractUsername(VkUpdate event) {
        return String.valueOf(event.object().get("user_id").asLong());
    }

    @Override
    protected Optional<ChatGroupRequirements> findRequirements(long chatId) {
        return VkChatGroupRequirements.findByChatId(chatId)
                .map(VkChatGroupRequirements::getRequirements);
    }

    @Override
    protected AuthorizedVkUser findAuthorizedUser(long platformUserId) {
        return authorizedUserService.findByVkId(platformUserId);
    }

    @Override
    protected long getGreenwayId(AuthorizedVkUser user) {
        return user.greenwayId;
    }

    @Override
    protected void onApproved(VkUpdate event, AuthorizedVkUser user, ChatGroupRequirements req) {
        membershipService.saveMembership(event.groupId(), user.vkId, Platform.VK);
    }

    @Override
    protected void onDeclined(VkUpdate event) {
        moderationService.removeMemberFromChat(event.groupId(), extractUserId(event));
    }
}
