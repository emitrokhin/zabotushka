package ru.mitrohinayulya.zabotushka.service.telegram;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;
import ru.mitrohinayulya.zabotushka.mapper.AuthorizedTelegramUserMapper;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformAuthorizationService;

import java.time.LocalDateTime;
import java.util.List;

/// Сервис для работы с авторизованными Telegram пользователями
@ApplicationScoped
public class AuthorizedTelegramUserService implements PlatformAuthorizationService {

    @Inject
    AuthorizedTelegramUserMapper userMapper;

    @Override
    public boolean existsByPlatformId(long platformId) {
        return existsByTelegramId(platformId);
    }

    @Override
    public void saveUser(long platformId, long greenwayId, String regDate) {
        saveAuthorizedUser(platformId, greenwayId, regDate);
    }

    /// Проверяет существование пользователя по telegramId
    public boolean existsByTelegramId(long telegramId) {
        return AuthorizedTelegramUser.existsByTelegramId(telegramId);
    }

    /// Сохраняет нового авторизованного пользователя
    /// Проверяет уникальность greenwayId перед сохранением в таблице Telegram
    /// @throws GreenwayIdAlreadyExistsException если greenwayId уже используется
    @Transactional
    public AuthorizedTelegramUser saveAuthorizedUser(long telegramId, long greenwayId, String regDate) {
        if (existsByGreenwayId(greenwayId)) {
            throw new GreenwayIdAlreadyExistsException(greenwayId);
        }

        var user = userMapper.toEntity(telegramId, greenwayId, regDate, LocalDateTime.now());
        user.persist();
        return user;
    }

    /// Проверяет совпадение данных пользователя с сохраненными в БД
    /// @return true если greenwayId и regDate совпадают
    public boolean matchesStoredData(long telegramId, long greenwayId, String regDate) {
        var user = findByTelegramId(telegramId);
        if (user == null) {
            return false;
        }
        return user.greenwayId == greenwayId && user.regDate.equals(regDate);
    }


    /// Поиск пользователя по telegramId
    public AuthorizedTelegramUser findByTelegramId(long telegramId) {
        return AuthorizedTelegramUser.findByTelegramId(telegramId);
    }


    /// Получает всех авторизованных пользователей
    public List<AuthorizedTelegramUser> findAll() {
        return AuthorizedTelegramUser.listAll();
    }

    /// Проверка существования пользователя по greenwayId в таблице Telegram
    public boolean existsByGreenwayId(long greenwayId) {
        return AuthorizedTelegramUser.existsByGreenwayId(greenwayId);
    }
}
