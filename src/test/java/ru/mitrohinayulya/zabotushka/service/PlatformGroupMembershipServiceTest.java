package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PlatformGroupMembershipServiceTest {

    @Inject
    PlatformGroupMembershipService membershipService;

    @AfterEach
    @Transactional
    void cleanup() {
        UserGroupMembership.deleteAll();
    }

    @Test
    @DisplayName("saveMembership persists a new membership record")
    void saveMembership_ShouldPersistMembership_WhenNew() {
        membershipService.saveMembership(-100L, 1L, Platform.TELEGRAM);

        assertThat(UserGroupMembership.exists(1L, -100L, Platform.TELEGRAM))
                .as("Membership should exist after being saved").isTrue();
    }

    @Test
    @DisplayName("saveMembership does not create a duplicate when called twice with same data")
    void saveMembership_ShouldSkipDuplicate_WhenMembershipAlreadyExists() {
        membershipService.saveMembership(-100L, 1L, Platform.TELEGRAM);
        membershipService.saveMembership(-100L, 1L, Platform.TELEGRAM);

        var members = UserGroupMembership.findByChatIdAndPlatform(-100L, Platform.TELEGRAM);
        assertThat(members).as("Should have exactly 1 membership after duplicate save").hasSize(1);
    }

    @Test
    @DisplayName("removeMembership returns true and removes record when membership exists")
    void removeMembership_ShouldReturnTrue_WhenMembershipExists() {
        membershipService.saveMembership(-100L, 1L, Platform.MAX);

        var removed = membershipService.removeMembership(-100L, 1L, Platform.MAX);

        assertThat(removed).as("Should return true when membership was found and removed").isTrue();
        assertThat(UserGroupMembership.exists(1L, -100L, Platform.MAX))
                .as("Membership should no longer exist after removal").isFalse();
    }

    @Test
    @DisplayName("removeMembership returns false when membership does not exist")
    void removeMembership_ShouldReturnFalse_WhenMembershipNotExists() {
        var removed = membershipService.removeMembership(-999L, 999L, Platform.TELEGRAM);

        assertThat(removed).as("Should return false when membership was not found").isFalse();
    }
}
