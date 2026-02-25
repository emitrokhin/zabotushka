package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.MaxChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.platform.AbstractGroupAccessService;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MaxGroupAccessService extends AbstractGroupAccessService {

    private static final Logger log = LoggerFactory.getLogger(MaxGroupAccessService.class);

    @Inject
    @RestClient
    MaxBotApi botApi;

    @Inject
    MaxMessageService messageService;

    @Override
    protected Platform getPlatform() {
        return Platform.MAX;
    }

    @Override
    protected Optional<ChatGroupRequirements> findRequirements(long chatId) {
        return MaxChatGroupRequirements.findByChatId(chatId)
                .map(MaxChatGroupRequirements::getRequirements);
    }

    @Override
    public boolean isMemberOfChat(long chatId, long userId) {
        try {
            var response = botApi.getChatMembers(chatId, List.of(userId));
            return !response.members().isEmpty();
        } catch (Exception e) {
            log.error("Error checking chat membership: chatId={}, userId={}", chatId, userId, e);
            return false;
        }
    }

    @Override
    public void removeMemberFromChat(long chatId, long userId) {
        try {
            var response = botApi.deleteChatMember(chatId, userId);

            if (response.success()) {
                log.info("Max user removed from chat: chatId={}, userId={}", chatId, userId);
                var chatName = MaxChatGroupRequirements.resolveGroupName(chatId);
                var message = String.format("Вы были удалены из «%s», так как не соответствуете требованиям по квалификации", chatName);
                messageService.sendMessage(userId, message);
                membershipService.removeMembership(chatId, userId, Platform.MAX);
            } else {
                log.error("Failed to remove max user from chat: chatId={}, userId={}, description={}",
                        chatId, userId, response.message());
            }
        } catch (Exception e) {
            log.error("Error removing max user from chat: chatId={}, userId={}", chatId, userId, e);
        }
    }
}
