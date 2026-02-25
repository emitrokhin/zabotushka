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

/// Service for managing authorized Telegram users
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

    /// Checks if a user exists by telegramId
    public boolean existsByTelegramId(long telegramId) {
        return AuthorizedTelegramUser.existsByTelegramId(telegramId);
    }

    /// Saves a new authorized user
    /// Validates greenwayId uniqueness before saving to the Telegram table
    /// @throws GreenwayIdAlreadyExistsException if the greenwayId is already in use
    @Transactional
    public AuthorizedTelegramUser saveAuthorizedUser(long telegramId, long greenwayId, String regDate) {
        if (existsByGreenwayId(greenwayId)) {
            throw new GreenwayIdAlreadyExistsException(greenwayId);
        }

        var user = userMapper.toEntity(telegramId, greenwayId, regDate, LocalDateTime.now());
        user.persist();
        return user;
    }

    /// Checks if the user data matches stored records in the DB
    /// @return true if greenwayId and regDate match
    public boolean matchesStoredData(long telegramId, long greenwayId, String regDate) {
        var user = findByTelegramId(telegramId);
        if (user == null) {
            return false;
        }
        return user.greenwayId == greenwayId && user.regDate.equals(regDate);
    }


    /// Finds a user by telegramId
    public AuthorizedTelegramUser findByTelegramId(long telegramId) {
        return AuthorizedTelegramUser.findByTelegramId(telegramId);
    }


    /// Returns all authorized users
    public List<AuthorizedTelegramUser> findAll() {
        return AuthorizedTelegramUser.listAll();
    }

    /// Checks if a user exists by greenwayId in the Telegram table
    public boolean existsByGreenwayId(long greenwayId) {
        return AuthorizedTelegramUser.existsByGreenwayId(greenwayId);
    }
}
