package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.config.MaxChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.max.AuthorizedMaxUserService;
import ru.mitrohinayulya.zabotushka.service.max.MaxService;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class MaxQualificationProcessor extends AbstractPlatformQualificationProcessor<AuthorizedMaxUser> {

    private AuthorizedMaxUserService authorizedMaxUserService;
    private MaxService maxService;

    MaxQualificationProcessor() {}

    @Inject
    public MaxQualificationProcessor(AuthorizedMaxUserService authorizedMaxUserService,
                                     MaxService maxService) {
        this.authorizedMaxUserService = authorizedMaxUserService;
        this.maxService = maxService;
    }

    @Override
    public Platform platform() {
        return Platform.MAX;
    }

    @Override
    protected List<Long> chatIds() {
        return Arrays.stream(MaxChatGroupRequirements.values())
                .map(MaxChatGroupRequirements::getChatId)
                .toList();
    }

    @Override
    protected AuthorizedMaxUser findAuthorizedUser(Long platformUserId) {
        return authorizedMaxUserService.findByMaxId(platformUserId);
    }

    @Override
    protected Long getPlatformUserId(AuthorizedMaxUser user) {
        return user.maxId;
    }

    @Override
    protected Long getGreenwayId(AuthorizedMaxUser user) {
        return user.greenwayId;
    }

    @Override
    protected void checkAndRemoveIfNotQualified(Long chatId, Long userId, Long greenwayId) {
        maxService.checkAndRemoveIfNotQualified(chatId, userId, greenwayId);
    }

    @Override
    protected void removeMemberFromChat(Long chatId, Long userId) {
        maxService.removeMemberFromChat(chatId, userId);
    }
}
