package ru.mitrohinayulya.zabotushka.service.telegram;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.TelegramAccessBotApi;
import ru.mitrohinayulya.zabotushka.dto.telegram.TelegramResponse;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void registerWebhook_Disabled() throws Exception {
        setField("webhookEnabled", false);

        webhookRegistrar.registerWebhook();

        verify(telegramAccessBotApi, never()).setWebhook(any());
    }

    @Test
    void registerWebhook_Success() {
        when(telegramAccessBotApi.setWebhook(any()))
                .thenReturn(new TelegramResponse<>(true, true, null));

        assertDoesNotThrow(() -> webhookRegistrar.registerWebhook());

        verify(telegramAccessBotApi).setWebhook(any());
    }

    @Test
    void registerWebhook_Failure() {
        when(telegramAccessBotApi.setWebhook(any()))
                .thenReturn(new TelegramResponse<>(false, false, "Bad Request"));

        assertDoesNotThrow(() -> webhookRegistrar.registerWebhook());
    }

    @Test
    void unregisterWebhook_JustLogsWarning() {
        assertDoesNotThrow(() -> webhookRegistrar.unregisterWebhook());

        verifyNoInteractions(telegramAccessBotApi);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = TelegramWebhookRegistrar.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(webhookRegistrar, value);
    }
}
