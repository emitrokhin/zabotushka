package ru.mitrohinayulya.zabotushka.service.max;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mitrohinayulya.zabotushka.client.MaxBotApi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void sendMessage_Success() {
        var response = Response.ok().build();
        when(botApi.sendMessage(eq(123L), any())).thenReturn(response);

        assertDoesNotThrow(() -> messageService.sendMessage(123L, "Hello"));
    }

    @Test
    void sendMessage_NonOkStatus() {
        var response = Response.status(500).build();
        when(botApi.sendMessage(eq(123L), any())).thenReturn(response);

        assertDoesNotThrow(() -> messageService.sendMessage(123L, "Hello"));
    }

    @Test
    void sendMessage_Exception() {
        when(botApi.sendMessage(eq(123L), any())).thenThrow(new RuntimeException("Network error"));

        assertDoesNotThrow(() -> messageService.sendMessage(123L, "Hello"));
    }
}
