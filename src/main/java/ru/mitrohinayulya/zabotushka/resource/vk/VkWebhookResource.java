package ru.mitrohinayulya.zabotushka.resource.vk;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.vk.VkUpdate;
import ru.mitrohinayulya.zabotushka.service.vk.VkService;

/// REST resource for handling VK Callback API events.
///
/// Unlike Telegram/Max, VK passes the secret inside the JSON body (not in an HTTP header),
/// and expects a plain text `"ok"` response (not JSON).
@Path("/vk/webhook")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class VkWebhookResource {

    private static final Logger log = LoggerFactory.getLogger(VkWebhookResource.class);
    private static final String GROUP_JOIN_TYPE = "group_join";
    private static final String GROUP_LEAVE_TYPE = "group_leave";
    private static final String OK = "ok";

    @Inject
    @ConfigProperty(name = "app.vk.webhook.secret")
    String webhookSecret;

    @Inject
    VkService vkService;

    @POST
    public Response handleWebhook(VkUpdate update) {
        log.debug("Received VK event: type={}, groupId={}", update.type(), update.groupId());

        if (webhookSecret != null && !webhookSecret.equals(update.secret())) {
            log.warn("Invalid VK secret received for groupId={}", update.groupId());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        switch (update.type()) {
            case GROUP_JOIN_TYPE  -> vkService.processGroupJoin(update);
            case GROUP_LEAVE_TYPE -> vkService.processGroupLeave(update);
            default -> log.debug("Ignoring VK event type: {}", update.type());
        }

        return Response.ok(OK).build();
    }
}
