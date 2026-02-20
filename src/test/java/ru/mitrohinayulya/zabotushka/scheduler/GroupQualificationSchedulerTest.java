package ru.mitrohinayulya.zabotushka.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.service.telegram.AuthorizedTelegramUserService;
import ru.mitrohinayulya.zabotushka.service.telegram.TelegramService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupQualificationSchedulerTest {

    @Mock
    AuthorizedTelegramUserService authorizedTelegramUserService;

    @Mock
    TelegramService telegramService;

    @InjectMocks
    GroupQualificationScheduler scheduler;

    private MockedStatic<UserGroupMembership> membershipMock;

    @BeforeEach
    void setUp() {
        membershipMock = mockStatic(UserGroupMembership.class);
    }

    @AfterEach
    void tearDown() {
        if (membershipMock != null) {
            membershipMock.close();
        }
    }

    @Test
    @DisplayName("checkGroupQualifications checks qualification for all members in all chats")
    void checkGroupQualifications_ShouldCheckAllMembers_WhenMembershipsExist() {
        var chatId1 = -1001968543887L;
        var chatId2 = -1001891048040L;

        var membership1 = createMembership(12345L, chatId1);
        var membership2 = createMembership(54321L, chatId1);
        var membership3 = createMembership(11111L, chatId2);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId1, Platform.TELEGRAM))
                .thenReturn(Arrays.asList(membership1, membership2));
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId2, Platform.TELEGRAM))
                .thenReturn(Collections.singletonList(membership3));
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(-1001835476759L, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(-1001811106801L, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(-1001929076200L, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.exists(anyLong(), anyLong(), any(Platform.class)))
                .thenReturn(true);

        var user1 = createUser(12345L, 999888L);
        var user2 = createUser(54321L, 888999L);
        var user3 = createUser(11111L, 777666L);

        when(authorizedTelegramUserService.findByTelegramId(12345L)).thenReturn(user1);
        when(authorizedTelegramUserService.findByTelegramId(54321L)).thenReturn(user2);
        when(authorizedTelegramUserService.findByTelegramId(11111L)).thenReturn(user3);

        scheduler.checkGroupQualifications();

        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 12345L, 999888L);
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 54321L, 888999L);
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId2, 11111L, 777666L);
        verify(membership1, times(1)).persist();
        verify(membership2, times(1)).persist();
        verify(membership3, times(1)).persist();
    }

    @Test
    @DisplayName("checkGroupQualifications skips qualification check when no memberships exist")
    void checkGroupQualifications_ShouldSkipQualificationCheck_WhenNoMembershipsExist() {
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(anyLong(), any(Platform.class)))
                .thenReturn(Collections.emptyList());

        scheduler.checkGroupQualifications();

        verify(authorizedTelegramUserService, never()).findByTelegramId(anyLong());
        verify(telegramService, never()).checkAndRemoveIfNotQualified(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("checkGroupQualifications removes member from chat when user is not authorized")
    void checkGroupQualifications_ShouldRemoveMemberFromChat_WhenUserIsNotAuthorized() {
        var chatId1 = -1001968543887L;
        var chatId2 = -1001891048040L;
        var chatId3 = -1001835476759L;
        var chatId4 = -1001811106801L;
        var chatId5 = -1001929076200L;
        var membership = createMembership(12345L, chatId1);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId1, Platform.TELEGRAM))
                .thenReturn(Collections.singletonList(membership));
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId2, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId3, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId4, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId5, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());

        when(authorizedTelegramUserService.findByTelegramId(12345L)).thenReturn(null);

        scheduler.checkGroupQualifications();

        verify(telegramService, times(1)).removeMemberFromChat(chatId1, 12345L);
        verify(membership, times(1)).delete();
        verify(telegramService, never()).checkAndRemoveIfNotQualified(anyLong(), anyLong(), anyLong());
        verify(membership, never()).persist();
    }

    @Test
    @DisplayName("checkGroupQualifications does not update timestamp when membership was removed externally")
    void checkGroupQualifications_ShouldNotUpdateTimestamp_WhenMembershipWasRemoved() {
        var chatId1 = -1001968543887L;
        var chatId2 = -1001891048040L;
        var chatId3 = -1001835476759L;
        var chatId4 = -1001811106801L;
        var chatId5 = -1001929076200L;
        var membership = createMembership(12345L, chatId1);
        var user = createUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId1, Platform.TELEGRAM))
                .thenReturn(Collections.singletonList(membership));
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId2, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId3, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId4, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId5, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.exists(12345L, chatId1, Platform.TELEGRAM))
                .thenReturn(false);

        when(authorizedTelegramUserService.findByTelegramId(12345L)).thenReturn(user);

        scheduler.checkGroupQualifications();

        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 12345L, 999888L);
        verify(membership, never()).persist();
    }

    @Test
    @DisplayName("checkGroupQualifications continues processing when an exception occurs for one member")
    void checkGroupQualifications_ShouldContinueProcessing_WhenExceptionOccurs() {
        var chatId1 = -1001968543887L;
        var chatId2 = -1001891048040L;
        var chatId3 = -1001835476759L;
        var chatId4 = -1001811106801L;
        var chatId5 = -1001929076200L;
        var membership = createMembership(12345L, chatId1);
        var user = createUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId1, Platform.TELEGRAM))
                .thenReturn(Collections.singletonList(membership));
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId2, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId3, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId4, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId5, Platform.TELEGRAM))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.exists(anyLong(), anyLong(), any(Platform.class)))
                .thenThrow(new RuntimeException("Database error"));

        when(authorizedTelegramUserService.findByTelegramId(12345L)).thenReturn(user);

        scheduler.checkGroupQualifications();

        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 12345L, 999888L);
    }

    private AuthorizedTelegramUser createUser(Long telegramId, Long greenwayId) {
        var user = new AuthorizedTelegramUser();
        user.id = UUID.randomUUID();
        user.telegramId = telegramId;
        user.greenwayId = greenwayId;
        user.regDate = "2023-01-15";
        user.creationDate = LocalDateTime.now();
        return user;
    }

    private UserGroupMembership createMembership(Long platformUserId, Long chatId) {
        var membership = mock(UserGroupMembership.class);
        membership.platformUserId = platformUserId;
        membership.chatId = chatId;
        membership.joinedAt = LocalDateTime.now();
        return membership;
    }
}
