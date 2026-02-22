package ru.mitrohinayulya.zabotushka.interceptor;

import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwaySessionManager;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRefreshInterceptorTest {

    @Mock
    GreenwaySessionManager sessionManager;

    @Mock
    InvocationContext context;

    @InjectMocks
    TokenRefreshInterceptor interceptor;

    @Test
    @DisplayName("Returns result when proceed succeeds")
    void refreshAndRetryOnExpiry_ShouldReturnResult_WhenProceedSucceeds() throws Exception {
        var expected = "success";
        when(context.proceed()).thenReturn(expected);

        var result = interceptor.refreshAndRetryOnExpiry(context);

        assertThat(result).as("Should return proceed result").isEqualTo(expected);
        verifyNoInteractions(sessionManager);
    }

    @Test
    @DisplayName("Refreshes token and retries when access token expired (401)")
    void refreshAndRetryOnExpiry_ShouldRefreshAndRetry_WhenAccessTokenExpired() throws Exception {
        var unauthorized = new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        var expected = "retried";
        when(context.proceed()).thenThrow(unauthorized).thenReturn(expected);
        when(context.getMethod()).thenReturn(stubMethod());

        var result = interceptor.refreshAndRetryOnExpiry(context);

        assertThat(result).as("Should return retried result").isEqualTo(expected);
        verify(sessionManager).refreshToken();
        verify(context, times(2)).proceed();
    }

    @Test
    @DisplayName("Wraps non-401 WebApplicationException in GreenwayApiException")
    void refreshAndRetryOnExpiry_ShouldWrapException_WhenNon401Error() throws Exception {
        var serverError = new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        when(context.proceed()).thenThrow(serverError);
        when(context.getMethod()).thenReturn(stubMethod());
        when(context.getParameters()).thenReturn(new Object[]{123L, 0});

        assertThatThrownBy(() -> interceptor.refreshAndRetryOnExpiry(context))
                .as("Should throw GreenwayApiException for non-401 errors")
                .isInstanceOf(GreenwayApiException.class)
                .hasCause(serverError);

        verifyNoInteractions(sessionManager);
    }

    @Test
    @DisplayName("Throws GreenwayApiException when retry fails after refresh")
    void refreshAndRetryOnExpiry_ShouldThrowException_WhenRetryFailsAfterRefresh() throws Exception {
        var unauthorized = new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        var retryError = new RuntimeException("Service unavailable");
        when(context.proceed()).thenThrow(unauthorized).thenThrow(retryError);
        when(context.getMethod()).thenReturn(stubMethod());
        when(context.getParameters()).thenReturn(new Object[]{123L, 0});

        assertThatThrownBy(() -> interceptor.refreshAndRetryOnExpiry(context))
                .as("Should throw GreenwayApiException with retry context")
                .isInstanceOf(GreenwayApiException.class)
                .hasMessageContaining("after token refresh retry")
                .hasCause(retryError);

        verify(sessionManager).refreshToken();
    }

    @Test
    @DisplayName("Propagates GreenwayApiException from refresh without wrapping")
    void refreshAndRetryOnExpiry_ShouldPropagateGreenwayApiException_WhenRefreshFails() throws Exception {
        var unauthorized = new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        var refreshError = new GreenwayApiException("Token refresh failed");
        when(context.proceed()).thenThrow(unauthorized);
        when(context.getMethod()).thenReturn(stubMethod());
        doThrow(refreshError).when(sessionManager).refreshToken();

        assertThatThrownBy(() -> interceptor.refreshAndRetryOnExpiry(context))
                .as("Should propagate GreenwayApiException from refresh")
                .isSameAs(refreshError);
    }

    private Method stubMethod() throws NoSuchMethodException {
        return String.class.getMethod("toString");
    }
}
