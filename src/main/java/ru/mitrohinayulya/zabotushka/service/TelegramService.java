package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.TelegramApi;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.telegram.ApproveChatJoinRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.ChatJoinRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.DeclineChatJoinRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.SendMessageRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.SetWebhookRequest;

/**
 * Сервис для работы с Telegram Bot API
 */
@Startup
@ApplicationScoped
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final String APPROVED_MESSAGE = "Ваш запрос на вступление одобрен";
    private static final String DECLINED_MESSAGE = "Ваш запрос на вступление отклонен. Условия не выполнены";

    @Inject
    @RestClient
    TelegramApi telegramApi;

    @Inject
    GreenwayService greenwayService;

    @Inject
    AuthorizedUserService authorizedUserService;

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
            var response = telegramApi.setWebhook(request);

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
    public void processChatJoinRequest(ChatJoinRequest chatJoinRequest) {
        var chatId = chatJoinRequest.chat().id();
        var userId = chatJoinRequest.from().id();
        var username = chatJoinRequest.from().username();

        log.info("Processing chat join request: chatId={}, userId={}, username={}",
                chatId, userId, username);

        // Находим требования для группы
        var groupRequirements = ChatGroupRequirements.findByChatId(chatId);
        if (groupRequirements.isEmpty()) {
            log.warn("No requirements found for chatId={}, declining by default", chatId);
            declineJoinRequest(chatId, userId);
            return;
        }

        // Проверяем, авторизован ли пользователь
        var authorizedUser = authorizedUserService.findByTelegramId(userId);
        if (authorizedUser == null) {
            log.warn("User not authorized: userId={}, declining join request", userId);
            declineJoinRequest(chatId, userId);
            return;
        }

        // Получаем лучшую квалификацию пользователя
        var greenwayId = authorizedUser.greenwayId;
        var qualification = getBestQualification(greenwayId);

        log.info("User qualification: userId={}, greenwayId={}, qualification={}",
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
     * Одобряет запрос на вступление и отправляет сообщение пользователю
     */
    private void approveJoinRequest(Long chatId, Long userId) {
        try {
            var request = ApproveChatJoinRequest.of(chatId, userId);
            var response = telegramApi.approveChatJoinRequest(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Join request approved successfully: chatId={}, userId={}", chatId, userId);
                sendMessage(userId, APPROVED_MESSAGE);
            } else {
                log.error("Failed to approve join request: chatId={}, userId={}, description={}",
                        chatId, userId, response.description());
            }
        } catch (Exception e) {
            log.error("Error approving join request: chatId={}, userId={}", chatId, userId, e);
        }
    }

    /**
     * Отклоняет запрос на вступление и отправляет сообщение пользователю
     */
    private void declineJoinRequest(Long chatId, Long userId) {
        try {
            var request = DeclineChatJoinRequest.of(chatId, userId);
            var response = telegramApi.declineChatJoinRequest(request);

            if (Boolean.TRUE.equals(response.ok())) {
                log.info("Join request declined successfully: chatId={}, userId={}", chatId, userId);
                sendMessage(userId, DECLINED_MESSAGE);
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
            var response = telegramApi.sendMessage(request);

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
}
