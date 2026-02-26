package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.config.VkChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedVkUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.vk.AuthorizedVkUserService;
import ru.mitrohinayulya.zabotushka.service.vk.VkService;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class VkQualificationProcessor extends AbstractPlatformQualificationProcessor<AuthorizedVkUser> {

    private AuthorizedVkUserService authorizedVkUserService;
    private VkService vkService;

    VkQualificationProcessor() {}

    @Inject
    public VkQualificationProcessor(AuthorizedVkUserService authorizedVkUserService,
                                    VkService vkService) {
        this.authorizedVkUserService = authorizedVkUserService;
        this.vkService = vkService;
    }

    @Override
    public Platform platform() {
        return Platform.VK;
    }

    @Override
    protected List<Long> chatIds() {
        return Arrays.stream(VkChatGroupRequirements.values())
                .map(VkChatGroupRequirements::getChatId)
                .toList();
    }

    @Override
    protected AuthorizedVkUser findAuthorizedUser(long platformUserId) {
        return authorizedVkUserService.findByVkId(platformUserId);
    }

    @Override
    protected long getPlatformUserId(AuthorizedVkUser user) {
        return user.vkId;
    }

    @Override
    protected long getGreenwayId(AuthorizedVkUser user) {
        return user.greenwayId;
    }

    @Override
    protected void checkAndRemoveIfNotQualified(long chatId, long userId, long greenwayId) {
        vkService.checkAndRemoveIfNotQualified(chatId, userId, greenwayId);
    }

    @Override
    protected void removeMemberFromChat(long chatId, long userId) {
        vkService.removeMemberFromChat(chatId, userId);
    }
}
