package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformAuthorizationService;

import java.time.LocalDateTime;
import java.util.List;

/// Сервис для работы с авторизованными Telegram пользователями
@ApplicationScoped
public class AuthorizedTelegramUserService implements PlatformAuthorizationService {

    @Override
    public boolean existsByPlatformId(Long platformId) {
        return existsByTelegramId(platformId);
    }

    @Override
    public void saveUser(Long platformId, Long greenwayId, String regDate) {
        saveAuthorizedUser(platformId, greenwayId, regDate);
    }

    /// Проверяет существование пользователя по telegramId
    public boolean existsByTelegramId(Long telegramId) {
        return AuthorizedTelegramUser.existsByTelegramId(telegramId);
    }

    /// Сохраняет нового авторизованного пользователя
    /// Проверяет уникальность greenwayId перед сохранением в таблице Telegram
    /// @throws GreenwayIdAlreadyExistsException если greenwayId уже используется
    @Transactional
    public AuthorizedTelegramUser saveAuthorizedUser(Long telegramId, Long greenwayId, String regDate) {
        if (existsByGreenwayId(greenwayId)) {
            throw new GreenwayIdAlreadyExistsException(greenwayId);
        }

        var user = new AuthorizedTelegramUser();
        user.telegramId = telegramId;
        user.greenwayId = greenwayId;
        user.regDate = regDate;
        user.creationDate = LocalDateTime.now();
        user.persist();
        return user;
    }

    /// Проверяет совпадение данных пользователя с сохраненными в БД
    /// @return true если greenwayId и regDate совпадают
    public boolean matchesStoredData(Long telegramId, Long greenwayId, String regDate) {
        var user = findByTelegramId(telegramId);
        if (user == null) {
            return false;
        }
        return user.greenwayId.equals(greenwayId) && user.regDate.equals(regDate);
    }


    /// Поиск пользователя по telegramId
    public AuthorizedTelegramUser findByTelegramId(Long telegramId) {
        return AuthorizedTelegramUser.findByTelegramId(telegramId);
    }


    /// Получает всех авторизованных пользователей
    public List<AuthorizedTelegramUser> findAll() {
        return AuthorizedTelegramUser.listAll();
    }

    /// Проверка существования пользователя по greenwayId в таблице Telegram
    public boolean existsByGreenwayId(Long greenwayId) {
        return AuthorizedTelegramUser.existsByGreenwayId(greenwayId);
    }
}
