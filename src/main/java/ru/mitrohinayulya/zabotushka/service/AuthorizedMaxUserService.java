package ru.mitrohinayulya.zabotushka.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с авторизованными Max пользователями
 */
@ApplicationScoped
public class AuthorizedMaxUserService implements PlatformAuthorizationService {

    @Override
    public boolean existsByPlatformId(Long platformId) {
        return existsByMaxId(platformId);
    }

    @Override
    public void saveUser(Long platformId, Long greenwayId, String regDate) {
        saveAuthorizedUser(platformId, greenwayId, regDate);
    }

    /**
     * Проверяет существование пользователя по maxId
     */
    public boolean existsByMaxId(Long maxId) {
        return AuthorizedMaxUser.existsByMaxId(maxId);
    }

    /**
     * Сохраняет нового авторизованного пользователя
     * Проверяет уникальность greenwayId перед сохранением (across both platforms)
     *
     * @throws GreenwayIdAlreadyExistsException если greenwayId уже используется
     */
    @Transactional
    public AuthorizedMaxUser saveAuthorizedUser(Long maxId, Long greenwayId, String regDate) {
        if (existsByGreenwayId(greenwayId)) {
            throw new GreenwayIdAlreadyExistsException(greenwayId);
        }

        var user = new AuthorizedMaxUser();
        user.maxId = maxId;
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
    public boolean matchesStoredData(Long maxId, Long greenwayId, String regDate) {
        var user = findByMaxId(maxId);
        if (user == null) {
            return false;
        }
        return user.greenwayId.equals(greenwayId) && user.regDate.equals(regDate);
    }

    /**
     * Поиск пользователя по maxId
     */
    public AuthorizedMaxUser findByMaxId(Long maxId) {
        return AuthorizedMaxUser.findByMaxId(maxId);
    }

    /**
     * Получает всех авторизованных пользователей
     */
    public List<AuthorizedMaxUser> findAll() {
        return AuthorizedMaxUser.listAll();
    }

    /**
     * Проверка существования пользователя по greenwayId в таблице Max
     */
    public boolean existsByGreenwayId(Long greenwayId) {
        return AuthorizedMaxUser.existsByGreenwayId(greenwayId);
    }
}
