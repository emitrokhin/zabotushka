package ru.mitrohinayulya.zabotushka.service.telegram;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.dto.telegram.TelegramResponse;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramWebhookRegistrarTest {

    @Mock
    @RestClient
    TelegramAccessBotApi telegramAccessBotApi;

    @InjectMocks
    TelegramWebhookRegistrar webhookRegistrar;

    @BeforeEach
    void setUp() throws Exception {
        setField("hostUrl", "https://example.com");
        setField("webhookPath", "/api/telegram/webhook");
        setField("webhookSecret", "secret123");
        setField("webhookEnabled", true);
    }

    @Test
    @DisplayName("registerWebhook skips API call when webhook is disabled")
    void registerWebhook_ShouldSkip_WhenWebhookIsDisabled() throws Exception {
        setField("webhookEnabled", false);

        webhookRegistrar.registerWebhook();

        verify(telegramAccessBotApi, never()).setWebhook(any());
    }

    @Test
    @DisplayName("registerWebhook calls setWebhook API when webhook is enabled")
    void registerWebhook_ShouldCallApi_WhenEnabled() {
        when(telegramAccessBotApi.setWebhook(any()))
                .thenReturn(new TelegramResponse<>(true, true, null));

        assertThatCode(() -> webhookRegistrar.registerWebhook())
                .as("Should not throw when webhook registration succeeds").doesNotThrowAnyException();
        verify(telegramAccessBotApi).setWebhook(any());
    }

    @Test
    @DisplayName("registerWebhook does not throw when API returns failure response")
    void registerWebhook_ShouldNotThrow_WhenApiReturnsFalse() {
        when(telegramAccessBotApi.setWebhook(any()))
                .thenReturn(new TelegramResponse<>(false, false, "Bad Request"));

        assertThatCode(() -> webhookRegistrar.registerWebhook())
                .as("Should not throw when API returns failure").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("unregisterWebhook does not interact with API")
    void unregisterWebhook_ShouldNotInteractWithApi() {
        assertThatCode(() -> webhookRegistrar.unregisterWebhook())
                .as("Should not throw when unregistering").doesNotThrowAnyException();
        verifyNoInteractions(telegramAccessBotApi);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = TelegramWebhookRegistrar.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(webhookRegistrar, value);
    }
}
