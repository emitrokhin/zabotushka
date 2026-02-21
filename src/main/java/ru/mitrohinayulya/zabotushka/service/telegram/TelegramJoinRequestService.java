package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.telegram.ApproveChatJoinRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.ChatJoinRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.DeclineChatJoinRequest;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.platform.AbstractJoinRequestService;

import java.util.Optional;

@ApplicationScoped
public class TelegramJoinRequestService extends AbstractJoinRequestService<ChatJoinRequest, AuthorizedTelegramUser> {

    private static final Logger log = LoggerFactory.getLogger(TelegramJoinRequestService.class);

    @Inject
    @RestClient
    TelegramAccessBotApi telegramAccessBotApi;

    @Inject
    AuthorizedTelegramUserService authorizedUserService;

    @Inject
    TelegramMessageService messageService;

    public void processChatJoinRequest(ChatJoinRequest chatJoinRequest) {
        process(chatJoinRequest);
    }

    @Override
    protected long extractChatId(ChatJoinRequest event) {
        return event.chat().id();
    }

    @Override
    protected long extractUserId(ChatJoinRequest event) {
        return event.from().id();
    }

    @Override
    protected String extractUsername(ChatJoinRequest event) {
        return event.from().username();
    }

    @Override
    protected Optional<ChatGroupRequirements> findRequirements(long chatId) {
        return TelegramChatGroupRequirements.findByChatId(chatId)
                .map(TelegramChatGroupRequirements::getRequirements);
    }

    @Override
    protected AuthorizedTelegramUser findAuthorizedUser(long platformUserId) {
        return authorizedUserService.findByTelegramId(platformUserId);
    }

    @Override
    protected long getGreenwayId(AuthorizedTelegramUser user) {
        return user.greenwayId;
    }

    @Override
    protected void onApproved(ChatJoinRequest event, AuthorizedTelegramUser user, ChatGroupRequirements req) {
        var chatId = event.chat().id();
        var userId = event.from().id();
        try {
            var request = ApproveChatJoinRequest.of(chatId, userId);
            var response = telegramAccessBotApi.approveChatJoinRequest(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Join request approved successfully: chatId={}, userId={}", chatId, userId);
                var message = String.format("Ваш запрос на вступление в «%s» одобрен", req.getGroupName());
                messageService.sendMessage(userId, message);
                membershipService.saveMembership(chatId, userId, Platform.TELEGRAM);
            } else {
                log.error("Failed to approve join request: chatId={}, userId={}, description={}",
                        chatId, userId, response.description());
            }
        } catch (Exception e) {
            log.error("Error approving join request: chatId={}, userId={}", chatId, userId, e);
        }
    }

    @Override
    protected void onDeclined(ChatJoinRequest event) {
        var chatId = event.chat().id();
        var userId = event.from().id();
        try {
            var request = DeclineChatJoinRequest.of(chatId, userId);
            var response = telegramAccessBotApi.declineChatJoinRequest(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Join request declined successfully: chatId={}, userId={}", chatId, userId);
                var chatName = TelegramChatGroupRequirements.resolveGroupName(chatId);
                var message = String.format("Ваш запрос на вступление в «%s» отклонен. Условия не выполнены", chatName);
                messageService.sendMessage(userId, message);
            } else {
                log.error("Failed to decline join request: chatId={}, userId={}, description={}",
                        chatId, userId, response.description());
            }
        } catch (Exception e) {
            log.error("Error declining join request: chatId={}, userId={}", chatId, userId, e);
        }
    }
}
