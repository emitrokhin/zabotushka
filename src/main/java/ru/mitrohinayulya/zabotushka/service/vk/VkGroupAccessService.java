package ru.mitrohinayulya.zabotushka.service.vk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.VkBotApi;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.VkChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.platform.AbstractGroupAccessService;

import java.util.Optional;

@ApplicationScoped
public class VkGroupAccessService extends AbstractGroupAccessService {

    private static final Logger log = LoggerFactory.getLogger(VkGroupAccessService.class);

    private static final long VK_CHAT_PEER_ID_OFFSET = 2_000_000_000L;

    @Inject
    @RestClient
    VkBotApi botApi;

    @Inject
    VkMessageService messageService;

    @Override
    protected Platform getPlatform() {
        return Platform.VK;
    }

    @Override
    protected Optional<ChatGroupRequirements> findRequirements(long chatId) {
        return VkChatGroupRequirements.findByChatId(chatId)
                .map(VkChatGroupRequirements::getRequirements);
    }

    @Override
    public boolean isMemberOfChat(long chatId, long userId) {
        try {
            var response = botApi.getConversationMembers(chatId, 0);
            if (response.response() == null || response.response().items() == null) {
                log.warn("Empty members response from VK: chatId={}, userId={}", chatId, userId);
                return false;
            }
            return response.response().items().stream()
                    .anyMatch(member -> member.memberId() == userId);
        } catch (Exception e) {
            log.error("Error checking VK chat membership: chatId={}, userId={}", chatId, userId, e);
            return false;
        }
    }

    @Override
    public void removeMemberFromChat(long chatId, long userId) {
        try {
            var localChatId = chatId - VK_CHAT_PEER_ID_OFFSET;
            var response = botApi.removeChatUser(localChatId, userId);

            if (response.isSuccess()) {
                log.info("User removed from VK chat: chatId={}, userId={}", chatId, userId);
                var chatName = VkChatGroupRequirements.resolveGroupName(chatId);
                var message = String.format("Вы были удалены из «%s», так как не соответствуете требованиям по квалификации", chatName);
                messageService.sendMessage(userId, message);
                membershipService.removeMembership(chatId, userId, Platform.VK);
            } else {
                log.error("Failed to remove VK user from chat: chatId={}, userId={}", chatId, userId);
            }
        } catch (Exception e) {
            log.error("Error removing VK user from chat: chatId={}, userId={}", chatId, userId, e);
        }
    }
}
