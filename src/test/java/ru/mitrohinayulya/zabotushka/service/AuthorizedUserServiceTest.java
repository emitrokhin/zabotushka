package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для AuthorizedUserService
 */
@QuarkusTest
class AuthorizedUserServiceTest {

    @Inject
    AuthorizedUserService authorizedUserService;

    @AfterEach
    @Transactional
    void cleanUp() {
        // Очищаем базу после каждого теста
        AuthorizedUser.deleteAll();
    }

    @Test
    @Transactional
    void testFindAll_WithUsers() {
        // Given: создаем нескольких пользователей
        authorizedUserService.saveAuthorizedUser(11111L, 999888L, "2023-01-15");
        authorizedUserService.saveAuthorizedUser(22222L, 888999L, "2023-02-20");
        authorizedUserService.saveAuthorizedUser(33333L, 777666L, "2023-03-25");

        // When: получаем всех пользователей
        var users = authorizedUserService.findAll();

        // Then: возвращаются все 3 пользователя
        assertNotNull(users);
        assertEquals(3, users.size());

        // Проверяем, что все пользователи присутствуют
        var telegramIds = users.stream().map(u -> u.telegramId).toList();
        assertTrue(telegramIds.contains(11111L));
        assertTrue(telegramIds.contains(22222L));
        assertTrue(telegramIds.contains(33333L));
    }

    @Test
    @Transactional
    void testFindAll_NoUsers() {
        // Given: база данных пуста

        // When: получаем всех пользователей
        var users = authorizedUserService.findAll();

        // Then: возвращается пустой список
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @Transactional
    void testFindAll_SingleUser() {
        // Given: создаем одного пользователя
        authorizedUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        // When: получаем всех пользователей
        var users = authorizedUserService.findAll();

        // Then: возвращается один пользователь
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(12345L, users.getFirst().telegramId);
        assertEquals(999888L, users.getFirst().greenwayId);
        assertEquals("2023-01-15", users.getFirst().regDate);
    }

    @Test
    @Transactional
    void testSaveAuthorizedUser_AndFindAll() {
        // Given: пустая база данных

        // When: сохраняем пользователя
        var savedUser = authorizedUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        // Then: пользователь успешно сохранен
        assertNotNull(savedUser);
        assertNotNull(savedUser.id);
        assertEquals(12345L, savedUser.telegramId);

        // И появляется в списке всех пользователей
        var allUsers = authorizedUserService.findAll();
        assertEquals(1, allUsers.size());
        assertEquals(savedUser.telegramId, allUsers.getFirst().telegramId);
    }

    @Test
    @Transactional
    void testFindByTelegramId_AndFindAll() {
        // Given: создаем пользователя
        authorizedUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        // When: находим пользователя по telegramId
        var foundUser = authorizedUserService.findByTelegramId(12345L);

        // Then: пользователь найден
        assertNotNull(foundUser);
        assertEquals(12345L, foundUser.telegramId);

        // И он присутствует в списке всех пользователей
        var allUsers = authorizedUserService.findAll();
        assertEquals(1, allUsers.size());
        assertEquals(foundUser.telegramId, allUsers.getFirst().telegramId);
    }

    @Test
    @Transactional
    void testExistsByTelegramId_AfterSave() {
        // Given: создаем пользователя
        authorizedUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        // When: проверяем существование
        var exists = authorizedUserService.existsByTelegramId(12345L);

        // Then: пользователь существует
        assertTrue(exists);

        // И присутствует в списке всех
        var allUsers = authorizedUserService.findAll();
        assertEquals(1, allUsers.size());
    }

    @Test
    @Transactional
    void testMatchesStoredData() {
        // Given: создаем пользователя
        authorizedUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        // When: проверяем совпадение данных
        var matches = authorizedUserService.matchesStoredData(12345L, 999888L, "2023-01-15");
        var notMatches = authorizedUserService.matchesStoredData(12345L, 111111L, "2023-01-15");

        // Then: данные совпадают/не совпадают корректно
        assertTrue(matches);
        assertFalse(notMatches);
    }

    @Test
    @Transactional
    void testSaveAuthorizedUser_ThrowsException_WhenGreenwayIdAlreadyExists() {
        // Given: создаем пользователя с greenwayId 999888
        authorizedUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        // When/Then: попытка создать другого пользователя с тем же greenwayId выбрасывает исключение
        var exception = assertThrows(
                GreenwayIdAlreadyExistsException.class,
                () -> authorizedUserService.saveAuthorizedUser(67890L, 999888L, "2023-02-20")
        );

        // Проверяем сообщение об ошибке
        assertTrue(exception.getMessage().contains("999888"));
        assertTrue(exception.getMessage().contains("already associated"));

        // Проверяем, что в БД остался только первый пользователь
        var allUsers = authorizedUserService.findAll();
        assertEquals(1, allUsers.size());
        assertEquals(12345L, allUsers.getFirst().telegramId);
    }

    @Test
    @Transactional
    void testExistsByGreenwayId() {
        // Given: создаем пользователя
        authorizedUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        // When/Then: проверяем существование по greenwayId
        assertTrue(authorizedUserService.existsByGreenwayId(999888L));
        assertFalse(authorizedUserService.existsByGreenwayId(777777L));
    }
}
