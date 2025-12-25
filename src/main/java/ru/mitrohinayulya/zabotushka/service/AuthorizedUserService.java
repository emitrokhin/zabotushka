package ru.mitrohinayulya.zabotushka.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedUser;

import java.time.LocalDateTime;

/**
 * Сервис для работы с авторизованными пользователями
 */
@ApplicationScoped
public class AuthorizedUserService {

    /**
     * Проверяет существование пользователя по telegramId
     */
    public boolean existsByTelegramId(Long telegramId) {
        return AuthorizedUser.existsByTelegramId(telegramId);
    }

    /**
     * Сохраняет нового авторизованного пользователя
     */
    @Transactional
    public AuthorizedUser saveAuthorizedUser(Long telegramId, Long greenwayId, String regDate) {
        var user = new AuthorizedUser();
        user.telegramId = telegramId;
        user.greenwayId = greenwayId;
        user.regDate = regDate;
        user.creationDate = LocalDateTime.now();
        user.persist();
        return user;
    }

    /**
     * Проверяет совпадение данных пользователя с сохраненными в БД
     * @return true если greenwayId и regDate совпадают
     */
    public boolean matchesStoredData(Long telegramId, Long greenwayId, String regDate) {
        var user = findByTelegramId(telegramId);
        if (user == null) {
            return false;
        }
        return user.greenwayId.equals(greenwayId) && user.regDate.equals(regDate);
    }

    /**
     * Поиск пользователя по telegramId
     */
    public AuthorizedUser findByTelegramId(Long telegramId) {
        return AuthorizedUser.findByTelegramId(telegramId);
    }
}
