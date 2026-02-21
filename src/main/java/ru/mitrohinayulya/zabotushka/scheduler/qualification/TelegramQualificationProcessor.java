package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.telegram.AuthorizedTelegramUserService;
import ru.mitrohinayulya.zabotushka.service.telegram.TelegramService;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class TelegramQualificationProcessor extends AbstractPlatformQualificationProcessor<AuthorizedTelegramUser> {

    private AuthorizedTelegramUserService authorizedTelegramUserService;
    private TelegramService telegramService;

    TelegramQualificationProcessor() {}

    @Inject
    public TelegramQualificationProcessor(AuthorizedTelegramUserService authorizedTelegramUserService,
                                          TelegramService telegramService) {
        this.authorizedTelegramUserService = authorizedTelegramUserService;
        this.telegramService = telegramService;
    }

    @Override
    public Platform platform() {
        return Platform.TELEGRAM;
    }

    @Override
    protected List<Long> chatIds() {
        return Arrays.stream(TelegramChatGroupRequirements.values())
                .map(TelegramChatGroupRequirements::getChatId)
                .toList();
    }

    @Override
    protected AuthorizedTelegramUser findAuthorizedUser(Long platformUserId) {
        return authorizedTelegramUserService.findByTelegramId(platformUserId);
    }

    @Override
    protected Long getPlatformUserId(AuthorizedTelegramUser user) {
        return user.telegramId;
    }

    @Override
    protected Long getGreenwayId(AuthorizedTelegramUser user) {
        return user.greenwayId;
    }

    @Override
    protected void checkAndRemoveIfNotQualified(Long chatId, Long userId, Long greenwayId) {
        telegramService.checkAndRemoveIfNotQualified(chatId, userId, greenwayId);
    }

    @Override
    protected void removeMemberFromChat(Long chatId, Long userId) {
        telegramService.removeMemberFromChat(chatId, userId);
    }
}
