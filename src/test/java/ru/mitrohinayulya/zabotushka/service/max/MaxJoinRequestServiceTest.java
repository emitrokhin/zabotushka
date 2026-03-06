package ru.mitrohinayulya.zabotushka.service.max;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationResult;
import ru.mitrohinayulya.zabotushka.dto.max.MaxUpdate;
import ru.mitrohinayulya.zabotushka.dto.max.MaxUser;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedMaxUser;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformGroupMembershipService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaxJoinRequestServiceTest {

    @Mock
    AuthorizedMaxUserService authorizedUserService;

    @Mock
    GreenwayQualificationService qualificationService;

    @Mock
    PlatformGroupMembershipService membershipService;

    @Mock
    MaxGroupAccessService moderationService;

    @InjectMocks
    MaxJoinRequestService joinRequestService;

    @Test
    @DisplayName("processUserAddedUpdate saves membership when user is qualified")
    void processUserAddedUpdate_ShouldSaveMembership_WhenUserIsQualified() {
        var update = new MaxUpdate("user_added", 1L, -71062621438079L,
                new MaxUser(555L, "Max", "User", "max_user", false, 0L),
                0L, false);

        var authorizedUser = new AuthorizedMaxUser();
        authorizedUser.maxId = 555L;
        authorizedUser.greenwayId = 999888L;

        when(authorizedUserService.findByMaxId(555L)).thenReturn(authorizedUser);
        when(qualificationService.getBestQualificationResult(999888L))
                .thenReturn(new QualificationResult(QualificationLevel.M, "M1"));

        joinRequestService.processUserAddedUpdate(update);

        verify(membershipService).saveMembership(-71062621438079L, 555L, Platform.MAX);
        verify(moderationService, never()).removeMemberFromChat(anyLong(), anyLong());
    }

    @Test
    @DisplayName("processUserAddedUpdate removes user from chat when user is not authorized")
    void processUserAddedUpdate_ShouldRemoveUser_WhenUserIsNotAuthorized() {
        var update = new MaxUpdate("user_added", 1L, -71062621438079L,
                new MaxUser(555L, "Max", "User", "max_user", false, 0L),
                0L, false);

        when(authorizedUserService.findByMaxId(555L)).thenReturn(null);

        joinRequestService.processUserAddedUpdate(update);

        verify(moderationService).removeMemberFromChat(-71062621438079L, 555L);
        verify(membershipService, never()).saveMembership(anyLong(), anyLong(), any());
    }
}
