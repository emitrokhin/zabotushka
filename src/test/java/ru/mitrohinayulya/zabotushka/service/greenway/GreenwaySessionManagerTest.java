package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.ws.rs.core.Form;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayAuthApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.RefreshTokenResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenwaySessionManagerTest {

    @Mock
    MyGreenwayAuthApi authApi;

    @Mock
    GreenwayTokenStore tokenStore;

    @InjectMocks
    GreenwaySessionManager sessionManager;

    @Test
    @DisplayName("refreshToken updates tokens in store when API returns success")
    void refreshToken_ShouldUpdateTokens_WhenApiReturnsSuccess() {
        when(tokenStore.getRefreshToken()).thenReturn("refresh-token");
        when(authApi.refreshToken(any(Form.class)))
                .thenReturn(new RefreshTokenResponse("new-access-token", "new-refresh-token", null, null));

        sessionManager.refreshToken();

        verify(tokenStore).setAccessToken("new-access-token");
        verify(tokenStore).setRefreshToken("new-refresh-token");
    }

    @Test
    @DisplayName("refreshToken clears store and throws GreenwayApiException when API returns error payload")
    void refreshToken_ShouldClearStoreAndThrow_WhenApiReturnsErrorPayload() {
        when(tokenStore.getRefreshToken()).thenReturn("refresh-token");
        when(authApi.refreshToken(any(Form.class)))
                .thenReturn(new RefreshTokenResponse(null, null, "TOKEN_EXPIRED", "Token has expired"));

        assertThatThrownBy(() -> sessionManager.refreshToken())
                .as("Should throw GreenwayApiException with error code on API error payload")
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("TOKEN_EXPIRED");

        verify(tokenStore).clear();
    }

    @Test
    @DisplayName("refreshToken throws IllegalStateException when refresh token is null")
    void refreshToken_ShouldThrow_WhenRefreshTokenIsNull() {
        when(tokenStore.getRefreshToken()).thenReturn(null);

        assertThatThrownBy(() -> sessionManager.refreshToken())
                .as("Should throw IllegalStateException when refresh token is not available")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Refresh token is not available");
    }
}
