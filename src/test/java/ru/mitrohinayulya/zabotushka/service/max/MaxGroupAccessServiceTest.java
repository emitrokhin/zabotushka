package ru.mitrohinayulya.zabotushka.service.max;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.max.MaxDeleteChatMemberResponse;
import ru.mitrohinayulya.zabotushka.dto.max.MaxGetChatMemberResponse;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    void testCheckAndRemoveIfNotQualified_RemovesOnlyMembershipWhenUserLeft() {
        when(botApi.getChatMembers(anyLong(), any())).thenReturn(new MaxGetChatMemberResponse(Collections.emptyList()));

        moderationService.checkAndRemoveIfNotQualified(-71062621438079L, 123L, 456L);

        verify(membershipService).removeMembership(-71062621438079L, 123L, Platform.MAX);
        verify(qualificationService, never()).getBestQualification(anyLong());
        verify(botApi, never()).deleteChatMember(anyLong(), anyLong());
    }

    @Test
    void testCheckAndRemoveIfNotQualified_RemovesUserWhenQualificationNotAllowed() {
        when(botApi.getChatMembers(anyLong(), any())).thenReturn(new MaxGetChatMemberResponse(
                java.util.List.of(mock(ru.mitrohinayulya.zabotushka.dto.max.MaxChatMember.class))));
        when(qualificationService.getBestQualification(456L)).thenReturn(QualificationLevel.NO);
        when(botApi.deleteChatMember(-71062621438079L, 123L))
                .thenReturn(new MaxDeleteChatMemberResponse(true, null));

        moderationService.checkAndRemoveIfNotQualified(-71062621438079L, 123L, 456L);

        verify(botApi).deleteChatMember(-71062621438079L, 123L);
        verify(messageService).sendMessage(eq(123L), contains("удалены"));
        verify(membershipService).removeMembership(-71062621438079L, 123L, Platform.MAX);
    }

    @Test
    void testCheckAndRemoveIfNotQualified_DoesNotRemoveWhenQualificationAllowed() {
        when(botApi.getChatMembers(anyLong(), any())).thenReturn(new MaxGetChatMemberResponse(
                java.util.List.of(mock(ru.mitrohinayulya.zabotushka.dto.max.MaxChatMember.class))));
        when(qualificationService.getBestQualification(456L)).thenReturn(QualificationLevel.M);

        moderationService.checkAndRemoveIfNotQualified(-71062621438079L, 123L, 456L);

        verify(botApi, never()).deleteChatMember(anyLong(), anyLong());
        verify(messageService, never()).sendMessage(anyLong(), anyString());
    }

    @Test
    void isMemberOfChat_ReturnsTrueWhenMemberExists() {
        when(botApi.getChatMembers(anyLong(), any())).thenReturn(new MaxGetChatMemberResponse(
                java.util.List.of(mock(ru.mitrohinayulya.zabotushka.dto.max.MaxChatMember.class))));

        var result = moderationService.isMemberOfChat(-71062621438079L, 123L);

        assertThat(result).isTrue();
    }

    @Test
    void isMemberOfChat_ReturnsFalseWhenEmpty() {
        when(botApi.getChatMembers(anyLong(), any())).thenReturn(new MaxGetChatMemberResponse(Collections.emptyList()));

        var result = moderationService.isMemberOfChat(-71062621438079L, 123L);

        assertThat(result).isFalse();
    }
}
