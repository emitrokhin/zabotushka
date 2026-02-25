package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;
import ru.mitrohinayulya.zabotushka.mapper.AuthorizedMaxUserMapper;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformAuthorizationService;

import java.time.LocalDateTime;
import java.util.List;

/// Service for managing authorized Max users
@ApplicationScoped
public class AuthorizedMaxUserService implements PlatformAuthorizationService {

    @Inject
    AuthorizedMaxUserMapper userMapper;

    @Override
    public boolean existsByPlatformId(long platformId) {
        return existsByMaxId(platformId);
    }

    @Override
    public void saveUser(long platformId, long greenwayId, String regDate) {
        saveAuthorizedUser(platformId, greenwayId, regDate);
    }

    /// Checks if a user exists by maxId
    public boolean existsByMaxId(long maxId) {
        return AuthorizedMaxUser.existsByMaxId(maxId);
    }

    /// Saves a new authorized user
    /// Validates greenwayId uniqueness before saving to the Max table
    /// @throws GreenwayIdAlreadyExistsException if the greenwayId is already in use
    @Transactional
    public AuthorizedMaxUser saveAuthorizedUser(long maxId, long greenwayId, String regDate) {
        if (existsByGreenwayId(greenwayId)) {
            throw new GreenwayIdAlreadyExistsException(greenwayId);
        }

        var user = userMapper.toEntity(maxId, greenwayId, regDate, LocalDateTime.now());
        user.persist();
        return user;
    }

    /// Checks if the user data matches stored records in the DB
    /// @return true if greenwayId and regDate match
    public boolean matchesStoredData(long maxId, long greenwayId, String regDate) {
        var user = findByMaxId(maxId);
        if (user == null) {
            return false;
        }
        return user.greenwayId == greenwayId && user.regDate.equals(regDate);
    }

    /// Finds a user by maxId
    public AuthorizedMaxUser findByMaxId(long maxId) {
        return AuthorizedMaxUser.findByMaxId(maxId);
    }

    /// Returns all authorized users
    public List<AuthorizedMaxUser> findAll() {
        return AuthorizedMaxUser.listAll();
    }

    /// Checks if a user exists by greenwayId in the Max table
    public boolean existsByGreenwayId(long greenwayId) {
        return AuthorizedMaxUser.existsByGreenwayId(greenwayId);
    }
}
