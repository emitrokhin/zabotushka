package ru.mitrohinayulya.zabotushka.service;

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
    void testProcessUserAddedUpdate_DelegatesToJoinService() {
        var update = org.mockito.Mockito.mock(MaxUpdate.class);

        maxService.processUserAddedUpdate(update);

        verify(joinRequestService).processUserAddedUpdate(update);
    }

    @Test
    void testIsMemberOfChat_DelegatesToModerationService() {
        when(moderationService.isMemberOfChat(10L, 20L)).thenReturn(true);

        var result = maxService.isMemberOfChat(10L, 20L);

        assertThat(result).isTrue();
        verify(moderationService).isMemberOfChat(10L, 20L);
    }

    @Test
    void testRemoveMemberFromChat_DelegatesToModerationService() {
        maxService.removeMemberFromChat(10L, 20L);

        verify(moderationService).removeMemberFromChat(10L, 20L);
    }

    @Test
    void testCheckAndRemoveIfNotQualified_DelegatesToModerationService() {
        maxService.checkAndRemoveIfNotQualified(10L, 20L, 30L);

        verify(moderationService).checkAndRemoveIfNotQualified(10L, 20L, 30L);
    }
}
