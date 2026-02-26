package ru.mitrohinayulya.zabotushka.client.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/// Injects VK API access token and API version as query params into all outgoing VK API requests.
/// VK API uses query params for auth instead of Authorization header.
public class VkTokenRequestFilter implements ClientRequestFilter {

    public static final String VK_API_VERSION = "5.199";

    @Inject
    @ConfigProperty(name = "app.vk.bot-token")
    String accessToken;

    @Override
    public void filter(ClientRequestContext requestContext) {
        var newUri = UriBuilder.fromUri(requestContext.getUri())
                .queryParam("access_token", accessToken)
                .queryParam("v", VK_API_VERSION)
                .build();
        requestContext.setUri(newUri);
    }
}
