package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;
import ru.mitrohinayulya.zabotushka.dto.max.MaxDeleteSubscriptionResponse;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void registerWebhook_Disabled() throws Exception {
        setField("webhookEnabled", false);

        webhookRegistrar.registerWebhook();

        verify(botApi, never()).setSubscription(any());
    }

    @Test
    void registerWebhook_Success() {
        var response = Response.ok().build();
        when(botApi.setSubscription(any())).thenReturn(response);

        assertDoesNotThrow(() -> webhookRegistrar.registerWebhook());

        verify(botApi).setSubscription(any());
    }

    @Test
    void registerWebhook_Failure() {
        var response = Response.status(500).build();
        when(botApi.setSubscription(any())).thenReturn(response);

        assertDoesNotThrow(() -> webhookRegistrar.registerWebhook());
    }

    @Test
    void unregisterWebhook_Disabled() throws Exception {
        setField("webhookEnabled", false);

        assertDoesNotThrow(() -> webhookRegistrar.unregisterWebhook());

        verify(botApi, never()).deleteSubscription(anyString());
    }

    @Test
    void unregisterWebhook_HostUrlBlank() throws Exception {
        setField("hostUrl", "");

        assertDoesNotThrow(() -> webhookRegistrar.unregisterWebhook());

        verify(botApi, never()).deleteSubscription(anyString());
    }

    @Test
    void unregisterWebhook_Success() {
        when(botApi.deleteSubscription(anyString()))
                .thenReturn(new MaxDeleteSubscriptionResponse(true, null));

        assertDoesNotThrow(() -> webhookRegistrar.unregisterWebhook());

        verify(botApi).deleteSubscription("https://example.com/api/max/webhook");
    }

    @Test
    void unregisterWebhook_Failure() {
        when(botApi.deleteSubscription(anyString()))
                .thenReturn(new MaxDeleteSubscriptionResponse(false, "Error"));

        assertDoesNotThrow(() -> webhookRegistrar.unregisterWebhook());
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = MaxWebhookRegistrar.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(webhookRegistrar, value);
    }
}
