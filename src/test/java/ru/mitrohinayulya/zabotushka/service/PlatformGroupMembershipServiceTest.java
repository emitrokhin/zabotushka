package ru.mitrohinayulya.zabotushka.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

import static org.junit.jupiter.api.Assertions.*;

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
    void saveMembership_PersistsNewMembership() {
        membershipService.saveMembership(-100L, 1L, Platform.TELEGRAM);

        assertTrue(UserGroupMembership.exists(1L, -100L, Platform.TELEGRAM));
    }

    @Test
    void saveMembership_SkipsDuplicate() {
        membershipService.saveMembership(-100L, 1L, Platform.TELEGRAM);
        membershipService.saveMembership(-100L, 1L, Platform.TELEGRAM);

        var members = UserGroupMembership.findByChatIdAndPlatform(-100L, Platform.TELEGRAM);
        assertEquals(1, members.size());
    }

    @Test
    void removeMembership_ReturnsTrueWhenExists() {
        membershipService.saveMembership(-100L, 1L, Platform.MAX);

        boolean removed = membershipService.removeMembership(-100L, 1L, Platform.MAX);

        assertTrue(removed);
        assertFalse(UserGroupMembership.exists(1L, -100L, Platform.MAX));
    }

    @Test
    void removeMembership_ReturnsFalseWhenNotExists() {
        boolean removed = membershipService.removeMembership(-999L, 999L, Platform.TELEGRAM);

        assertFalse(removed);
    }
}
