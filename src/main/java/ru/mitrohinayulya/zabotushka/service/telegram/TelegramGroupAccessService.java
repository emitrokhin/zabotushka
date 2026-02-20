package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.telegram.GetChatMemberRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.UnbanChatMemberRequest;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.platform.AbstractGroupAccessService;

import java.util.Optional;

@ApplicationScoped
public class TelegramGroupAccessService extends AbstractGroupAccessService {

    private static final Logger log = LoggerFactory.getLogger(TelegramGroupAccessService.class);

    @Inject
    @RestClient
    TelegramAccessBotApi telegramAccessBotApi;

    @Inject
    TelegramMessageService messageService;

    @Override
    protected Platform getPlatform() {
        return Platform.TELEGRAM;
    }

    @Override
    protected Optional<ChatGroupRequirements> findRequirements(Long chatId) {
        return TelegramChatGroupRequirements.findByChatId(chatId)
                .map(TelegramChatGroupRequirements::getRequirements);
    }

    @Override
    public boolean isMemberOfChat(Long chatId, Long userId) {
        try {
            var request = GetChatMemberRequest.of(chatId, userId);
            var response = telegramAccessBotApi.getChatMember(request);

            if (Boolean.TRUE.equals(response.ok()) && response.result() != null) {
                return response.result().isMember();
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking chat membership: chatId={}, userId={}", chatId, userId, e);
            return false;
        }
    }

    @Override
    public void removeMemberFromChat(Long chatId, Long userId) {
        try {
            var unbanRequest = UnbanChatMemberRequest.of(chatId, userId);
            var unbanResponse = telegramAccessBotApi.unbanChatMember(unbanRequest);

            if (Boolean.TRUE.equals(unbanResponse.ok())) {
                log.info("User removed from Telegram chat: chatId={}, userId={}", chatId, userId);
                var chatName = TelegramChatGroupRequirements.resolveGroupName(chatId);
                var message = String.format("Вы были удалены из «%s», так как не соответствуете требованиям по квалификации", chatName);
                messageService.sendMessage(userId, message);
                membershipService.removeMembership(chatId, userId, Platform.TELEGRAM);
            } else {
                log.error("Failed to remove Telegram user from chat: chatId={}, userId={}, description={}",
                        chatId, userId, unbanResponse.description());
            }
        } catch (Exception e) {
            log.error("Error removing Telegram user from chat: chatId={}, userId={}", chatId, userId, e);
        }
    }
}
