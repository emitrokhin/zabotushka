package ru.mitrohinayulya.zabotushka.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.entity.AuthorizedUser;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.service.AuthorizedUserService;
import ru.mitrohinayulya.zabotushka.service.TelegramService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для GroupQualificationScheduler
 */
@ExtendWith(MockitoExtension.class)
class GroupQualificationSchedulerTest {

    @Mock
    AuthorizedUserService authorizedUserService;

    @Mock
    TelegramService telegramService;

    @InjectMocks
    GroupQualificationScheduler scheduler;

    private MockedStatic<UserGroupMembership> membershipMock;

    @BeforeEach
    void setUp() {
        membershipMock = mockStatic(UserGroupMembership.class);
    }

    @AfterEach
    void tearDown() {
        if (membershipMock != null) {
            membershipMock.close();
        }
    }

    @Test
    void testCheckGroupQualifications_WithMembers() {
        // Given: есть члены в группах
        var chatId1 = -1001968543887L; // GROUP_1
        var chatId2 = -1001891048040L; // GROUP_2

        // Создаем членства для первой группы
        var membership1 = createMembership(12345L, chatId1);
        var membership2 = createMembership(54321L, chatId1);

        // Создаем членства для второй группы
        var membership3 = createMembership(11111L, chatId2);

        // Настраиваем мок для возврата членств по группам
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId1))
                .thenReturn(Arrays.asList(membership1, membership2));
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId2))
                .thenReturn(Collections.singletonList(membership3));
        membershipMock.when(() -> UserGroupMembership.findByChatId(-1001835476759L))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(-1001811106801L))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(-1001929076200L))
                .thenReturn(Collections.emptyList());

        // Настраиваем проверку существования (не были удалены)
        membershipMock.when(() -> UserGroupMembership.exists(anyLong(), anyLong()))
                .thenReturn(true);

        // Создаем соответствующих пользователей
        var user1 = createUser(12345L, 999888L);
        var user2 = createUser(54321L, 888999L);
        var user3 = createUser(11111L, 777666L);

        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(user1);
        when(authorizedUserService.findByTelegramId(54321L)).thenReturn(user2);
        when(authorizedUserService.findByTelegramId(11111L)).thenReturn(user3);

        // When: запускаем проверку
        scheduler.checkGroupQualifications();

        // Then: проверка выполнена для всех членов
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 12345L, 999888L);
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 54321L, 888999L);
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId2, 11111L, 777666L);

        // Проверяем, что lastCheckedAt обновился
        verify(membership1, times(1)).persist();
        verify(membership2, times(1)).persist();
        verify(membership3, times(1)).persist();
    }

    @Test
    void testCheckGroupQualifications_NoMembers() {
        // Given: нет членов ни в одной группе
        membershipMock.when(() -> UserGroupMembership.findByChatId(anyLong()))
                .thenReturn(Collections.emptyList());

        // When: запускаем проверку
        scheduler.checkGroupQualifications();

        // Then: проверка выполнена, но никакие методы не вызывались
        verify(authorizedUserService, never()).findByTelegramId(anyLong());
        verify(telegramService, never()).checkAndRemoveIfNotQualified(anyLong(), anyLong(), anyLong());
    }

    @Test
    void testCheckGroupQualifications_UserNotFound() {
        // Given: есть членство, но пользователь не найден в БД (orphaned membership)
        var chatId1 = -1001968543887L;
        var chatId2 = -1001891048040L;
        var chatId3 = -1001835476759L;
        var chatId4 = -1001811106801L;
        var chatId5 = -1001929076200L;
        var membership = createMembership(12345L, chatId1);

        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId1))
                .thenReturn(Collections.singletonList(membership));
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId2))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId3))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId4))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId5))
                .thenReturn(Collections.emptyList());

        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(null);

        // When: запускаем проверку
        scheduler.checkGroupQualifications();

        // Then: пользователь удаляется из группы и orphaned membership удаляется
        verify(telegramService, times(1)).removeMemberFromChat(chatId1, 12345L);
        verify(membership, times(1)).delete();
        // Квалификация не проверяется (нет user объекта)
        verify(telegramService, never()).checkAndRemoveIfNotQualified(anyLong(), anyLong(), anyLong());
        verify(membership, never()).persist();
    }

    @Test
    void testCheckGroupQualifications_UserRemoved() {
        // Given: пользователь не соответствует квалификации и будет удален
        var chatId1 = -1001968543887L;
        var chatId2 = -1001891048040L;
        var chatId3 = -1001835476759L;
        var chatId4 = -1001811106801L;
        var chatId5 = -1001929076200L;
        var membership = createMembership(12345L, chatId1);
        var user = createUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId1))
                .thenReturn(Collections.singletonList(membership));
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId2))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId3))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId4))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId5))
                .thenReturn(Collections.emptyList());

        // После удаления членство больше не существует
        membershipMock.when(() -> UserGroupMembership.exists(12345L, chatId1))
                .thenReturn(false);

        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(user);

        // When: запускаем проверку
        scheduler.checkGroupQualifications();

        // Then: квалификация проверена, но lastCheckedAt не обновляется (пользователь удален)
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 12345L, 999888L);
        verify(membership, never()).persist();
    }

    @Test
    void testCheckGroupQualifications_WithException() {
        // Given: при проверке возникает исключение
        var chatId1 = -1001968543887L;
        var chatId2 = -1001891048040L;
        var chatId3 = -1001835476759L;
        var chatId4 = -1001811106801L;
        var chatId5 = -1001929076200L;
        var membership = createMembership(12345L, chatId1);
        var user = createUser(12345L, 999888L);

        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId1))
                .thenReturn(Collections.singletonList(membership));
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId2))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId3))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId4))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.findByChatId(chatId5))
                .thenReturn(Collections.emptyList());
        membershipMock.when(() -> UserGroupMembership.exists(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        when(authorizedUserService.findByTelegramId(12345L)).thenReturn(user);

        // When: запускаем проверку (не должно выбрасывать исключение)
        scheduler.checkGroupQualifications();

        // Then: проверка обработала ошибку
        verify(telegramService, times(1)).checkAndRemoveIfNotQualified(chatId1, 12345L, 999888L);
    }

    // Helper methods

    private AuthorizedUser createUser(Long telegramId, Long greenwayId) {
        var user = new AuthorizedUser();
        user.id = UUID.randomUUID();
        user.telegramId = telegramId;
        user.greenwayId = greenwayId;
        user.regDate = "2023-01-15";
        user.creationDate = LocalDateTime.now();
        return user;
    }

    private UserGroupMembership createMembership(Long telegramId, Long chatId) {
        var membership = mock(UserGroupMembership.class);
        membership.telegramId = telegramId;
        membership.chatId = chatId;
        membership.joinedAt = LocalDateTime.now();
        return membership;
    }
}
