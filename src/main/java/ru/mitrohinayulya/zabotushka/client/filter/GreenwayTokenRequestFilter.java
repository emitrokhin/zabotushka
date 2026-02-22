package ru.mitrohinayulya.zabotushka.client.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayTokenStore;

/// Injects the Bearer token into outgoing requests for the MyGreenway API.
/// Registered only on MyGreenwayApi which contains authenticated endpoints.
public class GreenwayTokenRequestFilter implements ClientRequestFilter {

    @Inject
    GreenwayTokenStore tokenStore;

    @Override
    public void filter(ClientRequestContext requestContext) {
        var accessToken = tokenStore.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("Access token is not available. Please login first.");
        }

        requestContext.getHeaders().putSingle("Authorization", "Bearer " + accessToken);
    }
}
