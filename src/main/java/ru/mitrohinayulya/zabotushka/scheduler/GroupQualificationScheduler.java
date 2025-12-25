package ru.mitrohinayulya.zabotushka.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedUser;
import ru.mitrohinayulya.zabotushka.service.AuthorizedUserService;
import ru.mitrohinayulya.zabotushka.service.TelegramService;

/**
 * Планировщик для проверки квалификации пользователей в группах
 * Запускается каждый месяц 8 числа в 00:00
 */
@ApplicationScoped
public class GroupQualificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(GroupQualificationScheduler.class);

    @Inject
    AuthorizedUserService authorizedUserService;

    @Inject
    TelegramService telegramService;

    /**
     * Проверяет квалификацию всех авторизованных пользователей во всех группах
     * Запускается каждый месяц 8 числа в 00:00
     */
    @Scheduled(cron = "0 0 0 8 * ?")
    public void checkGroupQualifications() {
        log.info("Starting monthly group qualification check");

        try {
            // Получаем всех авторизованных пользователей
            var allUsers = authorizedUserService.findAll();
            log.info("Found {} authorized users to check", allUsers.size());

            // Проходим по всем пользователям
            for (var user : allUsers) {
                log.debug("Checking user: telegramId={}, greenwayId={}",
                         user.telegramId, user.greenwayId);

                // Проверяем пользователя во всех группах
                for (var group : ChatGroupRequirements.values()) {
                    checkUserQualificationInGroup(user, group);
                }
            }
        } catch (Exception e) {
            log.error("Error during monthly group qualification check", e);
        }
    }

    private void checkUserQualificationInGroup(AuthorizedUser user, ChatGroupRequirements group) {
        try {
            // Проверяем, является ли пользователь участником группы
            if (telegramService.isMemberOfChat(group.getChatId(), user.telegramId)) {
                log.debug("User is member of group: telegramId={}, chatId={}",
                         user.telegramId, group.getChatId());

                // Проверяем квалификацию и удаляем если не соответствует
                telegramService.checkAndRemoveIfNotQualified(
                        group.getChatId(),
                        user.telegramId,
                        user.greenwayId
                );
            }
        } catch (Exception e) {
            log.error("Error checking user in group: telegramId={}, chatId={}",
                     user.telegramId, group.getChatId(), e);
        }
    }
}
