package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.client.TelegramMessageBotApi;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.telegram.*;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;

import java.time.LocalDateTime;

/**
 * Сервис для работы с Telegram Bot API
 */
@Startup
@ApplicationScoped
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    @Inject
    @RestClient
    TelegramAccessBotApi telegramAccessBotApi;

    @Inject
    @RestClient
    TelegramMessageBotApi telegramMessageBotApi;

    @Inject
    GreenwayService greenwayService;

    @Inject
    AuthorizedTelegramUserService authorizedUserService;

    @ConfigProperty(name = "app.host")
    String hostUrl;

    @ConfigProperty(name = "app.telegram.webhook.path")
    String webhookPath;

    @ConfigProperty(name = "app.telegram.webhook.secret")
    String webhookSecret;

    @ConfigProperty(name = "app.telegram.webhook.enabled", defaultValue = "true")
    boolean webhookEnabled;

    /**
     * Регистрирует webhook в Telegram при старте приложения
     */
    @PostConstruct
    public void registerWebhook() {
        if (!webhookEnabled) {
            log.info("Telegram webhook registration is disabled");
            return;
        }

        if (hostUrl == null || hostUrl.isBlank()) {
            log.warn("Host URL is not configured, skipping webhook registration");
            return;
        }

        var fullWebhookUrl = hostUrl + webhookPath;

        try {
            log.info("Registering Telegram webhook: url={}", fullWebhookUrl);

            var request = SetWebhookRequest.forChatJoinRequests(fullWebhookUrl, webhookSecret);
            var response = telegramAccessBotApi.setWebhook(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Telegram webhook registered successfully");
            } else {
                log.error("Failed to register Telegram webhook: {}", response.description());
            }
        } catch (Exception e) {
            log.error("Error during Telegram webhook registration", e);
        }
    }

    /**
     * Обрабатывает запрос на вступление в группу
     */
    @Transactional
    public void processChatJoinRequest(ChatJoinRequest chatJoinRequest) {
        var chatId = chatJoinRequest.chat().id();
        var userId = chatJoinRequest.from().id();
        var username = chatJoinRequest.from().username();

        log.info("Processing chat join request: chatId={}, userId={}, username={}",
                chatId, userId, username);

        // Находим требования для группы
        var groupRequirements = TelegramChatGroupRequirements.findByChatId(chatId);
        if (groupRequirements.isEmpty()) {
            log.warn("No requirements found for chatId={}, declining by default", chatId);
            declineJoinRequest(chatId, userId);
            return;
        }

        // Проверяем, авторизован ли пользователь
        var authorizedUser = authorizedUserService.findByTelegramId(userId);
        if (authorizedUser == null) {
            log.warn("Telegram user not authorized: userId={}, declining join request", userId);
            declineJoinRequest(chatId, userId);
            return;
        }

        // Получаем лучшую квалификацию пользователя
        var greenwayId = authorizedUser.greenwayId;
        var qualification = getBestQualification(greenwayId);

        log.info("Telegram user qualification: userId={}, greenwayId={}, qualification={}",
                userId, greenwayId, qualification);

        // Проверяем соответствие квалификации требованиям группы
        if (groupRequirements.get().isQualificationAllowed(qualification)) {
            log.info("Qualification meets requirements, approving join request: chatId={}, userId={}",
                    chatId, userId);
            approveJoinRequest(chatId, userId);
        } else {
            log.info("Qualification does not meet requirements, declining join request: chatId={}, userId={}",
                    chatId, userId);
            declineJoinRequest(chatId, userId);
        }
    }

    /**
     * Получает лучшую квалификацию пользователя из текущего и предыдущего периодов
     */
    private QualificationLevel getBestQualification(Long greenwayId) {
        try {
            var previousPeriod = greenwayService.getPreviousPeriod();

            var currentPartnerList = greenwayService.getPartnerList(greenwayId, 0);
            var previousPartnerList = greenwayService.getPartnerList(greenwayId, previousPeriod);

            var currentQual = greenwayService.findPartnerById(currentPartnerList, greenwayId)
                    .map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var previousQual = greenwayService.findPartnerById(previousPartnerList, greenwayId)
                    .map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            return QualificationLevel.best(currentQual, previousQual);

        } catch (Exception e) {
            log.error("Error during qualification check: greenwayId={}", greenwayId, e);
            return QualificationLevel.NO;
        }
    }

    /**
     * Возвращает название клуба по chatId
     */
    private String getChatGroupName(Long chatId) {
        if (chatId.equals(-1001968543887L)) return "Золотой клуб";
        if (chatId.equals(-1001891048040L)) return "Серебряный клуб";
        if (chatId.equals(-1001835476759L)) return "Бронзовый клуб";
        if (chatId.equals(-1001811106801L)) return "Могу себе позволить";
        if (chatId.equals(-1001929076200L)) return "Могу себе позволить chat";
        return "клуб";
    }

    /**
     * Одобряет запрос на вступление и отправляет сообщение пользователю
     */
    private void approveJoinRequest(Long chatId, Long userId) {
        try {
            var request = ApproveChatJoinRequest.of(chatId, userId);
            var response = telegramAccessBotApi.approveChatJoinRequest(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Join request approved successfully: chatId={}, userId={}", chatId, userId);
                var chatName = getChatGroupName(chatId);
                var message = String.format("Ваш запрос на вступление в «%s» одобрен", chatName);
                sendMessage(userId, message);

                // Сохраняем информацию о членстве в БД
                saveMembership(chatId, userId);
            } else {
                log.error("Failed to approve join request: chatId={}, userId={}, description={}",
                        chatId, userId, response.description());
            }
        } catch (Exception e) {
            log.error("Error approving join request: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /**
     * Сохраняет информацию о членстве пользователя в группе
     */
    private void saveMembership(Long chatId, Long userId) {
        try {
            if (!UserGroupMembership.exists(userId, chatId, Platform.TELEGRAM)) {
                var membership = new UserGroupMembership();
                membership.platformUserId = userId;
                membership.chatId = chatId;
                membership.platform = Platform.TELEGRAM;
                membership.joinedAt = LocalDateTime.now();
                membership.persist();

                log.info("Telegram membership saved: chatId={}, userId={}", chatId, userId);
            } else {
                log.debug("Telegram membership already exists: chatId={}, userId={}", chatId, userId);
            }
        } catch (Exception e) {
            log.error("Error saving membership: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /**
     * Отклоняет запрос на вступление и отправляет сообщение пользователю
     */
    private void declineJoinRequest(Long chatId, Long userId) {
        try {
            var request = DeclineChatJoinRequest.of(chatId, userId);
            var response = telegramAccessBotApi.declineChatJoinRequest(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Join request declined successfully: chatId={}, userId={}", chatId, userId);
                var chatName = getChatGroupName(chatId);
                var message = String.format("Ваш запрос на вступление в «%s» отклонен. Условия не выполнены", chatName);
                sendMessage(userId, message);
            } else {
                log.error("Failed to decline join request: chatId={}, userId={}, description={}",
                        chatId, userId, response.description());
            }
        } catch (Exception e) {
            log.error("Error declining join request: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /**
     * Отправляет сообщение пользователю
     */
    private void sendMessage(Long chatId, String text) {
        try {
            var request = SendMessageRequest.of(chatId, text);
            var response = telegramMessageBotApi.sendMessage(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Message sent successfully: chatId={}", chatId);
            } else {
                log.error("Failed to send message: chatId={}, description={}",
                        chatId, response.description());
            }
        } catch (Exception e) {
            log.error("Error sending message: chatId={}", chatId, e);
        }
    }

    /**
     * Проверяет, является ли пользователь участником группы
     */
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

    /**
     * Удаляет пользователя из группы и отправляет ему уведомление
     * Использует только метод unbanChatMember для удаления
     */
    public void removeMemberFromChat(Long chatId, Long userId) {
        try {
            var unbanRequest = UnbanChatMemberRequest.of(chatId, userId);
            var unbanResponse = telegramAccessBotApi.unbanChatMember(unbanRequest);

            if (Boolean.TRUE.equals(unbanResponse.ok())) {
                log.info("MaxUser removed from chat: chatId={}, userId={}", chatId, userId);
                var chatName = getChatGroupName(chatId);
                var message = String.format("Вы были удалены из «%s», так как не соответствуете требованиям по квалификации", chatName);
                sendMessage(userId, message);

                // Удаляем информацию о членстве из БД
                removeMembershipFromDb(chatId, userId);
            } else {
                log.error("Failed to remove maxUser from chat: chatId={}, userId={}, description={}",
                        chatId, userId, unbanResponse.description());
            }
        } catch (Exception e) {
            log.error("Error removing maxUser from chat: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /**
     * Удаляет информацию о членстве пользователя в группе из БД
     */
    private void removeMembershipFromDb(Long chatId, Long userId) {
        try {
            boolean removed = UserGroupMembership.removeMembership(userId, chatId, Platform.TELEGRAM);
            if (removed) {
                log.info("Membership removed from DB: chatId={}, userId={}", chatId, userId);
            } else {
                log.warn("Membership not found in DB: chatId={}, userId={}", chatId, userId);
            }
        } catch (Exception e) {
            log.error("Error removing membership from DB: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /**
     * Проверяет соответствие квалификации пользователя требованиям группы
     * и удаляет его, если квалификация не соответствует
     */
    public void checkAndRemoveIfNotQualified(Long chatId, Long userId, Long greenwayId) {
        log.info("Checking qualification for maxUser: chatId={}, userId={}, greenwayId={}",
                chatId, userId, greenwayId);

        // Проверяем, является ли пользователь участником группы
        if (!isMemberOfChat(chatId, userId)) {
            log.info("MaxUser is not a member of chat (left by themselves): chatId={}, userId={}", chatId, userId);
            // Пользователь покинул группу сам - удаляем запись из БД
            removeMembershipFromDb(chatId, userId);
            return;
        }

        // Находим требования для группы
        var groupRequirements = TelegramChatGroupRequirements.findByChatId(chatId);
        if (groupRequirements.isEmpty()) {
            log.warn("No requirements found for chatId={}", chatId);
            return;
        }

        // Получаем лучшую квалификацию пользователя
        var qualification = getBestQualification(greenwayId);

        log.info("MaxUser qualification check: chatId={}, userId={}, greenwayId={}, qualification={}",
                chatId, userId, greenwayId, qualification);

        // Проверяем соответствие квалификации требованиям группы
        if (!groupRequirements.get().isQualificationAllowed(qualification)) {
            log.info("Qualification does not meet requirements, removing maxUser: chatId={}, userId={}, qualification={}",
                    chatId, userId, qualification);
            removeMemberFromChat(chatId, userId);
        } else {
            log.info("Qualification meets requirements: chatId={}, userId={}, qualification={}",
                    chatId, userId, qualification);
        }
    }
}
