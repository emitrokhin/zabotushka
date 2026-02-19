package ru.mitrohinayulya.zabotushka.resource.webhook;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.max.MaxUpdate;
import ru.mitrohinayulya.zabotushka.service.MaxService;

/// REST ресурс для обработки Max webhook
@Path("/max/webhook")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaxWebhookResource {

    private static final Logger log = LoggerFactory.getLogger(MaxWebhookResource.class);
    private static final String MAX_SECRET_HEADER = "X-Max-Bot-Api-Secret-Token";
    private static final String USER_ADDED_UPDATE_TYPE = "user_added";

    @Inject
    @ConfigProperty(name = "app.max.webhook.secret")
    String webhookSecret;

    @Inject
    MaxService maxService;

    @POST
    public Response handleWebhook(
            @HeaderParam(MAX_SECRET_HEADER) String secretToken,
            MaxUpdate update) {
        log.info("Received webhook update: timestamp={}", update.timestamp());

        // Проверка секретного токена
        if (webhookSecret != null && !webhookSecret.equals(secretToken)) {
            log.warn("Invalid secret token received");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!USER_ADDED_UPDATE_TYPE.equals(update.updateType())) {
            log.debug("Update does not contain user_added, ignoring");
            return Response.ok().build();
        }

        log.info("Processing chat join request: chat={}, user={}, userId={}",
                update.chatId(),
                update.user().username(),
                update.user().userId());

        // Обработка запроса на вступление в чат
        maxService.processUserAddedUpdate(update);

        return Response.ok().build();
    }
}
