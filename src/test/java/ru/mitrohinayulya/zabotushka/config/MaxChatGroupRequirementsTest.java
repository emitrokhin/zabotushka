package ru.mitrohinayulya.zabotushka.config;

import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import static org.junit.jupiter.api.Assertions.*;

class MaxChatGroupRequirementsTest {

    @Test
    void testGoldClubRequirements() {
        var goldClub = MaxChatGroupRequirements.GOLD_CLUB;

        assertTrue(goldClub.isQualificationAllowed(QualificationLevel.M));
        assertTrue(goldClub.isQualificationAllowed(QualificationLevel.GM));

        assertFalse(goldClub.isQualificationAllowed(QualificationLevel.S));
        assertFalse(goldClub.isQualificationAllowed(QualificationLevel.L));
        assertFalse(goldClub.isQualificationAllowed(QualificationLevel.NO));
    }

    @Test
    void testFindByChatId_Found() {
        var result = MaxChatGroupRequirements.findByChatId(-71062621438079L);

        assertTrue(result.isPresent());
        assertEquals(MaxChatGroupRequirements.GOLD_CLUB, result.get());
    }

    @Test
    void testFindByChatId_NotFound() {
        var result = MaxChatGroupRequirements.findByChatId(-9999999999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testResolveGroupName_KnownChatId() {
        var name = MaxChatGroupRequirements.resolveGroupName(-71062621438079L);

        assertEquals("Золотой клуб", name);
    }

    @Test
    void testResolveGroupName_UnknownChatId() {
        var name = MaxChatGroupRequirements.resolveGroupName(-9999999999L);

        assertEquals("клуб", name);
    }

    @Test
    void testGetChatId() {
        assertEquals(-71062621438079L, MaxChatGroupRequirements.GOLD_CLUB.getChatId());
    }

    @Test
    void testGetAllowedQualifications() {
        var allowedQuals = MaxChatGroupRequirements.GOLD_CLUB.getAllowedQualifications();

        assertEquals(2, allowedQuals.size());
        assertTrue(allowedQuals.contains(QualificationLevel.M));
        assertTrue(allowedQuals.contains(QualificationLevel.GM));
    }
}
