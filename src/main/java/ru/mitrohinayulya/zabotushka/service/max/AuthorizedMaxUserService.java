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

/// Сервис для работы с авторизованными Max пользователями
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

    /// Проверяет существование пользователя по maxId
    public boolean existsByMaxId(long maxId) {
        return AuthorizedMaxUser.existsByMaxId(maxId);
    }

    /// Сохраняет нового авторизованного пользователя
    /// Проверяет уникальность greenwayId перед сохранением в таблице Max
    /// @throws GreenwayIdAlreadyExistsException если greenwayId уже используется
    @Transactional
    public AuthorizedMaxUser saveAuthorizedUser(long maxId, long greenwayId, String regDate) {
        if (existsByGreenwayId(greenwayId)) {
            throw new GreenwayIdAlreadyExistsException(greenwayId);
        }

        var user = userMapper.toEntity(maxId, greenwayId, regDate, LocalDateTime.now());
        user.persist();
        return user;
    }

    /// Проверяет совпадение данных пользователя с сохраненными в БД
    /// @return true если greenwayId и regDate совпадают
    public boolean matchesStoredData(long maxId, long greenwayId, String regDate) {
        var user = findByMaxId(maxId);
        if (user == null) {
            return false;
        }
        return user.greenwayId == greenwayId && user.regDate.equals(regDate);
    }

    /// Поиск пользователя по maxId
    public AuthorizedMaxUser findByMaxId(long maxId) {
        return AuthorizedMaxUser.findByMaxId(maxId);
    }

    /// Получает всех авторизованных пользователей
    public List<AuthorizedMaxUser> findAll() {
        return AuthorizedMaxUser.listAll();
    }

    /// Проверка существования пользователя по greenwayId в таблице Max
    public boolean existsByGreenwayId(long greenwayId) {
        return AuthorizedMaxUser.existsByGreenwayId(greenwayId);
    }
}
