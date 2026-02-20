package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.exception.GreenwayIdAlreadyExistsException;
import ru.mitrohinayulya.zabotushka.service.max.AuthorizedMaxUserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class AuthorizedMaxUserServiceTest {

    @Inject
    AuthorizedMaxUserService authorizedMaxUserService;

    @Test
    @TestTransaction
    @DisplayName("saveAuthorizedUser persists user with correct fields")
    void saveAuthorizedUser_ShouldPersistUser_WhenDataIsValid() {
        var savedUser = authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertThat(savedUser).as("Saved user should not be null").isNotNull();
        assertThat(savedUser.id).as("Saved user should have generated ID").isNotNull();
        assertThat(savedUser.maxId).as("Max ID should match the provided value").isEqualTo(5001L);
        assertThat(savedUser.greenwayId).as("Greenway ID should match the provided value").isEqualTo(999888L);
        assertThat(savedUser.regDate).as("Registration date should match the provided value").isEqualTo("2023-01-15");
    }

    @Test
    @TestTransaction
    @DisplayName("findByMaxId returns user when user exists")
    void findByMaxId_ShouldReturnUser_WhenUserExists() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        var foundUser = authorizedMaxUserService.findByMaxId(5001L);

        assertThat(foundUser).as("Found user should not be null").isNotNull();
        assertThat(foundUser.maxId).as("Max ID should match the requested value").isEqualTo(5001L);
        assertThat(foundUser.greenwayId).as("Greenway ID should match the stored value").isEqualTo(999888L);
    }

    @Test
    @TestTransaction
    @DisplayName("existsByMaxId returns true for existing user and false for non-existing")
    void existsByMaxId_ShouldReturnCorrectResult_WhenChecked() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertThat(authorizedMaxUserService.existsByMaxId(5001L)).as("Should return true for existing Max ID").isTrue();
        assertThat(authorizedMaxUserService.existsByMaxId(9999L)).as("Should return false for non-existing Max ID").isFalse();
    }

    @Test
    @TestTransaction
    @DisplayName("matchesStoredData returns true when data matches and false when it differs")
    void matchesStoredData_ShouldReturnCorrectResult_WhenDataCompared() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertThat(authorizedMaxUserService.matchesStoredData(5001L, 999888L, "2023-01-15"))
                .as("Should return true when all data matches").isTrue();
        assertThat(authorizedMaxUserService.matchesStoredData(5001L, 111111L, "2023-01-15"))
                .as("Should return false when Greenway ID differs").isFalse();
    }

    @Test
    @TestTransaction
    @DisplayName("findAll returns all saved users")
    void findAll_ShouldReturnAllUsers_WhenMultipleUsersExist() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");
        authorizedMaxUserService.saveAuthorizedUser(5002L, 888999L, "2023-02-20");

        var users = authorizedMaxUserService.findAll();

        assertThat(users).as("Should return exactly 2 users").hasSize(2);
    }

    @Test
    @TestTransaction
    @DisplayName("saveAuthorizedUser throws exception and does not save when Greenway ID already exists")
    void saveAuthorizedUser_ShouldThrowException_WhenGreenwayIdAlreadyExists() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertThatThrownBy(() -> authorizedMaxUserService.saveAuthorizedUser(5002L, 999888L, "2023-02-20"))
                .as("Exception should be thrown when Greenway ID is already associated")
                .isInstanceOf(GreenwayIdAlreadyExistsException.class)
                .hasMessageContaining("999888")
                .hasMessageContaining("already associated");

        assertThat(authorizedMaxUserService.findAll()).as("Only original user should remain after failed save").hasSize(1);
    }

    @Test
    @TestTransaction
    @DisplayName("existsByGreenwayId returns true for existing Greenway ID and false for non-existing")
    void existsByGreenwayId_ShouldReturnCorrectResult_WhenChecked() {
        authorizedMaxUserService.saveAuthorizedUser(5001L, 999888L, "2023-01-15");

        assertThat(authorizedMaxUserService.existsByGreenwayId(999888L)).as("Should return true for existing Greenway ID").isTrue();
        assertThat(authorizedMaxUserService.existsByGreenwayId(777777L)).as("Should return false for non-existing Greenway ID").isFalse();
    }
}
