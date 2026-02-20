package ru.mitrohinayulya.zabotushka.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramChatGroupRequirementsTest {

    @Test
    @DisplayName("GOLD_CLUB allows M and GM qualifications only")
    void isQualificationAllowed_ShouldAllowMAndGM_WhenGoldClub() {
        var goldClub = TelegramChatGroupRequirements.GOLD_CLUB;

        assertThat(goldClub.isQualificationAllowed(QualificationLevel.M)).as("M should be allowed in GOLD_CLUB").isTrue();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.GM)).as("GM should be allowed in GOLD_CLUB").isTrue();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.L)).as("L should not be allowed in GOLD_CLUB").isFalse();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.S)).as("S should not be allowed in GOLD_CLUB").isFalse();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.NO)).as("NO should not be allowed in GOLD_CLUB").isFalse();
    }

    @Test
    @DisplayName("SILVER_CLUB allows L, M and GM qualifications only")
    void isQualificationAllowed_ShouldAllowLMAndGM_WhenSilverClub() {
        var silverClub = TelegramChatGroupRequirements.SILVER_CLUB;

        assertThat(silverClub.isQualificationAllowed(QualificationLevel.L)).as("L should be allowed in SILVER_CLUB").isTrue();
        assertThat(silverClub.isQualificationAllowed(QualificationLevel.M)).as("M should be allowed in SILVER_CLUB").isTrue();
        assertThat(silverClub.isQualificationAllowed(QualificationLevel.GM)).as("GM should be allowed in SILVER_CLUB").isTrue();
        assertThat(silverClub.isQualificationAllowed(QualificationLevel.S)).as("S should not be allowed in SILVER_CLUB").isFalse();
        assertThat(silverClub.isQualificationAllowed(QualificationLevel.NO)).as("NO should not be allowed in SILVER_CLUB").isFalse();
    }

    @Test
    @DisplayName("BRONZE_CLUB allows S, L, M and GM qualifications only")
    void isQualificationAllowed_ShouldAllowSLMAndGM_WhenBronzeClub() {
        var bronzeClub = TelegramChatGroupRequirements.BRONZE_CLUB;

        assertThat(bronzeClub.isQualificationAllowed(QualificationLevel.S)).as("S should be allowed in BRONZE_CLUB").isTrue();
        assertThat(bronzeClub.isQualificationAllowed(QualificationLevel.L)).as("L should be allowed in BRONZE_CLUB").isTrue();
        assertThat(bronzeClub.isQualificationAllowed(QualificationLevel.M)).as("M should be allowed in BRONZE_CLUB").isTrue();
        assertThat(bronzeClub.isQualificationAllowed(QualificationLevel.GM)).as("GM should be allowed in BRONZE_CLUB").isTrue();
        assertThat(bronzeClub.isQualificationAllowed(QualificationLevel.NO)).as("NO should not be allowed in BRONZE_CLUB").isFalse();
    }

    @Test
    @DisplayName("CAN_AFFORD allows S, L, M and GM qualifications only")
    void isQualificationAllowed_ShouldAllowSLMAndGM_WhenCanAfford() {
        var canAfford = TelegramChatGroupRequirements.CAN_AFFORD;

        assertThat(canAfford.isQualificationAllowed(QualificationLevel.S)).as("S should be allowed in CAN_AFFORD").isTrue();
        assertThat(canAfford.isQualificationAllowed(QualificationLevel.L)).as("L should be allowed in CAN_AFFORD").isTrue();
        assertThat(canAfford.isQualificationAllowed(QualificationLevel.M)).as("M should be allowed in CAN_AFFORD").isTrue();
        assertThat(canAfford.isQualificationAllowed(QualificationLevel.GM)).as("GM should be allowed in CAN_AFFORD").isTrue();
        assertThat(canAfford.isQualificationAllowed(QualificationLevel.NO)).as("NO should not be allowed in CAN_AFFORD").isFalse();
    }

    @Test
    @DisplayName("CAN_AFFORD_CHAT allows S, L, M and GM qualifications only")
    void isQualificationAllowed_ShouldAllowSLMAndGM_WhenCanAffordChat() {
        var canAffordChat = TelegramChatGroupRequirements.CAN_AFFORD_CHAT;

        assertThat(canAffordChat.isQualificationAllowed(QualificationLevel.S)).as("S should be allowed in CAN_AFFORD_CHAT").isTrue();
        assertThat(canAffordChat.isQualificationAllowed(QualificationLevel.L)).as("L should be allowed in CAN_AFFORD_CHAT").isTrue();
        assertThat(canAffordChat.isQualificationAllowed(QualificationLevel.M)).as("M should be allowed in CAN_AFFORD_CHAT").isTrue();
        assertThat(canAffordChat.isQualificationAllowed(QualificationLevel.GM)).as("GM should be allowed in CAN_AFFORD_CHAT").isTrue();
        assertThat(canAffordChat.isQualificationAllowed(QualificationLevel.NO)).as("NO should not be allowed in CAN_AFFORD_CHAT").isFalse();
    }

    @Test
    @DisplayName("findByChatId returns the matching group when chat ID exists")
    void findByChatId_ShouldReturnGroup_WhenChatIdExists() {
        var result = TelegramChatGroupRequirements.findByChatId(-1001968543887L);

        assertThat(result)
                .as("Result should be present for known chat ID")
                .isPresent()
                .as("Should return GOLD_CLUB for the known chat ID")
                .contains(TelegramChatGroupRequirements.GOLD_CLUB);
        assertThat(result.get().getChatId()).as("Chat ID should match the requested one").isEqualTo(-1001968543887L);
    }

    @Test
    @DisplayName("findByChatId returns empty when chat ID does not exist")
    void findByChatId_ShouldReturnEmpty_WhenChatIdNotFound() {
        var result = TelegramChatGroupRequirements.findByChatId(-9999999999L);

        assertThat(result).as("Result should be empty for unknown chat ID").isEmpty();
    }

    @Test
    @DisplayName("All groups have a valid number of unique chat IDs")
    void getChatIds_ShouldNotExceedGroupCount() {
        var groups = TelegramChatGroupRequirements.values();
        var chatIds = java.util.Arrays.stream(groups)
                .map(TelegramChatGroupRequirements::getChatId)
                .toList();
        var uniqueChatIds = new java.util.HashSet<>(chatIds);

        assertThat(uniqueChatIds)
                .as("Number of unique chat IDs should not exceed group count")
                .hasSizeLessThanOrEqualTo(groups.length);
    }

    @Test
    @DisplayName("getChatId returns correct chat IDs for all groups")
    void getChatId_ShouldReturnCorrectValues_ForAllGroups() {
        assertThat(TelegramChatGroupRequirements.GOLD_CLUB.getChatId()).as("GOLD_CLUB chat ID").isEqualTo(-1001968543887L);
        assertThat(TelegramChatGroupRequirements.SILVER_CLUB.getChatId()).as("SILVER_CLUB chat ID").isEqualTo(-1001891048040L);
        assertThat(TelegramChatGroupRequirements.BRONZE_CLUB.getChatId()).as("BRONZE_CLUB chat ID").isEqualTo(-1001835476759L);
        assertThat(TelegramChatGroupRequirements.CAN_AFFORD.getChatId()).as("CAN_AFFORD chat ID").isEqualTo(-1001811106801L);
        assertThat(TelegramChatGroupRequirements.CAN_AFFORD_CHAT.getChatId()).as("CAN_AFFORD_CHAT chat ID").isEqualTo(-1001929076200L);
    }

    @Test
    @DisplayName("getAllowedQualifications returns M and GM for GOLD_CLUB")
    void getAllowedQualifications_ShouldReturnMAndGM_WhenGoldClub() {
        var allowedQuals = TelegramChatGroupRequirements.GOLD_CLUB.getAllowedQualifications();

        assertThat(allowedQuals).as("GOLD_CLUB should have exactly 2 allowed qualifications")
                .hasSize(2)
                .as("GOLD_CLUB allowed qualifications should contain M")
                .contains(QualificationLevel.M)
                .as("GOLD_CLUB allowed qualifications should contain GM")
                .contains(QualificationLevel.GM);
    }
}
