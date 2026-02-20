package ru.mitrohinayulya.zabotushka.service.telegram;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.TelegramMessageBotApi;
import ru.mitrohinayulya.zabotushka.dto.telegram.TelegramResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramMessageServiceTest {

    @Mock
    @RestClient
    TelegramMessageBotApi telegramMessageBotApi;

    @InjectMocks
    TelegramMessageService messageService;

    @Test
    void sendMessage_Success() {
        when(telegramMessageBotApi.sendMessage(any()))
                .thenReturn(new TelegramResponse<>(true, null, null));

        assertDoesNotThrow(() -> messageService.sendMessage(123L, "Hello"));
    }

    @Test
    void sendMessage_ResponseNotOk() {
        when(telegramMessageBotApi.sendMessage(any()))
                .thenReturn(new TelegramResponse<>(false, null, "Bad Request"));

        assertDoesNotThrow(() -> messageService.sendMessage(123L, "Hello"));
    }

    @Test
    void sendMessage_Exception() {
        when(telegramMessageBotApi.sendMessage(any()))
                .thenThrow(new RuntimeException("Network error"));

        assertDoesNotThrow(() -> messageService.sendMessage(123L, "Hello"));
    }
}
