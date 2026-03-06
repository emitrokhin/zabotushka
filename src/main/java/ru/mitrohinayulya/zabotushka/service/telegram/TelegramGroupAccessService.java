package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationResult;
import ru.mitrohinayulya.zabotushka.dto.telegram.GetChatMemberRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.SetChatMemberTagRequest;
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
    protected Optional<ChatGroupRequirements> findRequirements(long chatId) {
        return TelegramChatGroupRequirements.findByChatId(chatId)
                .map(TelegramChatGroupRequirements::getRequirements);
    }

    @Override
    public boolean isMemberOfChat(long chatId, long userId) {
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
    public void removeMemberFromChat(long chatId, long userId) {
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

    @Override
    protected void onUserRemainsQualified(long chatId, long userId, QualificationResult result) {
        var qualTag = result.rawQual();
        if (qualTag == null || qualTag.isBlank()) {
            log.warn("Member tag is absent: chatId={}, userId={}", chatId, userId);
            return;
        }
        try {
            var tagRequest = SetChatMemberTagRequest.of(chatId, userId, qualTag);
            var tagResponse = telegramAccessBotApi.setChatMemberTag(tagRequest);
            if (Boolean.TRUE.equals(tagResponse.ok())) {
                log.info("Member tag updated: chatId={}, userId={}, tag={}", chatId, userId, qualTag);
            } else {
                log.warn("Failed to update member tag: chatId={}, userId={}, tag={}, description={}",
                        chatId, userId, qualTag, tagResponse.description());
            }
        } catch (Exception e) {
            log.warn("Error updating member tag: chatId={}, userId={}, tag={}", chatId, userId, qualTag, e);
        }
    }
}
