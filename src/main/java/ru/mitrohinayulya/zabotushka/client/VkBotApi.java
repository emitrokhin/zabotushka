package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mitrohinayulya.zabotushka.client.filter.VkTokenRequestFilter;
import ru.mitrohinayulya.zabotushka.dto.vk.VkAddCallbackServerResponse;
import ru.mitrohinayulya.zabotushka.dto.vk.VkApiResponse;
import ru.mitrohinayulya.zabotushka.dto.vk.VkConversationMembersResponse;

/// REST client for VK API.
/// Token and API version are injected automatically by VkTokenRequestFilter
/// @see VkTokenRequestFilter
@RegisterRestClient(configKey = "vk-bot-api")
@RegisterProvider(VkTokenRequestFilter.class)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface VkBotApi {

    /// Adds a callback server to the VK community.
    /// Returns the server ID assigned by VK, used later for settings and deletion.
    @POST
    @Path("/groups.addCallbackServer")
    VkApiResponse<VkAddCallbackServerResponse> addCallbackServer(
            @QueryParam("group_id") long groupId,
            @QueryParam("url") String url,
            @QueryParam("title") String title,
            @QueryParam("secret_key") String secretKey
    );

    /// Removes a callback server from the VK community.
    @POST
    @Path("/groups.deleteCallbackServer")
    VkApiResponse<Integer> deleteCallbackServer(
            @QueryParam("group_id") long groupId,
            @QueryParam("server_id") int serverId
    );

    /// Subscribes the callback server to specific event types.
    @POST
    @Path("/groups.setCallbackSettings")
    VkApiResponse<Integer> setCallbackSettings(
            @QueryParam("group_id") long groupId,
            @QueryParam("server_id") int serverId,
            @QueryParam("group_join") int groupJoin,
            @QueryParam("group_leave") int groupLeave
    );

    /// Sends a private message to a VK user.
    /// `randomId` must be unique per message to prevent duplicates.
    @POST
    @Path("/messages.send")
    VkApiResponse<Long> sendMessage(
            @QueryParam("user_id") long userId,
            @QueryParam("message") String message,
            @QueryParam("random_id") int randomId
    );

    /// Returns members of a VK conversation.
    /// `peerId` for group chats is `2_000_000_000 + local chat id`.
    /// `extended` is always 0 — only `items` with `member_id` are needed.
    @POST
    @Path("/messages.getConversationMembers")
    VkApiResponse<VkConversationMembersResponse> getConversationMembers(
            @QueryParam("peer_id") long peerId,
            @QueryParam("extended") int extended
    );

    /// Removes a user from a multi-user conversation.
    /// `chatId` is the local conversation identifier
    /// **But be careful, you need to substract 2_000_000_000**
    ///
    /// @return 1 on success
    /// @see Integer
    @POST
    @Path("/messages.removeChatUser")
    VkApiResponse<Integer> removeChatUser(
            @QueryParam("chat_id") long chatId,
            @QueryParam("user_id") long userId
    );
}
