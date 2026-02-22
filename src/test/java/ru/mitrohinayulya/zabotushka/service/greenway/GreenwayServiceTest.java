package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.RefreshTokenResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenwayServiceTest {

    @Mock
    MyGreenwayApi apiClient;

    @InjectMocks
    GreenwayService greenwayService;

    @Test
    @DisplayName("getPartnerList returns response when API call succeeds")
    void getPartnerList_ShouldReturnResponse_WhenApiCallSucceeds() {
        setAccessToken("access-token");
        var response = new PartnerListResponse(null, List.of(), null, null);

        when(apiClient.getPartnerList("Bearer access-token", 123L, null)).thenReturn(response);

        var result = greenwayService.getPartnerList(123L, 0);

        assertThat(result).isSameAs(response);
        verify(apiClient, never()).refreshToken(any(Form.class));
    }

    @Test
    @DisplayName("getPartnerList throws GreenwayApiException when API returns error payload")
    void getPartnerList_ShouldThrowGreenwayApiException_WhenApiReturnsErrorPayload() {
        setAccessToken("access-token");
        var responseWithError = new PartnerListResponse(null, List.of(), "ERROR_CODE", "Error details");

        when(apiClient.getPartnerList("Bearer access-token", 123L, null)).thenReturn(responseWithError);

        assertThatThrownBy(() -> greenwayService.getPartnerList(123L, 0))
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("partnerId=123")
                .hasMessageContaining("period=0")
                .hasMessageContaining("ERROR_CODE");

        verify(apiClient, never()).refreshToken(any(Form.class));
    }

    @Test
    @DisplayName("getPartnerList refreshes token and retries once when first call fails with unauthorized")
    void getPartnerList_ShouldRefreshAndRetry_WhenFirstCallIsUnauthorized() {
        setAccessToken("stale-token");
        setRefreshToken("refresh-token");
        var unauthorized = new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        var successResponse = new PartnerListResponse(null, List.of(), null, null);

        when(apiClient.getPartnerList("Bearer stale-token", 123L, null)).thenThrow(unauthorized);
        when(apiClient.refreshToken(any(Form.class)))
                .thenReturn(new RefreshTokenResponse("fresh-token", "new-refresh-token", null, null));
        when(apiClient.getPartnerList("Bearer fresh-token", 123L, null)).thenReturn(successResponse);

        var result = greenwayService.getPartnerList(123L, 0);

        assertThat(result).isSameAs(successResponse);
        verify(apiClient, times(2)).getPartnerList(anyString(), eq(123L), isNull());
        verify(apiClient, times(1)).refreshToken(any(Form.class));
    }

    @Test
    @DisplayName("getPartnerList throws contextual exception when retry fails after refresh")
    void getPartnerList_ShouldThrowContextualException_WhenRetryFailsAfterRefresh() {
        setAccessToken("stale-token");
        setRefreshToken("refresh-token");
        var unauthorized = new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        var retryError = new RuntimeException("Remote service unavailable");

        when(apiClient.getPartnerList("Bearer stale-token", 123L, null)).thenThrow(unauthorized);
        when(apiClient.refreshToken(any(Form.class)))
                .thenReturn(new RefreshTokenResponse("fresh-token", "new-refresh-token", null, null));
        when(apiClient.getPartnerList("Bearer fresh-token", 123L, null)).thenThrow(retryError);

        assertThatThrownBy(() -> greenwayService.getPartnerList(123L, 0))
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("partnerId=123")
                .hasMessageContaining("period=0")
                .hasMessageContaining("after token refresh retry");

        verify(apiClient, times(2)).getPartnerList(anyString(), eq(123L), isNull());
        verify(apiClient, times(1)).refreshToken(any(Form.class));
    }

    @Test
    @DisplayName("getPartnerList does not retry when failure is not auth related")
    void getPartnerList_ShouldNotRetry_WhenFailureIsNotAuthRelated() {
        setAccessToken("access-token");
        var internalServerError = new WebApplicationException(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());

        when(apiClient.getPartnerList("Bearer access-token", 123L, null)).thenThrow(internalServerError);

        assertThatThrownBy(() -> greenwayService.getPartnerList(123L, 0))
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("partnerId=123")
                .hasMessageContaining("period=0");

        verify(apiClient, times(1)).getPartnerList("Bearer access-token", 123L, null);
        verify(apiClient, never()).refreshToken(any(Form.class));
    }

    @Test
    @DisplayName("getPartnerList throws IllegalStateException when access token is missing")
    void getPartnerList_ShouldThrowIllegalStateException_WhenAccessTokenMissing() {
        assertThatThrownBy(() -> greenwayService.getPartnerList(123L, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Access token is not available");

        verifyNoInteractions(apiClient);
    }

    private void setAccessToken(String token) {
        setAtomicReferenceField("accessToken", token);
    }

    private void setRefreshToken(String token) {
        setAtomicReferenceField("refreshToken", token);
    }

    private void setAtomicReferenceField(String fieldName, String value) {
        try {
            Field field = GreenwayService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var reference = (AtomicReference<String>) field.get(greenwayService);
            reference.set(value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to set field: " + fieldName, e);
        }
    }
}
