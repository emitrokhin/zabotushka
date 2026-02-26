package ru.mitrohinayulya.zabotushka.service.vk;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedVkUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;
import ru.mitrohinayulya.zabotushka.mapper.AuthorizedVkUserMapper;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformAuthorizationService;

import java.time.LocalDateTime;

/// Service for managing authorized VK users
@ApplicationScoped
public class AuthorizedVkUserService implements PlatformAuthorizationService {

    @Inject
    AuthorizedVkUserMapper userMapper;

    @Override
    public boolean existsByPlatformId(long platformId) {
        return existsByVkId(platformId);
    }

    @Override
    public void saveUser(long platformId, long greenwayId, String regDate) {
        saveAuthorizedUser(platformId, greenwayId, regDate);
    }

    /// Checks if a user exists by vkId
    public boolean existsByVkId(long vkId) {
        return AuthorizedVkUser.existsByVkId(vkId);
    }

    /// Saves a new authorized user.
    /// Validates greenwayId uniqueness before saving to the VK table.
    /// @throws GreenwayIdAlreadyExistsException if the greenwayId is already in use
    @Transactional
    public AuthorizedVkUser saveAuthorizedUser(long vkId, long greenwayId, String regDate) {
        if (existsByGreenwayId(greenwayId)) {
            throw new GreenwayIdAlreadyExistsException(greenwayId);
        }

        var user = userMapper.toEntity(vkId, greenwayId, regDate, LocalDateTime.now());
        user.persist();
        return user;
    }

    /// Checks if the user data matches stored records in the DB.
    /// @return true if greenwayId and regDate match
    public boolean matchesStoredData(long vkId, long greenwayId, String regDate) {
        var user = findByVkId(vkId);
        if (user == null) {
            return false;
        }
        return user.greenwayId == greenwayId && user.regDate.equals(regDate);
    }

    /// Finds a user by vkId
    public AuthorizedVkUser findByVkId(long vkId) {
        return AuthorizedVkUser.findByVkId(vkId);
    }

    /// Checks if a user exists by greenwayId in the VK table
    public boolean existsByGreenwayId(long greenwayId) {
        return AuthorizedVkUser.existsByGreenwayId(greenwayId);
    }
}
