package ru.mitrohinayulya.zabotushka.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayPartnerService;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;

import java.util.List;
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
    @DisplayName("getBestQualification returns the higher level when both periods have qualifications")
    void getBestQualification_ShouldReturnBestLevel_WhenBothPeriodsHaveQualifications() {
        var greenwayId = 100L;
        var currentPartner = createPartner(100, "M1");
        var previousPartner = createPartner(100, "GM2");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayPartnerService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayPartnerService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayPartnerService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayPartnerService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.of(currentPartner));
        when(greenwayPartnerService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.of(previousPartner));

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return the higher qualification level GM from previous period").isEqualTo(QualificationLevel.GM);
    }

    @Test
    @DisplayName("getBestQualification uses current period when only current period has partner")
    void getBestQualification_ShouldUseCurrentPeriod_WhenOnlyCurrentHasPartner() {
        var greenwayId = 100L;
        var currentPartner = createPartner(100, "L3");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayPartnerService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayPartnerService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayPartnerService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayPartnerService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.of(currentPartner));
        when(greenwayPartnerService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.empty());

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return current period qualification L when previous is empty").isEqualTo(QualificationLevel.L);
    }

    @Test
    @DisplayName("getBestQualification uses previous period when only previous period has partner")
    void getBestQualification_ShouldUsePreviousPeriod_WhenOnlyPreviousHasPartner() {
        var greenwayId = 100L;
        var previousPartner = createPartner(100, "S1");

        var currentResponse = new PartnerListResponse(null, List.of(), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayPartnerService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayPartnerService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayPartnerService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayPartnerService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.empty());
        when(greenwayPartnerService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.of(previousPartner));

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return previous period qualification S when current is empty").isEqualTo(QualificationLevel.S);
    }

    @Test
    @DisplayName("getBestQualification returns NO when neither period has partner")
    void getBestQualification_ShouldReturnNO_WhenNeitherPeriodHasPartner() {
        var greenwayId = 100L;

        var currentResponse = new PartnerListResponse(null, List.of(), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayPartnerService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayPartnerService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayPartnerService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayPartnerService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.empty());
        when(greenwayPartnerService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.empty());

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return NO when partner is not found in either period").isEqualTo(QualificationLevel.NO);
    }

    @Test
    @DisplayName("getBestQualification returns NO when an exception occurs during API call")
    void getBestQualification_ShouldReturnNO_WhenExceptionOccurs() {
        var greenwayId = 100L;

        when(greenwayPartnerService.getPreviousPeriod()).thenThrow(new RuntimeException("API error"));

        var result = qualificationService.getBestQualification(greenwayId);

        assertThat(result).as("Should return NO when API throws exception").isEqualTo(QualificationLevel.NO);
    }

    private Partner createPartner(int number, String qualification) {
        return new Partner(
                null, null, null, null, null, null, null,
                number, null, null, null, null, null, null,
                null, null, null, null, null, null, qualification,
                null, null, null
        );
    }
}
