package ru.mitrohinayulya.zabotushka.service.telegram;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.TelegramMessageBotApi;
import ru.mitrohinayulya.zabotushka.dto.telegram.TelegramResponse;

import static org.assertj.core.api.Assertions.assertThatCode;
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
    @DisplayName("sendMessage does not throw when API returns successful response")
    void sendMessage_ShouldNotThrow_WhenResponseIsOk() {
        when(telegramMessageBotApi.sendMessage(any()))
                .thenReturn(new TelegramResponse<>(true, null, null));

        assertThatCode(() -> messageService.sendMessage(123L, "Hello"))
                .as("Should not throw when API returns OK response").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendMessage does not throw when API returns non-OK response")
    void sendMessage_ShouldNotThrow_WhenResponseIsNotOk() {
        when(telegramMessageBotApi.sendMessage(any()))
                .thenReturn(new TelegramResponse<>(false, null, "Bad Request"));

        assertThatCode(() -> messageService.sendMessage(123L, "Hello"))
                .as("Should not throw when API returns error response").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendMessage does not throw when API call throws an exception")
    void sendMessage_ShouldNotThrow_WhenExceptionOccurs() {
        when(telegramMessageBotApi.sendMessage(any()))
                .thenThrow(new RuntimeException("Network error"));

        assertThatCode(() -> messageService.sendMessage(123L, "Hello"))
                .as("Should not throw when API throws exception").doesNotThrowAnyException();
    }
}
