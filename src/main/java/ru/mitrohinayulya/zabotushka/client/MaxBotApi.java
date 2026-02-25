package ru.mitrohinayulya.zabotushka.client;

import io.quarkiverse.bucket4j.runtime.RateLimited;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mitrohinayulya.zabotushka.dto.max.MaxDeleteChatMemberResponse;
import ru.mitrohinayulya.zabotushka.dto.max.MaxDeleteSubscriptionResponse;
import ru.mitrohinayulya.zabotushka.dto.max.MaxGetChatMemberResponse;
import ru.mitrohinayulya.zabotushka.dto.max.MaxSendMessageRequest;
import ru.mitrohinayulya.zabotushka.dto.max.MaxSetSubscriptionRequest;

import java.util.List;

/// REST client for *Max Bot API*
/// Handles sending messages to users
@RegisterRestClient(configKey = "max-bot-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RateLimited(bucket = "telegram") // shared bucket with Telegram
public interface MaxBotApi {

    /// Subscribes the bot to receive updates via WebHook.
    /// After calling this method, the bot will receive notifications about new events in chats at the specified URL.
    @POST
    @Path("/subscriptions")
    Response setSubscription(MaxSetSubscriptionRequest request);

    /// Subscribes the bot to receive updates via WebHook.
    /// After calling this method, the bot will receive notifications about new events in chats at the specified URL.
    @POST
    @Path("/subscriptions")
    MaxDeleteSubscriptionResponse deleteSubscription(@QueryParam("url") String url);

    /// Sends a message to a user
    @POST
    @Path("/messages")
    Response sendMessage(@QueryParam("user_id") long userId, MaxSendMessageRequest request);

    /// Returns the list of chat members
    @GET
    @Path("/chats/{chatId}/members")
    MaxGetChatMemberResponse getChatMembers(@PathParam("chatId") long chatId, @QueryParam("user_ids") List<Long> userIds);

    /// Removes a member from a group chat.
    @DELETE
    @Path("/chats/{chatId}/members")
    MaxDeleteChatMemberResponse deleteChatMember(@PathParam("chatId") long chatId, @QueryParam("user_id") long userId);
}
