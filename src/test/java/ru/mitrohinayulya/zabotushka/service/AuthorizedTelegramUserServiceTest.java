package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для AuthorizedTelegramUserService
 */
@QuarkusTest
class AuthorizedTelegramUserServiceTest {

    @Inject
    AuthorizedTelegramUserService authorizedTelegramUserService;

    @AfterEach
    @Transactional
    void cleanUp() {
        AuthorizedTelegramUser.deleteAll();
    }

    @Test
    @Transactional
    void testFindAll_WithUsers() {
        authorizedTelegramUserService.saveAuthorizedUser(11111L, 999888L, "2023-01-15");
        authorizedTelegramUserService.saveAuthorizedUser(22222L, 888999L, "2023-02-20");
        authorizedTelegramUserService.saveAuthorizedUser(33333L, 777666L, "2023-03-25");

        var users = authorizedTelegramUserService.findAll();

        assertNotNull(users);
        assertEquals(3, users.size());

        var telegramIds = users.stream().map(u -> u.telegramId).toList();
        assertTrue(telegramIds.contains(11111L));
        assertTrue(telegramIds.contains(22222L));
        assertTrue(telegramIds.contains(33333L));
    }

    @Test
    @Transactional
    void testFindAll_NoUsers() {
        var users = authorizedTelegramUserService.findAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @Transactional
    void testFindAll_SingleUser() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        var users = authorizedTelegramUserService.findAll();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(12345L, users.getFirst().telegramId);
        assertEquals(999888L, users.getFirst().greenwayId);
        assertEquals("2023-01-15", users.getFirst().regDate);
    }

    @Test
    @Transactional
    void testSaveAuthorizedUser_AndFindAll() {
        var savedUser = authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        assertNotNull(savedUser);
        assertNotNull(savedUser.id);
        assertEquals(12345L, savedUser.telegramId);

        var allUsers = authorizedTelegramUserService.findAll();
        assertEquals(1, allUsers.size());
        assertEquals(savedUser.telegramId, allUsers.getFirst().telegramId);
    }

    @Test
    @Transactional
    void testFindByTelegramId_AndFindAll() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        var foundUser = authorizedTelegramUserService.findByTelegramId(12345L);

        assertNotNull(foundUser);
        assertEquals(12345L, foundUser.telegramId);

        var allUsers = authorizedTelegramUserService.findAll();
        assertEquals(1, allUsers.size());
        assertEquals(foundUser.telegramId, allUsers.getFirst().telegramId);
    }

    @Test
    @Transactional
    void testExistsByTelegramId_AfterSave() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        var exists = authorizedTelegramUserService.existsByTelegramId(12345L);

        assertTrue(exists);

        var allUsers = authorizedTelegramUserService.findAll();
        assertEquals(1, allUsers.size());
    }

    @Test
    @Transactional
    void testMatchesStoredData() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        var matches = authorizedTelegramUserService.matchesStoredData(12345L, 999888L, "2023-01-15");
        var notMatches = authorizedTelegramUserService.matchesStoredData(12345L, 111111L, "2023-01-15");

        assertTrue(matches);
        assertFalse(notMatches);
    }

    @Test
    @Transactional
    void testSaveAuthorizedUser_ThrowsException_WhenGreenwayIdAlreadyExists() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        var exception = assertThrows(
                GreenwayIdAlreadyExistsException.class,
                () -> authorizedTelegramUserService.saveAuthorizedUser(67890L, 999888L, "2023-02-20")
        );

        assertTrue(exception.getMessage().contains("999888"));
        assertTrue(exception.getMessage().contains("already associated"));

        var allUsers = authorizedTelegramUserService.findAll();
        assertEquals(1, allUsers.size());
        assertEquals(12345L, allUsers.getFirst().telegramId);
    }

    @Test
    @Transactional
    void testExistsByGreenwayId() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        assertTrue(authorizedTelegramUserService.existsByGreenwayId(999888L));
        assertFalse(authorizedTelegramUserService.existsByGreenwayId(777777L));
    }
}
