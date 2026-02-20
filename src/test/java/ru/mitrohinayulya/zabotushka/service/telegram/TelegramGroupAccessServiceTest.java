package ru.mitrohinayulya.zabotushka.service.telegram;

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
    void testIsMemberOfChat_ReturnsTrueWhenUserIsMember() {
        var member = new ChatMember("member", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, member, null));

        var result = moderationService.isMemberOfChat(-1001968543887L, 123L);

        assertThat(result).isTrue();
    }

    @Test
    void testCheckAndRemoveIfNotQualified_RemovesOnlyMembershipWhenUserLeft() {
        var leftMember = new ChatMember("left", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, leftMember, null));

        moderationService.checkAndRemoveIfNotQualified(-1001968543887L, 123L, 456L);

        verify(membershipService).removeMembership(-1001968543887L, 123L, Platform.TELEGRAM);
        verify(qualificationService, never()).getBestQualification(anyLong());
        verify(telegramAccessBotApi, never()).unbanChatMember(any());
    }

    @Test
    void testCheckAndRemoveIfNotQualified_RemovesUserWhenQualificationNotAllowed() {
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
    void testCheckAndRemoveIfNotQualified_DoesNotRemoveWhenQualificationAllowed() {
        var member = new ChatMember("member", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, member, null));
        when(qualificationService.getBestQualification(456L)).thenReturn(QualificationLevel.M);

        moderationService.checkAndRemoveIfNotQualified(-1001968543887L, 123L, 456L);

        verify(telegramAccessBotApi, never()).unbanChatMember(any());
        verify(messageService, never()).sendMessage(anyLong(), anyString());
    }

    @Test
    void testIsMemberOfChat_ReturnsFalseWhenUserLeft() {
        var leftMember = new ChatMember("left", new User(123L, false, "Test", null, null, null));
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(true, leftMember, null));

        var result = moderationService.isMemberOfChat(-1001968543887L, 123L);

        assertThat(result).isFalse();
    }

    @Test
    void testIsMemberOfChat_ReturnsFalseWhenResponseNotOk() {
        when(telegramAccessBotApi.getChatMember(any()))
                .thenReturn(new TelegramResponse<>(false, null, "Not Found"));

        var result = moderationService.isMemberOfChat(-1001968543887L, 123L);

        assertThat(result).isFalse();
    }
}
