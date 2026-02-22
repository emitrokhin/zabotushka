package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenwayPartnerServiceTest {

    @Mock
    MyGreenwayApi apiClient;

    @Mock
    GreenwayTokenStore tokenStore;

    @Mock
    GreenwaySessionManager sessionManager;

    @InjectMocks
    GreenwayPartnerService greenwayPartnerService;

    @Test
    @DisplayName("getPartnerList returns response when API call succeeds")
    void getPartnerList_ShouldReturnResponse_WhenApiCallSucceeds() {
        when(tokenStore.getAccessToken()).thenReturn("access-token");
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(apiClient.getPartnerList("Bearer access-token", 123L, null)).thenReturn(response);

        var result = greenwayPartnerService.getPartnerList(123L, 0);

        assertThat(result).as("Should return the API response").isSameAs(response);
        verifyNoInteractions(sessionManager);
    }

    @Test
    @DisplayName("getPartnerList throws GreenwayApiException when API returns error payload")
    void getPartnerList_ShouldThrowGreenwayApiException_WhenApiReturnsErrorPayload() {
        when(tokenStore.getAccessToken()).thenReturn("access-token");
        var responseWithError = new PartnerListResponse(null, List.of(), "ERROR_CODE", "Error details");

        when(apiClient.getPartnerList("Bearer access-token", 123L, null)).thenReturn(responseWithError);

        assertThatThrownBy(() -> greenwayPartnerService.getPartnerList(123L, 0))
                .as("Should throw GreenwayApiException with context and error code")
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("partnerId=123")
                .hasMessageContaining("period=0")
                .hasMessageContaining("ERROR_CODE");

        verifyNoInteractions(sessionManager);
    }

    @Test
    @DisplayName("getPartnerList refreshes token and retries once when first call fails with unauthorized")
    void getPartnerList_ShouldRefreshAndRetry_WhenFirstCallIsUnauthorized() {
        when(tokenStore.getAccessToken()).thenReturn("stale-token", "fresh-token");
        var unauthorized = new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        var successResponse = new PartnerListResponse(null, List.of(), null, null);

        when(apiClient.getPartnerList("Bearer stale-token", 123L, null)).thenThrow(unauthorized);
        when(apiClient.getPartnerList("Bearer fresh-token", 123L, null)).thenReturn(successResponse);

        var result = greenwayPartnerService.getPartnerList(123L, 0);

        assertThat(result).as("Should return response after successful retry").isSameAs(successResponse);
        verify(apiClient, times(2)).getPartnerList(anyString(), eq(123L), isNull());
        verify(sessionManager).refreshToken();
    }

    @Test
    @DisplayName("getPartnerList throws contextual exception when retry fails after refresh")
    void getPartnerList_ShouldThrowContextualException_WhenRetryFailsAfterRefresh() {
        when(tokenStore.getAccessToken()).thenReturn("stale-token", "fresh-token");
        var unauthorized = new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        var retryError = new RuntimeException("Remote service unavailable");

        when(apiClient.getPartnerList("Bearer stale-token", 123L, null)).thenThrow(unauthorized);
        when(apiClient.getPartnerList("Bearer fresh-token", 123L, null)).thenThrow(retryError);

        assertThatThrownBy(() -> greenwayPartnerService.getPartnerList(123L, 0))
                .as("Should throw GreenwayApiException with retry context")
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("partnerId=123")
                .hasMessageContaining("period=0")
                .hasMessageContaining("after token refresh retry");

        verify(apiClient, times(2)).getPartnerList(anyString(), eq(123L), isNull());
        verify(sessionManager).refreshToken();
    }

    @Test
    @DisplayName("getPartnerList does not retry when failure is not auth related")
    void getPartnerList_ShouldNotRetry_WhenFailureIsNotAuthRelated() {
        when(tokenStore.getAccessToken()).thenReturn("access-token");
        var internalServerError = new WebApplicationException(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());

        when(apiClient.getPartnerList("Bearer access-token", 123L, null)).thenThrow(internalServerError);

        assertThatThrownBy(() -> greenwayPartnerService.getPartnerList(123L, 0))
                .as("Should throw GreenwayApiException without retry")
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("partnerId=123")
                .hasMessageContaining("period=0");

        verify(apiClient, times(1)).getPartnerList("Bearer access-token", 123L, null);
        verifyNoInteractions(sessionManager);
    }

    @Test
    @DisplayName("getPartnerList throws IllegalStateException when access token is missing")
    void getPartnerList_ShouldThrowIllegalStateException_WhenAccessTokenMissing() {
        when(tokenStore.getAccessToken()).thenReturn(null);

        assertThatThrownBy(() -> greenwayPartnerService.getPartnerList(123L, 0))
                .as("Should throw IllegalStateException when token is missing")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Access token is not available");

        verifyNoInteractions(apiClient);
    }
}
