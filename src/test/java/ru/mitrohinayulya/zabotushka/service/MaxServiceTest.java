package ru.mitrohinayulya.zabotushka.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.dto.max.MaxUpdate;
import ru.mitrohinayulya.zabotushka.service.max.MaxJoinRequestService;
import ru.mitrohinayulya.zabotushka.service.max.MaxGroupAccessService;
import ru.mitrohinayulya.zabotushka.service.max.MaxService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaxServiceTest {

    @Mock
    MaxJoinRequestService joinRequestService;

    @Mock
    MaxGroupAccessService moderationService;

    @InjectMocks
    MaxService maxService;

    @Test
    @DisplayName("processUserAddedUpdate delegates to join request service")
    void processUserAddedUpdate_ShouldDelegateToJoinService() {
        var update = org.mockito.Mockito.mock(MaxUpdate.class);

        maxService.processUserAddedUpdate(update);

        verify(joinRequestService).processUserAddedUpdate(update);
    }

    @Test
    @DisplayName("isMemberOfChat delegates to moderation service and returns its result")
    void isMemberOfChat_ShouldDelegateToModerationService() {
        when(moderationService.isMemberOfChat(10L, 20L)).thenReturn(true);

        var result = maxService.isMemberOfChat(10L, 20L);

        assertThat(result).as("Should return the result from moderation service").isTrue();
        verify(moderationService).isMemberOfChat(10L, 20L);
    }

    @Test
    @DisplayName("removeMemberFromChat delegates to moderation service")
    void removeMemberFromChat_ShouldDelegateToModerationService() {
        maxService.removeMemberFromChat(10L, 20L);

        verify(moderationService).removeMemberFromChat(10L, 20L);
    }

    @Test
    @DisplayName("checkAndRemoveIfNotQualified delegates to moderation service")
    void checkAndRemoveIfNotQualified_ShouldDelegateToModerationService() {
        maxService.checkAndRemoveIfNotQualified(10L, 20L, 30L);

        verify(moderationService).checkAndRemoveIfNotQualified(10L, 20L, 30L);
    }
}
