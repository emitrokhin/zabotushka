package ru.mitrohinayulya.zabotushka.service.greenway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayPartnerApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenwayPartnerServiceTest {

    @Mock
    MyGreenwayPartnerApi apiClient;

    @InjectMocks
    GreenwayPartnerService greenwayPartnerService;

    @Test
    @DisplayName("getPartnerList returns response when API call succeeds")
    void getPartnerList_ShouldReturnResponse_WhenApiCallSucceeds() {
        var response = new PartnerListResponse(null, List.of(), null, null);
        when(apiClient.getPartnerList(123L, null)).thenReturn(response);

        var result = greenwayPartnerService.getPartnerList(123L, 0);

        assertThat(result).as("Should return the API response").isSameAs(response);
    }

    @Test
    @DisplayName("getPartnerList passes previous period when greater than zero")
    void getPartnerList_ShouldPassPreviousPeriod_WhenGreaterThanZero() {
        var response = new PartnerListResponse(null, List.of(), null, null);
        when(apiClient.getPartnerList(123L, 202501)).thenReturn(response);

        var result = greenwayPartnerService.getPartnerList(123L, 202501);

        assertThat(result).as("Should return the API response").isSameAs(response);
    }

    @Test
    @DisplayName("getPartnerList throws GreenwayApiException when API returns error payload")
    void getPartnerList_ShouldThrowGreenwayApiException_WhenApiReturnsErrorPayload() {
        var responseWithError = new PartnerListResponse(null, List.of(), "ERROR_CODE", "Error details");
        when(apiClient.getPartnerList(123L, null)).thenReturn(responseWithError);

        assertThatThrownBy(() -> greenwayPartnerService.getPartnerList(123L, 0))
                .as("Should throw GreenwayApiException with context and error code")
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("partnerId=123")
                .hasMessageContaining("period=0")
                .hasMessageContaining("ERROR_CODE");
    }

    @Test
    @DisplayName("findPartnerById returns partner when matching number exists")
    void findPartnerById_ShouldReturnPartner_WhenMatchingNumberExists() {
        var partner = new Partner(1, null, null, null, null, null, null, 123,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        var result = greenwayPartnerService.findPartnerById(response, 123L);

        assertThat(result).as("Should find the partner with matching number").isPresent().contains(partner);
    }

    @Test
    @DisplayName("findPartnerById returns empty when no matching partner")
    void findPartnerById_ShouldReturnEmpty_WhenNoMatchingPartner() {
        var partner = new Partner(1, null, null, null, null, null, null, 456,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var response = new PartnerListResponse(null, List.of(partner), null, null);

        var result = greenwayPartnerService.findPartnerById(response, 123L);

        assertThat(result).as("Should be empty when no partner matches").isEmpty();
    }

    @Test
    @DisplayName("findPartnerById returns empty when response is null")
    void findPartnerById_ShouldReturnEmpty_WhenResponseIsNull() {
        var result = greenwayPartnerService.findPartnerById(null, 123L);

        assertThat(result).as("Should be empty when response is null").isEmpty();
    }

    @Test
    @DisplayName("findPartnerById returns empty when partners list is empty")
    void findPartnerById_ShouldReturnEmpty_WhenPartnersListIsEmpty() {
        var response = new PartnerListResponse(null, Collections.emptyList(), null, null);

        var result = greenwayPartnerService.findPartnerById(response, 123L);

        assertThat(result).as("Should be empty when partners list is empty").isEmpty();
    }
}
