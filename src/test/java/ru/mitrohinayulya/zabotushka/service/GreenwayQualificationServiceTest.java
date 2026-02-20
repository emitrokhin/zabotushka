package ru.mitrohinayulya.zabotushka.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenwayQualificationServiceTest {

    @Mock
    GreenwayService greenwayService;

    @InjectMocks
    GreenwayQualificationService qualificationService;

    @Test
    void getBestQualification_ReturnsBestWhenBothPeriodsHaveQualifications() {
        var greenwayId = 100L;
        var currentPartner = createPartner(100, "M1");
        var previousPartner = createPartner(100, "GM2");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.of(previousPartner));

        var result = qualificationService.getBestQualification(greenwayId);

        assertEquals(QualificationLevel.GM, result);
    }

    @Test
    void getBestQualification_UsesCurrentWhenOnlyCurrentPeriodHasPartner() {
        var greenwayId = 100L;
        var currentPartner = createPartner(100, "L3");

        var currentResponse = new PartnerListResponse(null, List.of(currentPartner), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.of(currentPartner));
        when(greenwayService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.empty());

        var result = qualificationService.getBestQualification(greenwayId);

        assertEquals(QualificationLevel.L, result);
    }

    @Test
    void getBestQualification_UsesPreviousWhenOnlyPreviousPeriodHasPartner() {
        var greenwayId = 100L;
        var previousPartner = createPartner(100, "S1");

        var currentResponse = new PartnerListResponse(null, List.of(), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(previousPartner), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.empty());
        when(greenwayService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.of(previousPartner));

        var result = qualificationService.getBestQualification(greenwayId);

        assertEquals(QualificationLevel.S, result);
    }

    @Test
    void getBestQualification_ReturnsNoWhenNeitherPeriodHasPartner() {
        var greenwayId = 100L;

        var currentResponse = new PartnerListResponse(null, List.of(), null, null);
        var previousResponse = new PartnerListResponse(null, List.of(), null, null);

        when(greenwayService.getPreviousPeriod()).thenReturn(202501);
        when(greenwayService.getPartnerList(greenwayId, 0)).thenReturn(currentResponse);
        when(greenwayService.getPartnerList(greenwayId, 202501)).thenReturn(previousResponse);
        when(greenwayService.findPartnerById(currentResponse, greenwayId)).thenReturn(Optional.empty());
        when(greenwayService.findPartnerById(previousResponse, greenwayId)).thenReturn(Optional.empty());

        var result = qualificationService.getBestQualification(greenwayId);

        assertEquals(QualificationLevel.NO, result);
    }

    @Test
    void getBestQualification_ReturnsNoOnException() {
        var greenwayId = 100L;

        when(greenwayService.getPreviousPeriod()).thenThrow(new RuntimeException("API error"));

        var result = qualificationService.getBestQualification(greenwayId);

        assertEquals(QualificationLevel.NO, result);
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
