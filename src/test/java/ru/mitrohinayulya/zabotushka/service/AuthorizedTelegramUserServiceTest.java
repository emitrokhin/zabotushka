package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;
import ru.mitrohinayulya.zabotushka.service.telegram.AuthorizedTelegramUserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class AuthorizedTelegramUserServiceTest {

    @Inject
    AuthorizedTelegramUserService authorizedTelegramUserService;

    @Test
    @TestTransaction
    @DisplayName("findAll returns all users when multiple users exist")
    void findAll_ShouldReturnAllUsers_WhenMultipleUsersExist() {
        authorizedTelegramUserService.saveAuthorizedUser(11111L, 999888L, "2023-01-15");
        authorizedTelegramUserService.saveAuthorizedUser(22222L, 888999L, "2023-02-20");
        authorizedTelegramUserService.saveAuthorizedUser(33333L, 777666L, "2023-03-25");

        var users = authorizedTelegramUserService.findAll();

        assertThat(users).as("Should return exactly 3 users").hasSize(3);
        assertThat(users.stream().map(u -> u.telegramId).toList())
                .as("Should contain all saved Telegram IDs")
                .containsExactlyInAnyOrder(11111L, 22222L, 33333L);
    }

    @Test
    @TestTransaction
    @DisplayName("findAll returns empty list when no users exist")
    void findAll_ShouldReturnEmptyList_WhenNoUsersExist() {
        var users = authorizedTelegramUserService.findAll();

        assertThat(users).as("Should return empty list when no users saved").isEmpty();
    }

    @Test
    @TestTransaction
    @DisplayName("findAll returns single user with correct fields when one user exists")
    void findAll_ShouldReturnSingleUser_WhenOneUserExists() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        var users = authorizedTelegramUserService.findAll();

        assertThat(users).as("Should return exactly 1 user").hasSize(1);
        assertThat(users.getFirst().telegramId).as("Telegram ID should match the saved value").isEqualTo(12345L);
        assertThat(users.getFirst().greenwayId).as("Greenway ID should match the saved value").isEqualTo(999888L);
        assertThat(users.getFirst().regDate).as("Registration date should match the saved value").isEqualTo("2023-01-15");
    }

    @Test
    @TestTransaction
    @DisplayName("saveAuthorizedUser persists user and appears in findAll result")
    void saveAuthorizedUser_ShouldPersistUser_WhenDataIsValid() {
        var savedUser = authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        assertThat(savedUser).as("Saved user should not be null").isNotNull();
        assertThat(savedUser.id).as("Saved user should have generated ID").isNotNull();
        assertThat(savedUser.telegramId).as("Telegram ID should match the provided value").isEqualTo(12345L);

        var allUsers = authorizedTelegramUserService.findAll();
        assertThat(allUsers).as("findAll should return the saved user").hasSize(1);
        assertThat(allUsers.getFirst().telegramId).as("findAll result should have matching Telegram ID").isEqualTo(savedUser.telegramId);
    }

    @Test
    @TestTransaction
    @DisplayName("findByTelegramId returns user when user exists")
    void findByTelegramId_ShouldReturnUser_WhenUserExists() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        var foundUser = authorizedTelegramUserService.findByTelegramId(12345L);

        assertThat(foundUser).as("Found user should not be null").isNotNull();
        assertThat(foundUser.telegramId).as("Telegram ID should match the requested value").isEqualTo(12345L);
    }

    @Test
    @TestTransaction
    @DisplayName("existsByTelegramId returns true when user exists")
    void existsByTelegramId_ShouldReturnTrue_WhenUserExists() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        assertThat(authorizedTelegramUserService.existsByTelegramId(12345L))
                .as("Should return true for existing Telegram ID").isTrue();
        assertThat(authorizedTelegramUserService.findAll())
                .as("findAll should still return the user").hasSize(1);
    }

    @Test
    @TestTransaction
    @DisplayName("matchesStoredData returns true when data matches and false when it differs")
    void matchesStoredData_ShouldReturnCorrectResult_WhenDataCompared() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        assertThat(authorizedTelegramUserService.matchesStoredData(12345L, 999888L, "2023-01-15"))
                .as("Should return true when all data matches").isTrue();
        assertThat(authorizedTelegramUserService.matchesStoredData(12345L, 111111L, "2023-01-15"))
                .as("Should return false when Greenway ID differs").isFalse();
    }

    @Test
    @TestTransaction
    @DisplayName("saveAuthorizedUser throws exception and does not save when Greenway ID already exists")
    void saveAuthorizedUser_ShouldThrowException_WhenGreenwayIdAlreadyExists() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        assertThatThrownBy(() -> authorizedTelegramUserService.saveAuthorizedUser(67890L, 999888L, "2023-02-20"))
                .as("Exception should be thrown when Greenway ID is already associated")
                .isInstanceOf(GreenwayIdAlreadyExistsException.class)
                .hasMessageContaining("999888")
                .hasMessageContaining("already associated");

        var allUsers = authorizedTelegramUserService.findAll();
        assertThat(allUsers).as("Only original user should remain after failed save").hasSize(1);
        assertThat(allUsers.getFirst().telegramId).as("Remaining user should be the original one").isEqualTo(12345L);
    }

    @Test
    @TestTransaction
    @DisplayName("existsByGreenwayId returns true for existing Greenway ID and false for non-existing")
    void existsByGreenwayId_ShouldReturnCorrectResult_WhenChecked() {
        authorizedTelegramUserService.saveAuthorizedUser(12345L, 999888L, "2023-01-15");

        assertThat(authorizedTelegramUserService.existsByGreenwayId(999888L))
                .as("Should return true for existing Greenway ID").isTrue();
        assertThat(authorizedTelegramUserService.existsByGreenwayId(777777L))
                .as("Should return false for non-existing Greenway ID").isFalse();
    }
}
