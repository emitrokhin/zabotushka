package ru.mitrohinayulya.zabotushka.interceptor;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwaySessionManager;

import java.util.Arrays;

/// Intercepts methods annotated with @RefreshTokenOnExpiry.
/// On HTTP 401 (expired access token), refreshes the token and retries once.
/// If refresh itself fails, the error propagates — a full re-login is needed.
@Interceptor
@RefreshTokenOnExpiry
@Priority(Interceptor.Priority.APPLICATION)
public class TokenRefreshInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshInterceptor.class);

    @Inject
    GreenwaySessionManager sessionManager;

    @AroundInvoke
    Object refreshAndRetryOnExpiry(InvocationContext context) throws Exception {
        try {
            return context.proceed();
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() != Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new GreenwayApiException(buildMessage(context), e);
            }

            log.info("Access token expired on {}, refreshing and retrying", context.getMethod().getName());

            try {
                sessionManager.refreshToken();
                return context.proceed();
            } catch (GreenwayApiException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new GreenwayApiException(buildMessage(context) + " after token refresh retry", ex);
            }
        }
    }

    private String buildMessage(InvocationContext context) {
        return "Failed to call %s with args %s".formatted(
                context.getMethod().getName(),
                Arrays.toString(context.getParameters()));
    }
}
