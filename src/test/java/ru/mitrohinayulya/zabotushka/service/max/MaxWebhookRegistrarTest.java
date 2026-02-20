package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.dto.max.MaxDeleteSubscriptionResponse;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaxWebhookRegistrarTest {

    @Mock
    @RestClient
    MaxBotApi botApi;

    @InjectMocks
    MaxWebhookRegistrar webhookRegistrar;

    @BeforeEach
    void setUp() throws Exception {
        setField("hostUrl", "https://example.com");
        setField("webhookPath", "/api/max/webhook");
        setField("webhookSecret", "secret123");
        setField("webhookEnabled", true);
    }

    @Test
    @DisplayName("registerWebhook skips API call when webhook is disabled")
    void registerWebhook_ShouldSkip_WhenWebhookIsDisabled() throws Exception {
        setField("webhookEnabled", false);

        webhookRegistrar.registerWebhook();

        verify(botApi, never()).setSubscription(any());
    }

    @Test
    @DisplayName("registerWebhook calls setSubscription API when webhook is enabled")
    void registerWebhook_ShouldCallApi_WhenEnabled() {
        var response = Response.ok().build();
        when(botApi.setSubscription(any())).thenReturn(response);

        assertThatCode(() -> webhookRegistrar.registerWebhook())
                .as("Should not throw when webhook registration succeeds").doesNotThrowAnyException();
        verify(botApi).setSubscription(any());
    }

    @Test
    @DisplayName("registerWebhook does not throw when API returns error status")
    void registerWebhook_ShouldNotThrow_WhenApiReturnsError() {
        var response = Response.status(500).build();
        when(botApi.setSubscription(any())).thenReturn(response);

        assertThatCode(() -> webhookRegistrar.registerWebhook())
                .as("Should not throw when API returns error status").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("unregisterWebhook skips API call when webhook is disabled")
    void unregisterWebhook_ShouldSkip_WhenWebhookIsDisabled() throws Exception {
        setField("webhookEnabled", false);

        assertThatCode(() -> webhookRegistrar.unregisterWebhook())
                .as("Should not throw when webhook is disabled").doesNotThrowAnyException();
        verify(botApi, never()).deleteSubscription(anyString());
    }

    @Test
    @DisplayName("unregisterWebhook skips API call when host URL is blank")
    void unregisterWebhook_ShouldSkip_WhenHostUrlIsBlank() throws Exception {
        setField("hostUrl", "");

        assertThatCode(() -> webhookRegistrar.unregisterWebhook())
                .as("Should not throw when host URL is blank").doesNotThrowAnyException();
        verify(botApi, never()).deleteSubscription(anyString());
    }

    @Test
    @DisplayName("unregisterWebhook calls deleteSubscription with full URL when enabled")
    void unregisterWebhook_ShouldCallDeleteApi_WhenEnabled() {
        when(botApi.deleteSubscription(anyString()))
                .thenReturn(new MaxDeleteSubscriptionResponse(true, null));

        assertThatCode(() -> webhookRegistrar.unregisterWebhook())
                .as("Should not throw when unregistration succeeds").doesNotThrowAnyException();
        verify(botApi).deleteSubscription("https://example.com/api/max/webhook");
    }

    @Test
    @DisplayName("unregisterWebhook does not throw when API returns failure response")
    void unregisterWebhook_ShouldNotThrow_WhenApiReturnsFalse() {
        when(botApi.deleteSubscription(anyString()))
                .thenReturn(new MaxDeleteSubscriptionResponse(false, "Error"));

        assertThatCode(() -> webhookRegistrar.unregisterWebhook())
                .as("Should not throw when API returns failure response").doesNotThrowAnyException();
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = MaxWebhookRegistrar.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(webhookRegistrar, value);
    }
}
