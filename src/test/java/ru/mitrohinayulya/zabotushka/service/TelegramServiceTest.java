package ru.mitrohinayulya.zabotushka.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.dto.telegram.ChatJoinRequest;
import ru.mitrohinayulya.zabotushka.service.telegram.TelegramJoinRequestService;
import ru.mitrohinayulya.zabotushka.service.telegram.TelegramGroupAccessService;
import ru.mitrohinayulya.zabotushka.service.telegram.TelegramService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    @Mock
    TelegramJoinRequestService joinRequestService;

    @Mock
    TelegramGroupAccessService moderationService;

    @InjectMocks
    TelegramService telegramService;

    @Test
    @DisplayName("processChatJoinRequest delegates to join request service")
    void processChatJoinRequest_ShouldDelegateToJoinService() {
        var request = org.mockito.Mockito.mock(ChatJoinRequest.class);

        telegramService.processChatJoinRequest(request);

        verify(joinRequestService).processChatJoinRequest(request);
    }

    @Test
    @DisplayName("isMemberOfChat delegates to moderation service and returns its result")
    void isMemberOfChat_ShouldDelegateToModerationService() {
        when(moderationService.isMemberOfChat(10L, 20L)).thenReturn(true);

        var result = telegramService.isMemberOfChat(10L, 20L);

        assertThat(result).as("Should return the result from moderation service").isTrue();
        verify(moderationService).isMemberOfChat(10L, 20L);
    }

    @Test
    @DisplayName("removeMemberFromChat delegates to moderation service")
    void removeMemberFromChat_ShouldDelegateToModerationService() {
        telegramService.removeMemberFromChat(10L, 20L);

        verify(moderationService).removeMemberFromChat(10L, 20L);
    }

    @Test
    @DisplayName("checkAndRemoveIfNotQualified delegates to moderation service")
    void checkAndRemoveIfNotQualified_ShouldDelegateToModerationService() {
        telegramService.checkAndRemoveIfNotQualified(10L, 20L, 30L);

        verify(moderationService).checkAndRemoveIfNotQualified(10L, 20L, 30L);
    }
}
