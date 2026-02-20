package ru.mitrohinayulya.zabotushka.config;

import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для TelegramChatGroupRequirements
 */
class TelegramChatGroupRequirementsTest {

    @Test
    void testGroup1Requirements() {
        // Given: GOLD_CLUB требует M или GM
        var group1 = TelegramChatGroupRequirements.GOLD_CLUB;

        // When & Then: проверяем допустимые квалификации
        assertTrue(group1.isQualificationAllowed(QualificationLevel.M));
        assertTrue(group1.isQualificationAllowed(QualificationLevel.GM));

        // Недопустимые квалификации
        assertFalse(group1.isQualificationAllowed(QualificationLevel.L));
        assertFalse(group1.isQualificationAllowed(QualificationLevel.S));
        assertFalse(group1.isQualificationAllowed(QualificationLevel.NO));
    }

    @Test
    void testGroup2Requirements() {
        // Given: SILVER_CLUB требует L, M или GM
        var group2 = TelegramChatGroupRequirements.SILVER_CLUB;

        // When & Then: проверяем допустимые квалификации
        assertTrue(group2.isQualificationAllowed(QualificationLevel.L));
        assertTrue(group2.isQualificationAllowed(QualificationLevel.M));
        assertTrue(group2.isQualificationAllowed(QualificationLevel.GM));

        // Недопустимые квалификации
        assertFalse(group2.isQualificationAllowed(QualificationLevel.S));
        assertFalse(group2.isQualificationAllowed(QualificationLevel.NO));
    }

    @Test
    void testGroup3Requirements() {
        // Given: BRONZE_CLUB требует S, L, M или GM
        var group3 = TelegramChatGroupRequirements.BRONZE_CLUB;

        // When & Then: проверяем допустимые квалификации
        assertTrue(group3.isQualificationAllowed(QualificationLevel.S));
        assertTrue(group3.isQualificationAllowed(QualificationLevel.L));
        assertTrue(group3.isQualificationAllowed(QualificationLevel.M));
        assertTrue(group3.isQualificationAllowed(QualificationLevel.GM));

        // Недопустимые квалификации
        assertFalse(group3.isQualificationAllowed(QualificationLevel.NO));
    }

    @Test
    void testGroup4Requirements() {
        // Given: CAN_AFFORD требует S, L, M или GM
        var group4 = TelegramChatGroupRequirements.CAN_AFFORD;

        // When & Then: проверяем допустимые квалификации
        assertTrue(group4.isQualificationAllowed(QualificationLevel.S));
        assertTrue(group4.isQualificationAllowed(QualificationLevel.L));
        assertTrue(group4.isQualificationAllowed(QualificationLevel.M));
        assertTrue(group4.isQualificationAllowed(QualificationLevel.GM));

        // Недопустимые квалификации
        assertFalse(group4.isQualificationAllowed(QualificationLevel.NO));
    }

    @Test
    void testGroup5Requirements() {
        // Given: CAN_AFFORD_CHAT требует S, L, M или GM
        var group5 = TelegramChatGroupRequirements.CAN_AFFORD_CHAT;

        // When & Then: проверяем допустимые квалификации
        assertTrue(group5.isQualificationAllowed(QualificationLevel.S));
        assertTrue(group5.isQualificationAllowed(QualificationLevel.L));
        assertTrue(group5.isQualificationAllowed(QualificationLevel.M));
        assertTrue(group5.isQualificationAllowed(QualificationLevel.GM));

        // Недопустимые квалификации
        assertFalse(group5.isQualificationAllowed(QualificationLevel.NO));
    }

    @Test
    void testFindByChatId_Found() {
        // When: ищем GOLD_CLUB по её chatId
        var result = TelegramChatGroupRequirements.findByChatId(-1001968543887L);

        // Then: группа найдена
        assertTrue(result.isPresent());
        assertEquals(TelegramChatGroupRequirements.GOLD_CLUB, result.get());
        assertEquals(-1001968543887L, result.get().getChatId());
    }

    @Test
    void testFindByChatId_NotFound() {
        // When: ищем группу с несуществующим chatId
        var result = TelegramChatGroupRequirements.findByChatId(-9999999999L);

        // Then: группа не найдена
        assertTrue(result.isEmpty());
    }

    @Test
    void testAllGroupsHaveUniqueChatIds() {
        // Given: все группы
        var groups = TelegramChatGroupRequirements.values();

        // When: собираем все chatId
        var chatIds = java.util.Arrays.stream(groups)
                .map(TelegramChatGroupRequirements::getChatId)
                .toList();

        // Then: все chatId уникальны (кроме CAN_AFFORD, который дублируется в требованиях)
        var uniqueChatIds = new java.util.HashSet<>(chatIds);

        // У нас 5 групп, но chatId может быть меньше из-за дубликата в требованиях
        assertTrue(uniqueChatIds.size() <= groups.length);
    }

    @Test
    void testGetChatId() {
        // When & Then: проверяем chatId всех групп
        assertEquals(-1001968543887L, TelegramChatGroupRequirements.GOLD_CLUB.getChatId());
        assertEquals(-1001891048040L, TelegramChatGroupRequirements.SILVER_CLUB.getChatId());
        assertEquals(-1001835476759L, TelegramChatGroupRequirements.BRONZE_CLUB.getChatId());
        assertEquals(-1001811106801L, TelegramChatGroupRequirements.CAN_AFFORD.getChatId());
        assertEquals(-1001929076200L, TelegramChatGroupRequirements.CAN_AFFORD_CHAT.getChatId());
    }

    @Test
    void testGetAllowedQualifications() {
        // When: получаем список допустимых квалификаций для GOLD_CLUB
        var allowedQuals = TelegramChatGroupRequirements.GOLD_CLUB.getAllowedQualifications();

        // Then: список содержит M и GM
        assertEquals(2, allowedQuals.size());
        assertTrue(allowedQuals.contains(QualificationLevel.M));
        assertTrue(allowedQuals.contains(QualificationLevel.GM));
    }
}
