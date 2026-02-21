package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.config.TelegramChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.service.telegram.AuthorizedTelegramUserService;
import ru.mitrohinayulya.zabotushka.service.telegram.TelegramService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramQualificationProcessorTest {

    @Mock
    AuthorizedTelegramUserService authorizedTelegramUserService;

    @Mock
    TelegramService telegramService;

    @InjectMocks
    TelegramQualificationProcessor processor;

    MockedStatic<UserGroupMembership> membershipMock;

    @BeforeEach
    void setUp() {
        membershipMock = mockStatic(UserGroupMembership.class);
    }

    @AfterEach
    void tearDown() {
        membershipMock.close();
    }

    @Test
    @DisplayName("processQualifications checks authorized user and updates lastCheckedAt")
    void processQualifications_ShouldCheckAndTouchMembership_WhenUserStillQualified() {
        mockAllTelegramGroupsAsEmpty();
        var chatId = TelegramChatGroupRequirements.GOLD_CLUB.getChatId();
        var membership = spy(membership(12345L, chatId));
        doNothing().when(membership).persist();
        var user = telegramUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId, Platform.TELEGRAM))
                .thenReturn(List.of(membership));
        membershipMock.when(() -> UserGroupMembership.exists(12345L, chatId, Platform.TELEGRAM))
                .thenReturn(true);
        when(authorizedTelegramUserService.findByTelegramId(12345L)).thenReturn(user);

        var result = processor.processQualifications();

        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId, 12345L, 999888L);
        assertThat(membership.lastCheckedAt).as("lastCheckedAt should be updated").isNotNull();
        verify(membership).persist();
        assertThat(result).as("stats should match").isEqualTo(new QualificationProcessStats(1, 0, 0, 0));
    }

    @Test
    @DisplayName("processQualifications removes orphaned membership when user is missing")
    void processQualifications_ShouldRemoveOrphanedMembership_WhenUserNotAuthorized() {
        mockAllTelegramGroupsAsEmpty();
        var chatId = TelegramChatGroupRequirements.GOLD_CLUB.getChatId();
        var membership = spy(membership(12345L, chatId));
        doNothing().when(membership).delete();

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId, Platform.TELEGRAM))
                .thenReturn(List.of(membership));
        when(authorizedTelegramUserService.findByTelegramId(12345L)).thenReturn(null);

        var result = processor.processQualifications();

        verify(telegramService, times(1)).removeMemberFromChat(chatId, 12345L);
        verify(membership).delete();
        verify(telegramService, never()).checkAndRemoveIfNotQualified(any(), any(), any());
        assertThat(result).as("stats should match").isEqualTo(new QualificationProcessStats(1, 1, 1, 0));
    }

    @Test
    @DisplayName("processQualifications does not update timestamp when membership removed by moderation")
    void processQualifications_ShouldNotTouchMembership_WhenMembershipWasRemoved() {
        mockAllTelegramGroupsAsEmpty();
        var chatId = TelegramChatGroupRequirements.GOLD_CLUB.getChatId();
        var membership = spy(membership(12345L, chatId));
        var user = telegramUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId, Platform.TELEGRAM))
                .thenReturn(List.of(membership));
        membershipMock.when(() -> UserGroupMembership.exists(12345L, chatId, Platform.TELEGRAM))
                .thenReturn(false);
        when(authorizedTelegramUserService.findByTelegramId(12345L)).thenReturn(user);

        var result = processor.processQualifications();

        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId, 12345L, 999888L);
        verify(membership, never()).persist();
        assertThat(result).as("stats should match").isEqualTo(new QualificationProcessStats(1, 1, 0, 0));
    }

    @Test
    @DisplayName("processQualifications continues when one member fails")
    void processQualifications_ShouldContinue_WhenSingleMemberFails() {
        mockAllTelegramGroupsAsEmpty();
        var chatId = TelegramChatGroupRequirements.GOLD_CLUB.getChatId();
        var firstMembership = spy(membership(111L, chatId));
        var secondMembership = spy(membership(222L, chatId));
        doNothing().when(secondMembership).persist();
        var firstUser = telegramUser(111L, 1001L);
        var secondUser = telegramUser(222L, 1002L);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId, Platform.TELEGRAM))
                .thenReturn(List.of(firstMembership, secondMembership));
        membershipMock.when(() -> UserGroupMembership.exists(222L, chatId, Platform.TELEGRAM))
                .thenReturn(true);
        when(authorizedTelegramUserService.findByTelegramId(111L)).thenReturn(firstUser);
        when(authorizedTelegramUserService.findByTelegramId(222L)).thenReturn(secondUser);
        doThrow(new RuntimeException("failure"))
                .when(telegramService).checkAndRemoveIfNotQualified(chatId, 111L, 1001L);

        var result = processor.processQualifications();

        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId, 111L, 1001L);
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId, 222L, 1002L);
        verify(secondMembership).persist();
        assertThat(result).as("stats should match").isEqualTo(new QualificationProcessStats(2, 0, 0, 1));
    }

    private void mockAllTelegramGroupsAsEmpty() {
        for (var group : TelegramChatGroupRequirements.values()) {
            membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(group.getChatId(), Platform.TELEGRAM))
                    .thenReturn(Collections.emptyList());
        }
    }

    private AuthorizedTelegramUser telegramUser(Long telegramId, Long greenwayId) {
        var user = new AuthorizedTelegramUser();
        user.id = UUID.randomUUID();
        user.telegramId = telegramId;
        user.greenwayId = greenwayId;
        user.regDate = "2023-01-15";
        user.creationDate = LocalDateTime.now();
        return user;
    }

    private UserGroupMembership membership(Long platformUserId, Long chatId) {
        var membership = new UserGroupMembership();
        membership.id = UUID.randomUUID();
        membership.platformUserId = platformUserId;
        membership.chatId = chatId;
        membership.platform = Platform.TELEGRAM;
        membership.joinedAt = LocalDateTime.now();
        return membership;
    }
}
