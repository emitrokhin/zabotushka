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
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.config.MaxChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.max.MaxGetChatMemberRequest;
import ru.mitrohinayulya.zabotushka.dto.max.MaxSendMessageRequest;
import ru.mitrohinayulya.zabotushka.dto.max.MaxSubscriptionRequest;
import ru.mitrohinayulya.zabotushka.dto.max.MaxUpdate;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;

import java.time.LocalDateTime;
import java.util.List;

///
/// Сервис для работы с Max Bot API
///
@Startup
@ApplicationScoped
public class MaxService {

    private static final Logger log = LoggerFactory.getLogger(MaxService.class);

    @Inject
    @RestClient
    MaxBotApi botApi;

    @Inject
    GreenwayService greenwayService;

    @Inject
    AuthorizedMaxUserService authorizedUserService;

    @ConfigProperty(name = "app.host")
    String hostUrl;

    @ConfigProperty(name = "app.max.webhook.path")
    String webhookPath;

    @ConfigProperty(name = "app.max.webhook.secret")
    String webhookSecret;

    @ConfigProperty(name = "app.max.webhook.enabled", defaultValue = "true")
    boolean webhookEnabled;

    /// Регистрирует webhook в Telegram при старте приложения
    @PostConstruct
    public void registerWebhook() {
        if (!webhookEnabled) {
            log.info("Max webhook registration is disabled");
            return;
        }

        if (hostUrl == null || hostUrl.isBlank()) {
            log.warn("Host URL is not configured, skipping webhook registration");
            return;
        }

        var fullWebhookUrl = hostUrl + webhookPath;
        var request = MaxSubscriptionRequest.forAllUpdateTypes(fullWebhookUrl, webhookSecret);

        try (var response = botApi.setSubscription(request)) {
            log.info("Registering Max webhook: url={}", fullWebhookUrl);

            if (response.getStatus() == 200) {
                log.info("Max subscription registered successfully");
            } else {
                log.error("Failed to register Max subscription: status={}", response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error during Max subscription registration", e);
        }
    }

    /// Обрабатывает запрос на вступление в группу
    @Transactional
    public void processUserAddedUpdate(MaxUpdate update) {
        var chatId = update.chatId();
        var userId = update.user().userId();
        var username = update.user().username();

        log.info("Processing user added update: chatId={}, userId={}, username={}",
                chatId, userId, username);

        // Находим требования для группы
        var groupRequirements = MaxChatGroupRequirements.findByChatId(chatId);
        if (groupRequirements.isEmpty()) {
            log.warn("No requirements found for chatId={}, removing by default", chatId);
            removeMemberFromChat(chatId, userId);
            return;
        }

        // Проверяем, авторизован ли пользователь
        var authorizedUser = authorizedUserService.findByMaxId(userId);
        if (authorizedUser == null) {
            log.warn("Max user not authorized: userId={}, removing", userId);
            removeMemberFromChat(chatId, userId);
            return;
        }

        // Получаем лучшую квалификацию пользователя
        var greenwayId = authorizedUser.greenwayId;
        var qualification = getBestQualification(greenwayId);

        log.info("Max user qualification: userId={}, greenwayId={}, qualification={}",
                userId, greenwayId, qualification);

        // Проверяем соответствие квалификации требованиям группы
        if (groupRequirements.get().isQualificationAllowed(qualification)) {
            log.info("Max user userId={} successfully entered chatId={}. Qualification meets requirements.",
                    chatId, userId);
            saveMembership(chatId, userId);
        } else {
            log.info("Qualification does not meet requirements, declining join request: chatId={}, userId={}",
                    chatId, userId);
            removeMemberFromChat(chatId, userId);
        }
    }

    /// Получает лучшую квалификацию пользователя из текущего и предыдущего периодов
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

    /// Возвращает название клуба по chatId
    private String getChatGroupName(Long chatId) {
        if (chatId.equals(-1001968543887L)) return "Золотой клуб";
        if (chatId.equals(-1001891048040L)) return "Серебряный клуб";
        if (chatId.equals(-1001835476759L)) return "Бронзовый клуб";
        if (chatId.equals(-1001811106801L)) return "Могу себе позволить";
        if (chatId.equals(-1001929076200L)) return "Могу себе позволить chat";
        return "клуб";
    }

    /// Сохраняет информацию о членстве пользователя в группе
    private void saveMembership(Long chatId, Long userId) {
        try {
            if (!UserGroupMembership.exists(userId, chatId, Platform.MAX)) {
                var membership = new UserGroupMembership();
                membership.platformUserId = userId;
                membership.chatId = chatId;
                membership.platform = Platform.MAX;
                membership.joinedAt = LocalDateTime.now();
                membership.persist();

                log.info("Max membership saved: chatId={}, userId={}", chatId, userId);
            } else {
                log.debug("Max membership already exists: chatId={}, userId={}", chatId, userId);
            }
        } catch (Exception e) {
            log.error("Error saving max membership: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /// Отправляет сообщение пользователю
    private void sendMessage(Long chatId, String text) {
        var request = MaxSendMessageRequest.withText(text);
        try (var response = botApi.sendMessage(chatId, request)) {
            if (response.getStatus() == 200) {
                log.info("Max message sent successfully: chatId={}", chatId);
            } else {
                log.error("Failed to send max message: chatId={}", chatId);
            }
        } catch (Exception e) {
            log.error("Error sending max message: chatId={}", chatId, e);
        }
    }

    /// Проверяет, является ли пользователь участником группы Max
    public boolean isMemberOfChat(Long chatId, Long userId) {
        try {
            var singleMemberList = List.of(userId);
            var request = MaxGetChatMemberRequest.ofMemberList(singleMemberList);
            var response = botApi.getChatMembers(chatId, request);
            return !response.members().isEmpty();
        } catch (Exception e) {
            log.error("Error checking chat membership: chatId={}, userId={}", chatId, userId, e);
            return false;
        }
    }

    /// Удаляет пользователя из группы и отправляет ему уведомление
    public void removeMemberFromChat(Long chatId, Long userId) {
        try {
            var response = botApi.deleteChatMember(chatId, userId);

            if (response.success()) {
                log.info("Max user removed from chat: chatId={}, userId={}", chatId, userId);
                var chatName = getChatGroupName(chatId);
                var message = String.format("Вы были удалены из «%s», так как не соответствуете требованиям по квалификации", chatName);
                sendMessage(userId, message);

                // Удаляем информацию о членстве из БД
                removeMembershipFromDb(chatId, userId);
            } else {
                log.error("Failed to remove max user from chat: chatId={}, userId={}, description={}",
                        chatId, userId, response.message());
            }
        } catch (Exception e) {
            log.error("Error removing maxUser from chat: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /// Удаляет информацию о членстве пользователя в группе из БД
    private void removeMembershipFromDb(Long chatId, Long userId) {
        try {
            boolean removed = UserGroupMembership.removeMembership(userId, chatId, Platform.MAX);
            if (removed) {
                log.info("Membership removed from DB: platform={}, chatId={}, userId={}", Platform.MAX, chatId, userId);
            } else {
                log.warn("Membership not found in DB: platform={}, chatId={}, userId={}", Platform.MAX, chatId, userId);
            }
        } catch (Exception e) {
            log.error("Error removing membership from DB: platform={}, chatId={}, userId={}", Platform.MAX, chatId, userId, e);
        }
    }

    /// Проверяет соответствие квалификации пользователя требованиям группы
    /// и удаляет его, если квалификация не соответствует
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
