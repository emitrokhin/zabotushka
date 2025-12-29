package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mitrohinayulya.zabotushka.dto.telegram.SendMessageRequest;
import ru.mitrohinayulya.zabotushka.dto.telegram.TelegramResponse;

/**
 * REST client for Telegram Bot API - Message Bot
 * Handles sending messages to users
 */
@RegisterRestClient(configKey = "message-bot-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MessageBotApi {

    @POST
    @Path("/sendMessage")
    TelegramResponse<Object> sendMessage(SendMessageRequest request);
}
