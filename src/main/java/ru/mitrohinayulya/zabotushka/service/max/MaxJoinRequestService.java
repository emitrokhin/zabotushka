package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.MaxChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.max.MaxUpdate;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.platform.AbstractJoinRequestService;

import java.util.Optional;

@ApplicationScoped
public class MaxJoinRequestService extends AbstractJoinRequestService<MaxUpdate, AuthorizedMaxUser> {

    @Inject
    AuthorizedMaxUserService authorizedUserService;

    @Inject
    MaxGroupAccessService moderationService;

    public void processUserAddedUpdate(MaxUpdate update) {
        process(update);
    }

    @Override
    protected Long extractChatId(MaxUpdate event) {
        return event.chatId();
    }

    @Override
    protected Long extractUserId(MaxUpdate event) {
        return event.user().userId();
    }

    @Override
    protected String extractUsername(MaxUpdate event) {
        return event.user().username();
    }

    @Override
    protected Optional<ChatGroupRequirements> findRequirements(Long chatId) {
        return MaxChatGroupRequirements.findByChatId(chatId)
                .map(MaxChatGroupRequirements::getRequirements);
    }

    @Override
    protected AuthorizedMaxUser findAuthorizedUser(Long platformUserId) {
        return authorizedUserService.findByMaxId(platformUserId);
    }

    @Override
    protected Long getGreenwayId(AuthorizedMaxUser user) {
        return user.greenwayId;
    }

    @Override
    protected void onApproved(MaxUpdate event, AuthorizedMaxUser user, ChatGroupRequirements req) {
        membershipService.saveMembership(event.chatId(), user.maxId, Platform.MAX);
    }

    @Override
    protected void onDeclined(MaxUpdate event) {
        moderationService.removeMemberFromChat(event.chatId(), event.user().userId());
    }
}
