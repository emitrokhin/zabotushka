package ru.mitrohinayulya.zabotushka.client.filter;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayTokenStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenwayTokenRequestFilterTest {

    @Mock
    GreenwayTokenStore tokenStore;

    @Mock
    ClientRequestContext requestContext;

    @InjectMocks
    GreenwayTokenRequestFilter filter;

    @Test
    @DisplayName("Sets Authorization header when token is present")
    void filter_ShouldSetAuthorizationHeader_WhenTokenIsPresent() {
        when(tokenStore.getAccessToken()).thenReturn("my-token");
        var headers = new MultivaluedHashMap<String, Object>();
        when(requestContext.getHeaders()).thenReturn(headers);

        filter.filter(requestContext);

        assertThat(headers.getFirst("Authorization"))
                .as("Should set Bearer token header")
                .isEqualTo("Bearer my-token");
    }

    @Test
    @DisplayName("Throws IllegalStateException when token is null")
    void filter_ShouldThrowIllegalStateException_WhenTokenIsNull() {
        when(tokenStore.getAccessToken()).thenReturn(null);

        assertThatThrownBy(() -> filter.filter(requestContext))
                .as("Should throw when access token is missing")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Access token is not available");
    }
}
