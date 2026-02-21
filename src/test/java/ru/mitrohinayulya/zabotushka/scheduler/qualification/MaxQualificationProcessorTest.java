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
import ru.mitrohinayulya.zabotushka.config.MaxChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.service.max.AuthorizedMaxUserService;
import ru.mitrohinayulya.zabotushka.service.max.MaxService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaxQualificationProcessorTest {

    @Mock
    AuthorizedMaxUserService authorizedMaxUserService;

    @Mock
    MaxService maxService;

    @InjectMocks
    MaxQualificationProcessor processor;

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
    @DisplayName("processQualifications checks authorized max users and updates lastCheckedAt")
    void processQualifications_ShouldCheckAndTouchMembership_WhenUserStillQualified() {
        mockAllMaxGroupsAsEmpty();
        var chatId = MaxChatGroupRequirements.GOLD_CLUB.getChatId();
        var membership = spy(membership(12345L, chatId));
        doNothing().when(membership).persist();
        var user = maxUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId, Platform.MAX))
                .thenReturn(List.of(membership));
        membershipMock.when(() -> UserGroupMembership.exists(12345L, chatId, Platform.MAX))
                .thenReturn(true);
        when(authorizedMaxUserService.findByMaxId(12345L)).thenReturn(user);

        var result = processor.processQualifications();

        verify(maxService, times(1)).checkAndRemoveIfNotQualified(chatId, 12345L, 999888L);
        assertThat(membership.lastCheckedAt).as("lastCheckedAt should be updated").isNotNull();
        verify(membership).persist();
        assertThat(result).as("stats should match").isEqualTo(new QualificationProcessStats(1, 0, 0, 0));
    }

    @Test
    @DisplayName("processQualifications removes orphaned max membership when user is missing")
    void processQualifications_ShouldRemoveOrphanedMembership_WhenUserNotAuthorized() {
        mockAllMaxGroupsAsEmpty();
        var chatId = MaxChatGroupRequirements.GOLD_CLUB.getChatId();
        var membership = spy(membership(12345L, chatId));
        doNothing().when(membership).delete();

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId, Platform.MAX))
                .thenReturn(List.of(membership));
        when(authorizedMaxUserService.findByMaxId(12345L)).thenReturn(null);

        var result = processor.processQualifications();

        verify(maxService, times(1)).removeMemberFromChat(chatId, 12345L);
        verify(membership).delete();
        verify(maxService, never()).checkAndRemoveIfNotQualified(anyLong(), anyLong(), anyLong());
        assertThat(result).as("stats should match").isEqualTo(new QualificationProcessStats(1, 1, 1, 0));
    }

    @Test
    @DisplayName("processQualifications does not update timestamp when max membership was removed")
    void processQualifications_ShouldNotTouchMembership_WhenMembershipWasRemoved() {
        mockAllMaxGroupsAsEmpty();
        var chatId = MaxChatGroupRequirements.GOLD_CLUB.getChatId();
        var membership = spy(membership(12345L, chatId));
        var user = maxUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(chatId, Platform.MAX))
                .thenReturn(List.of(membership));
        membershipMock.when(() -> UserGroupMembership.exists(12345L, chatId, Platform.MAX))
                .thenReturn(false);
        when(authorizedMaxUserService.findByMaxId(12345L)).thenReturn(user);

        var result = processor.processQualifications();

        verify(maxService, times(1)).checkAndRemoveIfNotQualified(chatId, 12345L, 999888L);
        verify(membership, never()).persist();
        assertThat(result).as("stats should match").isEqualTo(new QualificationProcessStats(1, 1, 0, 0));
    }

    private void mockAllMaxGroupsAsEmpty() {
        for (var group : MaxChatGroupRequirements.values()) {
            membershipMock.when(() -> UserGroupMembership.findByChatIdAndPlatform(group.getChatId(), Platform.MAX))
                    .thenReturn(Collections.emptyList());
        }
    }

    private AuthorizedMaxUser maxUser(long maxId, long greenwayId) {
        var user = new AuthorizedMaxUser();
        user.id = UUID.randomUUID();
        user.maxId = maxId;
        user.greenwayId = greenwayId;
        user.regDate = "2023-01-15";
        user.creationDate = LocalDateTime.now();
        return user;
    }

    private UserGroupMembership membership(long platformUserId, long chatId) {
        var membership = new UserGroupMembership();
        membership.id = UUID.randomUUID();
        membership.platformUserId = platformUserId;
        membership.chatId = chatId;
        membership.platform = Platform.MAX;
        membership.joinedAt = LocalDateTime.now();
        return membership;
    }
}
