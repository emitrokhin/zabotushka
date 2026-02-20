package ru.mitrohinayulya.zabotushka.service.telegram;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.telegram.*;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramGroupAccessServiceTest {

    @Mock
    @RestClient
    TelegramAccessBotApi telegramAccessBotApi;

    @Mock
    GreenwayQualificationService qualificationService;

    @Mock
    PlatformGroupMembershipService membershipService;

    @Mock
    TelegramMessageService messageService;

    @InjectMocks
    TelegramGroupAccessService moderationService;

    @Test
    @DisplayName("isMemberOfChat returns true when user has 'member' status")
    void isMemberOfChat_ShouldReturnTrue_WhenUserIsMember() {
        var member = new ChatMember("member", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, member, null));

        var result = moderationService.isMemberOfChat(-1001968543887L, 123L);

        assertThat(result).as("Should return true when user status is 'member'").isTrue();
    }

    @Test
    @DisplayName("isMemberOfChat returns false when user has 'left' status")
    void isMemberOfChat_ShouldReturnFalse_WhenUserHasLeft() {
        var leftMember = new ChatMember("left", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, leftMember, null));

        var result = moderationService.isMemberOfChat(-1001968543887L, 123L);

        assertThat(result).as("Should return false when user status is 'left'").isFalse();
    }

    @Test
    @DisplayName("isMemberOfChat returns false when API response is not OK")
    void isMemberOfChat_ShouldReturnFalse_WhenApiResponseIsNotOk() {
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(false, null, "Not Found"));

        var result = moderationService.isMemberOfChat(-1001968543887L, 123L);

        assertThat(result).as("Should return false when API response is not OK").isFalse();
    }

    @Test
    @DisplayName("checkAndRemoveIfNotQualified only removes membership when user has already left")
    void checkAndRemoveIfNotQualified_ShouldRemoveOnlyMembership_WhenUserHasLeft() {
        var leftMember = new ChatMember("left", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, leftMember, null));

        moderationService.checkAndRemoveIfNotQualified(-1001968543887L, 123L, 456L);

        verify(membershipService).removeMembership(-1001968543887L, 123L, Platform.TELEGRAM);
        verify(qualificationService, never()).getBestQualification(anyLong());
        verify(telegramAccessBotApi, never()).unbanChatMember(any());
    }

    @Test
    @DisplayName("checkAndRemoveIfNotQualified removes user from chat when qualification is not allowed")
    void checkAndRemoveIfNotQualified_ShouldRemoveUser_WhenQualificationNotAllowed() {
        var member = new ChatMember("member", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, member, null));
        when(qualificationService.getBestQualification(456L)).thenReturn(QualificationLevel.NO);
        when(telegramAccessBotApi.unbanChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, true, null));

        moderationService.checkAndRemoveIfNotQualified(-1001968543887L, 123L, 456L);

        verify(telegramAccessBotApi).unbanChatMember(any(UnbanChatMemberRequest.class));
        verify(messageService).sendMessage(eq(123L), contains("удалены"));
        verify(membershipService).removeMembership(-1001968543887L, 123L, Platform.TELEGRAM);
    }

    @Test
    @DisplayName("checkAndRemoveIfNotQualified does not remove user when qualification is allowed")
    void checkAndRemoveIfNotQualified_ShouldNotRemoveUser_WhenQualificationAllowed() {
        var member = new ChatMember("member", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, member, null));
        when(qualificationService.getBestQualification(456L)).thenReturn(QualificationLevel.M);

        moderationService.checkAndRemoveIfNotQualified(-1001968543887L, 123L, 456L);

        verify(telegramAccessBotApi, never()).unbanChatMember(any());
        verify(messageService, never()).sendMessage(anyLong(), anyString());
    }
}
