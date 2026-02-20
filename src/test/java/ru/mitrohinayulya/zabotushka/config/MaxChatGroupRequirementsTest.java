package ru.mitrohinayulya.zabotushka.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import static org.assertj.core.api.Assertions.assertThat;

class MaxChatGroupRequirementsTest {

    @Test
    @DisplayName("GOLD_CLUB allows M and GM qualifications only")
    void isQualificationAllowed_ShouldAllowMAndGM_WhenGoldClub() {
        var goldClub = MaxChatGroupRequirements.GOLD_CLUB;

        assertThat(goldClub.isQualificationAllowed(QualificationLevel.M)).as("M should be allowed in GOLD_CLUB").isTrue();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.GM)).as("GM should be allowed in GOLD_CLUB").isTrue();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.S)).as("S should not be allowed in GOLD_CLUB").isFalse();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.L)).as("L should not be allowed in GOLD_CLUB").isFalse();
        assertThat(goldClub.isQualificationAllowed(QualificationLevel.NO)).as("NO should not be allowed in GOLD_CLUB").isFalse();
    }

    @Test
    @DisplayName("findByChatId returns the matching group when chat ID exists")
    void findByChatId_ShouldReturnGroup_WhenChatIdExists() {
        var result = MaxChatGroupRequirements.findByChatId(-71062621438079L);

        assertThat(result)
                .as("Result should be present for known chat ID").
                isPresent()
                .as("Should return GOLD_CLUB for the known chat ID")
                .contains(MaxChatGroupRequirements.GOLD_CLUB);
    }

    @Test
    @DisplayName("findByChatId returns empty when chat ID does not exist")
    void findByChatId_ShouldReturnEmpty_WhenChatIdNotFound() {
        var result = MaxChatGroupRequirements.findByChatId(-9999999999L);

        assertThat(result).as("Result should be empty for unknown chat ID").isEmpty();
    }

    @Test
    @DisplayName("resolveGroupName returns group name when chat ID is known")
    void resolveGroupName_ShouldReturnGroupName_WhenChatIdIsKnown() {
        var name = MaxChatGroupRequirements.resolveGroupName(-71062621438079L);

        assertThat(name).as("Should return the correct group name for known chat ID").isEqualTo("Золотой клуб");
    }

    @Test
    @DisplayName("resolveGroupName returns default name when chat ID is unknown")
    void resolveGroupName_ShouldReturnDefaultName_WhenChatIdIsUnknown() {
        var name = MaxChatGroupRequirements.resolveGroupName(-9999999999L);

        assertThat(name).as("Should return default name for unknown chat ID").isEqualTo("клуб");
    }

    @Test
    @DisplayName("getChatId returns correct chat ID for GOLD_CLUB")
    void getChatId_ShouldReturnCorrectValue_WhenGoldClub() {
        assertThat(MaxChatGroupRequirements.GOLD_CLUB.getChatId()).as("GOLD_CLUB chat ID should match").isEqualTo(-71062621438079L);
    }

    @Test
    @DisplayName("getAllowedQualifications returns M and GM for GOLD_CLUB")
    void getAllowedQualifications_ShouldReturnMAndGM_WhenGoldClub() {
        var allowedQuals = MaxChatGroupRequirements.GOLD_CLUB.getAllowedQualifications();

        assertThat(allowedQuals)
                .as("GOLD_CLUB should have exactly 2 allowed qualifications")
                .hasSize(2)
                .as("GOLD_CLUB allowed qualifications should contain M")
                .contains(QualificationLevel.M)
                .as("GOLD_CLUB allowed qualifications should contain GM")
                .contains(QualificationLevel.GM);
    }
}
