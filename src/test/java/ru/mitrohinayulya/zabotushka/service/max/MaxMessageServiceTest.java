package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaxMessageServiceTest {

    @Mock
    @RestClient
    MaxBotApi botApi;

    @InjectMocks
    MaxMessageService messageService;

    @Test
    @DisplayName("sendMessage does not throw when API returns successful response")
    void sendMessage_ShouldNotThrow_WhenResponseIsOk() {
        var response = Response.ok().build();
        when(botApi.sendMessage(eq(123L), any())).thenReturn(response);

        assertThatCode(() -> messageService.sendMessage(123L, "Hello"))
                .as("Should not throw when API returns 200 OK").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendMessage does not throw when API returns non-OK status")
    void sendMessage_ShouldNotThrow_WhenResponseIsNotOk() {
        var response = Response.status(500).build();
        when(botApi.sendMessage(eq(123L), any())).thenReturn(response);

        assertThatCode(() -> messageService.sendMessage(123L, "Hello"))
                .as("Should not throw when API returns error status").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendMessage does not throw when API call throws an exception")
    void sendMessage_ShouldNotThrow_WhenExceptionOccurs() {
        when(botApi.sendMessage(eq(123L), any())).thenThrow(new RuntimeException("Network error"));

        assertThatCode(() -> messageService.sendMessage(123L, "Hello"))
                .as("Should not throw when API throws exception").doesNotThrowAnyException();
    }
}
