package ru.mitrohinayulya.zabotushka.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedUser;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.service.AuthorizedUserService;
import ru.mitrohinayulya.zabotushka.service.TelegramService;

import java.time.LocalDateTime;

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
     * Проверяет квалификацию пользователей в группах
     * Запускается каждый месяц 8 числа в 00:00
     * Оптимизировано: проверяет только тех пользователей, которые действительно состоят в группах
     */
    @Scheduled(cron = "0 0 0 8 * ?")
    @Transactional
    public void checkGroupQualifications() {
        log.info("Starting monthly group qualification check");

        try {
            int totalChecked = 0;
            int totalRemoved = 0;

            // Проходим по всем группам
            for (var group : ChatGroupRequirements.values()) {
                log.info("Checking group: chatId={}", group.getChatId());

                // Получаем всех членов группы из БД
                var memberships = UserGroupMembership.findByChatId(group.getChatId());
                log.info("Found {} members in group: chatId={}", memberships.size(), group.getChatId());

                // Проверяем каждого члена группы
                for (var membership : memberships) {
                    totalChecked++;

                    // Получаем данные авторизованного пользователя
                    var user = authorizedUserService.findByTelegramId(membership.telegramId);

                    if (user == null) {
                        log.warn("User not found in authorized users: telegramId={}, chatId={}",
                                membership.telegramId, group.getChatId());
                        continue;
                    }

                    log.debug("Checking user: telegramId={}, greenwayId={}, chatId={}",
                            user.telegramId, user.greenwayId, group.getChatId());

                    // Проверяем квалификацию и удаляем если не соответствует
                    boolean wasRemoved = checkUserQualificationInGroup(user, group);

                    if (wasRemoved) {
                        totalRemoved++;
                    } else {
                        // Обновляем время последней проверки только если пользователь не был удален
                        membership.lastCheckedAt = LocalDateTime.now();
                        membership.persist();
                    }
                }
            }

            log.info("Monthly group qualification check completed: totalChecked={}, totalRemoved={}",
                    totalChecked, totalRemoved);
        } catch (Exception e) {
            log.error("Error during monthly group qualification check", e);
        }
    }

    /**
     * Проверяет квалификацию пользователя в группе и удаляет если не соответствует
     * @return true если пользователь был удален, false в противном случае
     */
    private boolean checkUserQualificationInGroup(AuthorizedUser user, ChatGroupRequirements group) {
        try {
            // Вызываем метод проверки и удаления из TelegramService
            // Запись о членстве уже существует в БД, поэтому не нужна дополнительная проверка isMemberOfChat
            telegramService.checkAndRemoveIfNotQualified(
                    group.getChatId(),
                    user.telegramId,
                    user.greenwayId
            );

            // Проверяем, была ли удалена запись о членстве (это означает, что пользователь был удален)
            return !UserGroupMembership.exists(user.telegramId, group.getChatId());
        } catch (Exception e) {
            log.error("Error checking user in group: telegramId={}, chatId={}",
                     user.telegramId, group.getChatId(), e);
            return false;
        }
    }
}
