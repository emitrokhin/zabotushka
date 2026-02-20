package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;
import ru.mitrohinayulya.zabotushka.service.max.AuthorizedMaxUserService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для AuthorizedMaxUserService
 */
@QuarkusTest
class AuthorizedMaxUserServiceTest {

    @Inject
    AuthorizedMaxUserService authorizedMaxUserService;

    @AfterEach
    @Transactional
    void cleanUp() {
        AuthorizedMaxUser.deleteAll();
    }

    @Test
    @Transactional
    void testSaveAuthorizedUser_Success() {
        var savedUser = authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertNotNull(savedUser);
        assertNotNull(savedUser.id);
        assertEquals(5001L, savedUser.maxId);
        assertEquals(999888L, savedUser.greenwayId);
        assertEquals("2023-01-15", savedUser.regDate);
    }

    @Test
    @Transactional
    void testFindByMaxId() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        var foundUser = authorizedMaxUserService.findByMaxId(5001L);

        assertNotNull(foundUser);
        assertEquals(5001L, foundUser.maxId);
        assertEquals(999888L, foundUser.greenwayId);
    }

    @Test
    @Transactional
    void testExistsByMaxId() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertTrue(authorizedMaxUserService.existsByMaxId(5001L));
        assertFalse(authorizedMaxUserService.existsByMaxId(9999L));
    }

    @Test
    @Transactional
    void testMatchesStoredData() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertTrue(authorizedMaxUserService.matchesStoredData(5001L, 999888L, "2023-01-15"));
        assertFalse(authorizedMaxUserService.matchesStoredData(5001L, 111111L, "2023-01-15"));
    }

    @Test
    @Transactional
    void testFindAll() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");
        authorizedMaxUserService.saveAuthorizedUser(5002L, 888999L, "2023-02-20");

        var users = authorizedMaxUserService.findAll();

        assertEquals(2, users.size());
    }

    @Test
    @Transactional
    void testSaveAuthorizedUser_ThrowsException_WhenGreenwayIdAlreadyExistsInMaxTable() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        var exception = assertThrows(
                GreenwayIdAlreadyExistsException.class,
                () -> authorizedMaxUserService.saveAuthorizedUser(5002L, 999888L, "2023-02-20")
        );

        assertTrue(exception.getMessage().contains("999888"));
        assertTrue(exception.getMessage().contains("already associated"));

        var allUsers = authorizedMaxUserService.findAll();
        assertEquals(1, allUsers.size());
    }

    @Test
    @Transactional
    void testExistsByGreenwayId() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertTrue(authorizedMaxUserService.existsByGreenwayId(999888L));
        assertFalse(authorizedMaxUserService.existsByGreenwayId(777777L));
    }
}
