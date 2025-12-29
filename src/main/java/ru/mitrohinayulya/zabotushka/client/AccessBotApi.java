package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mitrohinayulya.zabotushka.dto.telegram.*;

/**
 * REST client for Telegram Bot API - Access Bot
 * Handles webhook registration and access control operations
 */
@RegisterRestClient(configKey = "access-bot-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AccessBotApi {

    @POST
    @Path("/setWebhook")
    TelegramResponse<Boolean> setWebhook(SetWebhookRequest request);

    @POST
    @Path("/approveChatJoinRequest")
    TelegramResponse<Boolean> approveChatJoinRequest(ApproveChatJoinRequest request);

    @POST
    @Path("/declineChatJoinRequest")
    TelegramResponse<Boolean> declineChatJoinRequest(DeclineChatJoinRequest request);

    @POST
    @Path("/getChatMember")
    TelegramResponse<ChatMember> getChatMember(GetChatMemberRequest request);

    @POST
    @Path("/unbanChatMember")
    TelegramResponse<Boolean> unbanChatMember(UnbanChatMemberRequest request);
}
