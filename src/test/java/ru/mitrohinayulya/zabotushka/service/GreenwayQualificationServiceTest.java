package ru.mitrohinayulya.zabotushka.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayPartnerService;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenwayQualificationServiceTest {

    @Mock
    GreenwayPartnerService greenwayPartnerService;

    @InjectMocks
    GreenwayQualificationService qualificationService;

    @Test
    @DisplayName("getBestQualification returns the higher level when both qual and prev_qual are present")
    void getBestQualification_ShouldReturnBestLevel_WhenBothQualAndPrevQualPresent() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId))
                .thenReturn(Optional.of(createPartner(100, "M1", "GM2")));

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return the higher qualification level GM from prev_qual").isEqualTo(QualificationLevel.GM);
    }

    @Test
    @DisplayName("getBestQualification uses qual when only qual is present")
    void getBestQualification_ShouldUseQual_WhenOnlyQualPresent() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId))
                .thenReturn(Optional.of(createPartner(100, "L3", null)));

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return L from qual when prev_qual is absent").isEqualTo(QualificationLevel.L);
    }

    @Test
    @DisplayName("getBestQualification uses prev_qual when only prev_qual is present")
    void getBestQualification_ShouldUsePrevQual_WhenOnlyPrevQualPresent() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId))
                .thenReturn(Optional.of(createPartner(100, null, "S1")));

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return S from prev_qual when qual is absent").isEqualTo(QualificationLevel.S);
    }

    @Test
    @DisplayName("getBestQualification returns NO when partner is not found")
    void getBestQualification_ShouldReturnNO_WhenPartnerNotFound() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId)).thenReturn(Optional.empty());

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return NO when partner is not found").isEqualTo(QualificationLevel.NO);
    }

    @Test
    @DisplayName("getBestQualification returns NO when an exception occurs during API call")
    void getBestQualification_ShouldReturnNO_WhenExceptionOccurs() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId)).thenThrow(new RuntimeException("API error"));

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return NO when API throws exception").isEqualTo(QualificationLevel.NO);
    }

    @Test
    @DisplayName("getBestQualificationResult returns qual raw when qual is better than prev_qual")
    void getBestQualificationResult_ShouldReturnQualRaw_WhenQualIsBetter() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId))
                .thenReturn(Optional.of(createPartner(100, "GM4", "M2")));

        var result = qualificationService.getBestQualificationResult(greenwayId);

        assertThat(result.level()).as("Level should be GM from qual").isEqualTo(QualificationLevel.GM);
        assertThat(result.rawQual()).as("Raw qualification should be the qual string GM4").isEqualTo("GM4");
    }

    @Test
    @DisplayName("getBestQualificationResult returns prev_qual raw when prev_qual is better than qual")
    void getBestQualificationResult_ShouldReturnPrevQualRaw_WhenPrevQualIsBetter() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId))
                .thenReturn(Optional.of(createPartner(100, "S1", "L3")));

        var result = qualificationService.getBestQualificationResult(greenwayId);

        assertThat(result.level()).as("Level should be L from prev_qual").isEqualTo(QualificationLevel.L);
        assertThat(result.rawQual()).as("Raw qualification should be the prev_qual string L3").isEqualTo("L3");
    }

    @Test
    @DisplayName("getBestQualificationResult returns null raw qualification when partner is not found")
    void getBestQualificationResult_ShouldReturnNullRawQual_WhenPartnerNotFound() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId)).thenReturn(Optional.empty());

        var result = qualificationService.getBestQualificationResult(greenwayId);

        assertThat(result.level()).as("Level should be NO when partner not found").isEqualTo(QualificationLevel.NO);
        assertThat(result.rawQual()).as("Raw qualification should be null when partner not found").isNull();
    }

    @Test
    @DisplayName("getBestQualificationResult returns prev_qual raw when both levels are equal")
    void getBestQualificationResult_ShouldReturnPrevQualRaw_WhenBothLevelsAreEqual() {
        var greenwayId = 100L;
        when(greenwayPartnerService.findCurrentPartner(greenwayId))
                .thenReturn(Optional.of(createPartner(100, "M1", "M3")));

        var result = qualificationService.getBestQualificationResult(greenwayId);

        assertThat(result.level()).as("Level should be M when both are M").isEqualTo(QualificationLevel.M);
        assertThat(result.rawQual()).as("Raw qualification should be from prev_qual when levels are equal").isEqualTo("M3");
    }

    private Partner createPartner(int number, String qualification, String prevQualification) {
        return new Partner(
                null, null, null, null, null, null, null,
                number, null, null, null, null, null, null,
                null, null, null, null, null, null, qualification,
                prevQualification, null, null, null
        );
    }
}
