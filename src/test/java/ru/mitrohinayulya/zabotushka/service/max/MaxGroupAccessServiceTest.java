package ru.mitrohinayulya.zabotushka.service.max;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.max.MaxChatMember;
import ru.mitrohinayulya.zabotushka.dto.max.MaxDeleteChatMemberResponse;
import ru.mitrohinayulya.zabotushka.dto.max.MaxGetChatMemberResponse;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaxGroupAccessServiceTest {

    @Mock
    @RestClient
    MaxBotApi botApi;

    @Mock
    GreenwayQualificationService qualificationService;

    @Mock
    PlatformGroupMembershipService membershipService;

    @Mock
    MaxMessageService messageService;

    @InjectMocks
    MaxGroupAccessService moderationService;

    @Test
    @DisplayName("checkAndRemoveIfNotQualified only removes membership when user has already left the chat")
    void checkAndRemoveIfNotQualified_ShouldRemoveOnlyMembership_WhenUserHasLeft() {
        when(botApi.getChatMembers(anyLong(), anyList())).thenReturn(new MaxGetChatMemberResponse(Collections.emptyList()));

        moderationService.checkAndRemoveIfNotQualified(-71062621438079L, 123L, 456L);

        verify(membershipService).removeMembership(-71062621438079L, 123L, Platform.MAX);
        verify(qualificationService, never()).getBestQualification(anyLong());
        verify(botApi, never()).deleteChatMember(anyLong(), anyLong());
    }

    @Test
    @DisplayName("checkAndRemoveIfNotQualified removes user from chat when qualification is not allowed")
    void checkAndRemoveIfNotQualified_ShouldRemoveUser_WhenQualificationNotAllowed() {
        when(botApi.getChatMembers(anyLong(), anyList())).thenReturn(new MaxGetChatMemberResponse(
                List.of(mock(MaxChatMember.class))));
        when(qualificationService.getBestQualification(456L)).thenReturn(QualificationLevel.NO);
        when(botApi.deleteChatMember(-71062621438079L, 123L))
                .thenReturn(new MaxDeleteChatMemberResponse(true, null));

        moderationService.checkAndRemoveIfNotQualified(-71062621438079L, 123L, 456L);

        verify(botApi).deleteChatMember(-71062621438079L, 123L);
        verify(messageService).sendMessage(eq(123L), contains("удалены"));
        verify(membershipService).removeMembership(-71062621438079L, 123L, Platform.MAX);
    }

    @Test
    @DisplayName("checkAndRemoveIfNotQualified does not remove user when qualification is allowed")
    void checkAndRemoveIfNotQualified_ShouldNotRemoveUser_WhenQualificationAllowed() {
        when(botApi.getChatMembers(anyLong(), anyList())).thenReturn(new MaxGetChatMemberResponse(
                List.of(mock(MaxChatMember.class))));
        when(qualificationService.getBestQualification(456L)).thenReturn(QualificationLevel.M);

        moderationService.checkAndRemoveIfNotQualified(-71062621438079L, 123L, 456L);

        verify(botApi, never()).deleteChatMember(anyLong(), anyLong());
        verify(messageService, never()).sendMessage(anyLong(), anyString());
    }

    @Test
    @DisplayName("isMemberOfChat returns true when member list is non-empty")
    void isMemberOfChat_ShouldReturnTrue_WhenMemberExists() {
        when(botApi.getChatMembers(anyLong(), anyList())).thenReturn(new MaxGetChatMemberResponse(
                List.of(mock(MaxChatMember.class))));

        var result = moderationService.isMemberOfChat(-71062621438079L, 123L);

        assertThat(result).as("Should return true when member list is non-empty").isTrue();
    }

    @Test
    @DisplayName("isMemberOfChat returns false when member list is empty")
    void isMemberOfChat_ShouldReturnFalse_WhenMemberListIsEmpty() {
        when(botApi.getChatMembers(anyLong(), anyList())).thenReturn(new MaxGetChatMemberResponse(Collections.emptyList()));

        var result = moderationService.isMemberOfChat(-71062621438079L, 123L);

        assertThat(result).as("Should return false when member list is empty").isFalse();
    }
}
