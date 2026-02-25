package ru.mitrohinayulya.zabotushka.dto.greenway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel.*;

class QualificationLevelTest {

    @Test
    @DisplayName("isAtLeast returns true when level exceeds required")
    void isAtLeast_ShouldReturnTrue_WhenLevelExceedsRequired() {
        assertThat(GM.isAtLeast(M)).as("GM should be at least M").isTrue();
        assertThat(GM.isAtLeast(L)).as("GM should be at least L").isTrue();
        assertThat(M.isAtLeast(S)).as("M should be at least S").isTrue();
        assertThat(L.isAtLeast(NO)).as("L should be at least NO").isTrue();
    }

    @Test
    @DisplayName("isAtLeast returns true when level equals required")
    void isAtLeast_ShouldReturnTrue_WhenLevelEqualsRequired() {
        assertThat(GM.isAtLeast(GM)).as("GM should be at least GM").isTrue();
        assertThat(M.isAtLeast(M)).as("M should be at least M").isTrue();
        assertThat(S.isAtLeast(S)).as("S should be at least S").isTrue();
        assertThat(NO.isAtLeast(NO)).as("NO should be at least NO").isTrue();
    }

    @Test
    @DisplayName("isAtLeast returns false when level is below required")
    void isAtLeast_ShouldReturnFalse_WhenLevelBelowRequired() {
        assertThat(S.isAtLeast(M)).as("S should not be at least M").isFalse();
        assertThat(L.isAtLeast(GM)).as("L should not be at least GM").isFalse();
        assertThat(NO.isAtLeast(S)).as("NO should not be at least S").isFalse();
        assertThat(M.isAtLeast(GM)).as("M should not be at least GM").isFalse();
    }
}
