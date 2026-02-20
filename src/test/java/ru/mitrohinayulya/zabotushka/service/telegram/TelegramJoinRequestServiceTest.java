package ru.mitrohinayulya.zabotushka.service.telegram;

import org.junit.jupiter.api.BeforeEach;
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
import ru.mitrohinayulya.zabotushka.entity.AuthorizedTelegramUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramJoinRequestServiceTest {

    @Mock
    @RestClient
    TelegramAccessBotApi telegramAccessBotApi;

    @Mock
    AuthorizedTelegramUserService authorizedUserService;

    @Mock
    GreenwayQualificationService qualificationService;

    @Mock
    PlatformGroupMembershipService membershipService;

    @Mock
    TelegramMessageService messageService;

    @InjectMocks
    TelegramJoinRequestService joinRequestService;

    private ChatJoinRequest chatJoinRequest;

    @BeforeEach
    void setUp() {
        var user = new User(12345L, false, "Иван", "Иванов", "ivan_test", "ru");
        var chat = new Chat(-1001968543887L, "supergroup", "Test Group", null, null, null);
        chatJoinRequest = new ChatJoinRequest(chat, user, 12345L, 1234567890L, "Test bio", null);
    }

    @Test
    @DisplayName("processChatJoinRequest approves request when user qualification matches")
    void processChatJoinRequest_ShouldApproveRequest_WhenQualificationMatches() {
        var authorizedUser = new AuthorizedTelegramUser();
        authorizedUser.telegramId = 12345L;
        authorizedUser.greenwayId = 999888L;

        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(authorizedUser);
        when(qualificationService.getBestQualification(999888L)).thenReturn(QualificationLevel.M);
        when(telegramAccessBotApi.approveChatJoinRequest(any()))
                .thenReturn(new TelegramResponse<>(true, true, null));

        joinRequestService.processChatJoinRequest(chatJoinRequest);

        verify(telegramAccessBotApi).approveChatJoinRequest(any(ApproveChatJoinRequest.class));
        verify(telegramAccessBotApi, never()).declineChatJoinRequest(any());
        verify(messageService).sendMessage(eq(12345L), contains("одобрен"));
        verify(membershipService).saveMembership(-1001968543887L, 12345L, Platform.TELEGRAM);
    }

    @Test
    @DisplayName("processChatJoinRequest declines request when user is not authorized")
    void processChatJoinRequest_ShouldDeclineRequest_WhenUserIsNotAuthorized() {
        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(null);
        when(telegramAccessBotApi.declineChatJoinRequest(any()))
                .thenReturn(new TelegramResponse<>(true, true, null));

        joinRequestService.processChatJoinRequest(chatJoinRequest);

        verify(telegramAccessBotApi).declineChatJoinRequest(any(DeclineChatJoinRequest.class));
        verify(telegramAccessBotApi, never()).approveChatJoinRequest(any());
        verify(qualificationService, never()).getBestQualification(anyLong());
        verify(membershipService, never()).saveMembership(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("processChatJoinRequest declines request when group is unknown")
    void processChatJoinRequest_ShouldDeclineRequest_WhenGroupIsUnknown() {
        var unknownChat = new Chat(-9999999999L, "supergroup", "Unknown Group", null, null, null);
        var unknownRequest = new ChatJoinRequest(unknownChat, chatJoinRequest.from(),
                12345L, 1234567890L, "Test bio", null);

        when(telegramAccessBotApi.declineChatJoinRequest(any()))
                .thenReturn(new TelegramResponse<>(true, true, null));

        joinRequestService.processChatJoinRequest(unknownRequest);

        verify(telegramAccessBotApi).declineChatJoinRequest(any(DeclineChatJoinRequest.class));
        verify(authorizedUserService, never()).findByTelegramId(anyLong());
    }

    @Test
    @DisplayName("processChatJoinRequest declines request when qualification does not match")
    void processChatJoinRequest_ShouldDeclineRequest_WhenQualificationDoesNotMatch() {
        var authorizedUser = new AuthorizedTelegramUser();
        authorizedUser.telegramId = 12345L;
        authorizedUser.greenwayId = 999888L;

        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(authorizedUser);
        when(qualificationService.getBestQualification(999888L)).thenReturn(QualificationLevel.NO);
        when(telegramAccessBotApi.declineChatJoinRequest(any()))
                .thenReturn(new TelegramResponse<>(true, true, null));

        joinRequestService.processChatJoinRequest(chatJoinRequest);

        verify(telegramAccessBotApi).declineChatJoinRequest(any(DeclineChatJoinRequest.class));
        verify(telegramAccessBotApi, never()).approveChatJoinRequest(any());
        verify(membershipService, never()).saveMembership(anyLong(), anyLong(), any());
    }
}
