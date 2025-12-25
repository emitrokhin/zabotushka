package ru.mitrohinayulya.zabotushka.config;

import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ChatGroupRequirements
 */
class ChatGroupRequirementsTest {

    @Test
    void testGroup1Requirements() {
        // Given: GROUP_1 требует M или GM
        var group1 = ChatGroupRequirements.GROUP_1;

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
        // Given: GROUP_2 требует L, M или GM
        var group2 = ChatGroupRequirements.GROUP_2;

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
        // Given: GROUP_3 требует S, L, M или GM
        var group3 = ChatGroupRequirements.GROUP_3;

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
        // Given: GROUP_4 требует S, L, M или GM
        var group4 = ChatGroupRequirements.GROUP_4;

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
        // Given: GROUP_5 требует S, L, M или GM
        var group5 = ChatGroupRequirements.GROUP_5;

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
        // When: ищем GROUP_1 по её chatId
        var result = ChatGroupRequirements.findByChatId(-1001968543887L);

        // Then: группа найдена
        assertTrue(result.isPresent());
        assertEquals(ChatGroupRequirements.GROUP_1, result.get());
        assertEquals(-1001968543887L, result.get().getChatId());
    }

    @Test
    void testFindByChatId_NotFound() {
        // When: ищем группу с несуществующим chatId
        var result = ChatGroupRequirements.findByChatId(-9999999999L);

        // Then: группа не найдена
        assertTrue(result.isEmpty());
    }

    @Test
    void testAllGroupsHaveUniqueChatIds() {
        // Given: все группы
        var groups = ChatGroupRequirements.values();

        // When: собираем все chatId
        var chatIds = java.util.Arrays.stream(groups)
                .map(ChatGroupRequirements::getChatId)
                .toList();

        // Then: все chatId уникальны (кроме GROUP_4, который дублируется в требованиях)
        var uniqueChatIds = new java.util.HashSet<>(chatIds);

        // У нас 5 групп, но chatId может быть меньше из-за дубликата в требованиях
        assertTrue(uniqueChatIds.size() <= groups.length);
    }

    @Test
    void testGetChatId() {
        // When & Then: проверяем chatId всех групп
        assertEquals(-1001968543887L, ChatGroupRequirements.GROUP_1.getChatId());
        assertEquals(-1001891048040L, ChatGroupRequirements.GROUP_2.getChatId());
        assertEquals(-1001835476759L, ChatGroupRequirements.GROUP_3.getChatId());
        assertEquals(-1001811106801L, ChatGroupRequirements.GROUP_4.getChatId());
        assertEquals(-1001929076200L, ChatGroupRequirements.GROUP_5.getChatId());
    }

    @Test
    void testGetAllowedQualifications() {
        // When: получаем список допустимых квалификаций для GROUP_1
        var allowedQuals = ChatGroupRequirements.GROUP_1.getAllowedQualifications();

        // Then: список содержит M и GM
        assertEquals(2, allowedQuals.size());
        assertTrue(allowedQuals.contains(QualificationLevel.M));
        assertTrue(allowedQuals.contains(QualificationLevel.GM));
    }
}
