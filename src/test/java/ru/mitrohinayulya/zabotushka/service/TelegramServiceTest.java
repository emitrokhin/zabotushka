package ru.mitrohinayulya.zabotushka.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.TelegramApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.dto.telegram.*;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedUser;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Тесты для TelegramService
 */
@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    @Mock
    TelegramApi telegramApi;

    @Mock
    GreenwayService greenwayService;

    @Mock
    AuthorizedUserService authorizedUserService;

    @InjectMocks
    TelegramService telegramService;

    private ChatJoinRequest chatJoinRequest;
    private AuthorizedUser authorizedUser;

    @BeforeEach
    void setUp() {
        // Создаем типичный запрос на вступление в GROUP_1 (требует M или GM)
        var user = new User(12345L, false, "Иван", "Иванов", "ivan_test", "ru");
        var chat = new Chat(-1001968543887L, "supergroup", "Test Group", null, null, null);
        chatJoinRequest = new ChatJoinRequest(chat, user, 12345L, 1234567890L, "Test bio", null);

        // Создаем авторизованного пользователя
        authorizedUser = new AuthorizedUser();
        authorizedUser.telegramId = 12345L;
        authorizedUser.greenwayId = 999888L;
        authorizedUser.regDate = "2023-01-15";
    }

    @Test
    void testProcessChatJoinRequest_Approved_WithGMQualification() {
        // Given: пользователь авторизован и имеет квалификацию GM
        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(authorizedUser);

        var currentPartner = createPartner("GM4");
        var previousPartner = createPartner("M1");
        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(999888L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(999888L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, 999888L))
                .thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, 999888L))
                .thenReturn(Optional.of(previousPartner));

        var approveResponse = new TelegramResponse<Boolean>(true, null, null);
        when(telegramApi.approveChatJoinRequest(any())).thenReturn(approveResponse);

        when(telegramApi.sendMessage(any())).thenReturn(new TelegramResponse<>(true, null, null));

        // When: обрабатываем запрос
        telegramService.processChatJoinRequest(chatJoinRequest);

        // Then: запрос одобрен
        verify(telegramApi, times(1)).approveChatJoinRequest(any(ApproveChatJoinRequest.class));
        verify(telegramApi, never()).declineChatJoinRequest(any());
        verify(telegramApi, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void testProcessChatJoinRequest_Declined_WithLowQualification() {
        // Given: пользователь авторизован, но имеет квалификацию L (недостаточно для GROUP_1)
        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(authorizedUser);

        var currentPartner = createPartner("L2");
        var previousPartner = createPartner("S1");
        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(999888L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(999888L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, 999888L))
                .thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, 999888L))
                .thenReturn(Optional.of(previousPartner));

        var declineResponse = new TelegramResponse<Boolean>(true, null, null);
        when(telegramApi.declineChatJoinRequest(any())).thenReturn(declineResponse);

        when(telegramApi.sendMessage(any())).thenReturn(new TelegramResponse<>(true, null, null));

        // When: обрабатываем запрос
        telegramService.processChatJoinRequest(chatJoinRequest);

        // Then: запрос отклонен
        verify(telegramApi, never()).approveChatJoinRequest(any());
        verify(telegramApi, times(1)).declineChatJoinRequest(any(DeclineChatJoinRequest.class));
        verify(telegramApi, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void testProcessChatJoinRequest_Declined_UserNotAuthorized() {
        // Given: пользователь НЕ авторизован (нет в БД)
        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(null);

        var declineResponse = new TelegramResponse<Boolean>(true, null, null);
        when(telegramApi.declineChatJoinRequest(any())).thenReturn(declineResponse);

        when(telegramApi.sendMessage(any())).thenReturn(new TelegramResponse<>(true, null, null));

        // When: обрабатываем запрос
        telegramService.processChatJoinRequest(chatJoinRequest);

        // Then: запрос отклонен, к Greenway API не обращались
        verify(telegramApi, never()).approveChatJoinRequest(any());
        verify(telegramApi, times(1)).declineChatJoinRequest(any(DeclineChatJoinRequest.class));
        verify(greenwayService, never()).getPartnerList(anyLong(), anyInt());
    }

    @Test
    void testProcessChatJoinRequest_Declined_UnknownGroup() {
        // Given: группа не найдена в требованиях
        var unknownChat = new Chat(-9999999999L, "supergroup", "Unknown Group", null, null, null);
        var unknownChatRequest = new ChatJoinRequest(unknownChat, chatJoinRequest.from(),
                12345L, 1234567890L, "Test bio", null);

        var declineResponse = new TelegramResponse<Boolean>(true, null, null);
        when(telegramApi.declineChatJoinRequest(any())).thenReturn(declineResponse);

        when(telegramApi.sendMessage(any())).thenReturn(new TelegramResponse<>(true, null, null));

        // When: обрабатываем запрос
        telegramService.processChatJoinRequest(unknownChatRequest);

        // Then: запрос отклонен, к Greenway API не обращались
        verify(telegramApi, never()).approveChatJoinRequest(any());
        verify(telegramApi, times(1)).declineChatJoinRequest(any(DeclineChatJoinRequest.class));
        verify(greenwayService, never()).getPartnerList(anyLong(), anyInt());
    }

    @Test
    void testProcessChatJoinRequest_Approved_PreviousQualificationBetter() {
        // Given: пользователь авторизован, текущая квалификация L, предыдущая M (подходит для GROUP_1)
        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(authorizedUser);

        var currentPartner = createPartner("L2");
        var previousPartner = createPartner("M3");
        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(999888L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(999888L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, 999888L))
                .thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, 999888L))
                .thenReturn(Optional.of(previousPartner));

        var approveResponse = new TelegramResponse<Boolean>(true, null, null);
        when(telegramApi.approveChatJoinRequest(any())).thenReturn(approveResponse);

        when(telegramApi.sendMessage(any())).thenReturn(new TelegramResponse<>(true, null, null));

        // When: обрабатываем запрос
        telegramService.processChatJoinRequest(chatJoinRequest);

        // Then: запрос одобрен (берется лучшая квалификация M из предыдущего периода)
        verify(telegramApi, times(1)).approveChatJoinRequest(any(ApproveChatJoinRequest.class));
        verify(telegramApi, never()).declineChatJoinRequest(any());
    }

    @Test
    void testProcessChatJoinRequest_Declined_NoQualification() {
        // Given: пользователь авторизован, но квалификация NO
        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(authorizedUser);

        var currentPartner = createPartner("NO");
        var previousPartner = createPartner("NO");
        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(999888L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(999888L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(any(), eq(999888L)))
                .thenReturn(Optional.of(currentPartner), Optional.of(previousPartner));

        var declineResponse = new TelegramResponse<Boolean>(true, null, null);
        when(telegramApi.declineChatJoinRequest(any())).thenReturn(declineResponse);

        when(telegramApi.sendMessage(any())).thenReturn(new TelegramResponse<>(true, null, null));

        // When: обрабатываем запрос
        telegramService.processChatJoinRequest(chatJoinRequest);

        // Then: запрос отклонен
        verify(telegramApi, never()).approveChatJoinRequest(any());
        verify(telegramApi, times(1)).declineChatJoinRequest(any(DeclineChatJoinRequest.class));
    }

    @Test
    void testProcessChatJoinRequest_Group2_Approved_WithLQualification() {
        // Given: пользователь авторизован с квалификацией L для GROUP_2 (требует L, M или GM)
        var group2Chat = new Chat(-1001891048040L, "supergroup", "Group 2", null, null, null);
        var group2Request = new ChatJoinRequest(group2Chat, chatJoinRequest.from(),
                12345L, 1234567890L, "Test bio", null);

        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(authorizedUser);

        var currentPartner = createPartner("L3");
        var previousPartner = createPartner("S1");
        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(999888L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(999888L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, 999888L))
                .thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, 999888L))
                .thenReturn(Optional.of(previousPartner));

        var approveResponse = new TelegramResponse<Boolean>(true, null, null);
        when(telegramApi.approveChatJoinRequest(any())).thenReturn(approveResponse);

        when(telegramApi.sendMessage(any())).thenReturn(new TelegramResponse<>(true, null, null));

        // When: обрабатываем запрос
        telegramService.processChatJoinRequest(group2Request);

        // Then: запрос одобрен (L достаточно для GROUP_2)
        verify(telegramApi, times(1)).approveChatJoinRequest(any(ApproveChatJoinRequest.class));
        verify(telegramApi, never()).declineChatJoinRequest(any());
    }

    @Test
    void testIsMemberOfChat_UserIsMember() {
        // Given: пользователь является участником группы
        var chatMember = new ChatMember("member", new User(12345L, false, "Test", null, null, null));
        var response = new TelegramResponse<>(true, chatMember, null);
        when(telegramApi.getChatMember(any(GetChatMemberRequest.class))).thenReturn(response);

        // When: проверяем членство
        var result = telegramService.isMemberOfChat(-1001968543887L, 12345L);

        // Then: возвращается true
        assertThat(result).isTrue();
        verify(telegramApi, times(1)).getChatMember(any(GetChatMemberRequest.class));
    }

    @Test
    void testIsMemberOfChat_UserIsNotMember() {
        // Given: пользователь не является участником группы
        var chatMember = new ChatMember("left", new User(12345L, false, "Test", null, null, null));
        var response = new TelegramResponse<>(true, chatMember, null);
        when(telegramApi.getChatMember(any(GetChatMemberRequest.class))).thenReturn(response);

        // When: проверяем членство
        var result = telegramService.isMemberOfChat(-1001968543887L, 12345L);

        // Then: возвращается false
        assertThat(result).isFalse();
    }

    @Test
    void testIsMemberOfChat_ApiError() {
        // Given: ошибка при запросе к API
        when(telegramApi.getChatMember(any(GetChatMemberRequest.class)))
                .thenThrow(new RuntimeException("API error"));

        // When: проверяем членство
        var result = telegramService.isMemberOfChat(-1001968543887L, 12345L);

        // Then: возвращается false
        assertThat(result).isFalse();
    }

    @Test
    void testRemoveMemberFromChat_Success() {
        // Given: успешное удаление пользователя
        var banResponse = new TelegramResponse<Boolean>(true, true, null);
        var unbanResponse = new TelegramResponse<Boolean>(true, true, null);
        var messageResponse = new TelegramResponse<>(true, null, null);

        when(telegramApi.banChatMember(any(BanChatMemberRequest.class))).thenReturn(banResponse);
        when(telegramApi.unbanChatMember(any(UnbanChatMemberRequest.class))).thenReturn(unbanResponse);
        when(telegramApi.sendMessage(any(SendMessageRequest.class))).thenReturn(messageResponse);

        // When: удаляем пользователя
        telegramService.removeMemberFromChat(-1001968543887L, 12345L);

        // Then: вызваны все методы
        verify(telegramApi, times(1)).banChatMember(any(BanChatMemberRequest.class));
        verify(telegramApi, times(1)).unbanChatMember(any(UnbanChatMemberRequest.class));
        verify(telegramApi, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void testRemoveMemberFromChat_BanFailed() {
        // Given: неудачный бан пользователя
        var banResponse = new TelegramResponse<Boolean>(false, null, "Error");

        when(telegramApi.banChatMember(any(BanChatMemberRequest.class))).thenReturn(banResponse);

        // When: удаляем пользователя
        telegramService.removeMemberFromChat(-1001968543887L, 12345L);

        // Then: unban и сообщение не вызваны
        verify(telegramApi, times(1)).banChatMember(any(BanChatMemberRequest.class));
        verify(telegramApi, never()).unbanChatMember(any());
        verify(telegramApi, never()).sendMessage(any());
    }

    @Test
    void testCheckAndRemoveIfNotQualified_UserNotMember() {
        // Given: пользователь не является участником группы
        var chatMember = new ChatMember("left", new User(12345L, false, "Test", null, null, null));
        var response = new TelegramResponse<>(true, chatMember, null);
        when(telegramApi.getChatMember(any(GetChatMemberRequest.class))).thenReturn(response);

        // When: проверяем и удаляем если не квалифицирован
        telegramService.checkAndRemoveIfNotQualified(-1001968543887L, 12345L, 999888L);

        // Then: не обращаемся к Greenway API и не удаляем
        verify(greenwayService, never()).getPartnerList(anyLong(), anyInt());
        verify(telegramApi, never()).banChatMember(any());
    }

    @Test
    void testCheckAndRemoveIfNotQualified_QualificationNotMet_UserRemoved() {
        // Given: пользователь является участником, но квалификация недостаточна
        var chatMember = new ChatMember("member", new User(12345L, false, "Test", null, null, null));
        var getMemberResponse = new TelegramResponse<>(true, chatMember, null);
        when(telegramApi.getChatMember(any(GetChatMemberRequest.class))).thenReturn(getMemberResponse);

        // Квалификация L (недостаточно для GROUP_1, требует M или GM)
        var currentPartner = createPartner("L2");
        var previousPartner = createPartner("S1");
        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(999888L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(999888L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, 999888L))
                .thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, 999888L))
                .thenReturn(Optional.of(previousPartner));

        var banResponse = new TelegramResponse<Boolean>(true, true, null);
        var unbanResponse = new TelegramResponse<Boolean>(true, true, null);
        var messageResponse = new TelegramResponse<>(true, null, null);

        when(telegramApi.banChatMember(any(BanChatMemberRequest.class))).thenReturn(banResponse);
        when(telegramApi.unbanChatMember(any(UnbanChatMemberRequest.class))).thenReturn(unbanResponse);
        when(telegramApi.sendMessage(any(SendMessageRequest.class))).thenReturn(messageResponse);

        // When: проверяем и удаляем
        telegramService.checkAndRemoveIfNotQualified(-1001968543887L, 12345L, 999888L);

        // Then: пользователь удален
        verify(telegramApi, times(1)).banChatMember(any(BanChatMemberRequest.class));
        verify(telegramApi, times(1)).unbanChatMember(any(UnbanChatMemberRequest.class));
        verify(telegramApi, atLeastOnce()).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void testCheckAndRemoveIfNotQualified_QualificationMet_UserNotRemoved() {
        // Given: пользователь является участником и квалификация достаточна
        var chatMember = new ChatMember("member", new User(12345L, false, "Test", null, null, null));
        var getMemberResponse = new TelegramResponse<>(true, chatMember, null);
        when(telegramApi.getChatMember(any(GetChatMemberRequest.class))).thenReturn(getMemberResponse);

        // Квалификация M (достаточно для GROUP_1)
        var currentPartner = createPartner("M2");
        var previousPartner = createPartner("L1");
        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(75);
        when(greenwayService.getPartnerList(999888L, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(999888L, 75)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, 999888L))
                .thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, 999888L))
                .thenReturn(Optional.of(previousPartner));

        // When: проверяем
        telegramService.checkAndRemoveIfNotQualified(-1001968543887L, 12345L, 999888L);

        // Then: пользователь НЕ удален
        verify(telegramApi, never()).banChatMember(any());
        verify(telegramApi, never()).unbanChatMember(any());
    }

    @Test
    void testCheckAndRemoveIfNotQualified_UnknownGroup() {
        // Given: группа не найдена в требованиях
        var chatMember = new ChatMember("member", new User(12345L, false, "Test", null, null, null));
        var getMemberResponse = new TelegramResponse<>(true, chatMember, null);
        when(telegramApi.getChatMember(any(GetChatMemberRequest.class))).thenReturn(getMemberResponse);

        // When: проверяем для неизвестной группы
        telegramService.checkAndRemoveIfNotQualified(-9999999999L, 12345L, 999888L);

        // Then: не обращаемся к Greenway API и не удаляем
        verify(greenwayService, never()).getPartnerList(anyLong(), anyInt());
        verify(telegramApi, never()).banChatMember(any());
    }

    // Helper methods

    private Partner createPartner(String qualification) {
        return new Partner(
                999888,
                "Иванов",
                "Иван",
                "Иванович",
                "1990-01-01",
                "test@example.com",
                "+79001234567",
                999888,
                "ACTIVE",
                "2023-01-15",
                "Россия",
                1,
                "Москва",
                null,
                null,
                null,
                null,
                0.0,
                0.0,
                0.0,
                qualification,
                1,
                false,
                "Петров Петр"
        );
    }
}
