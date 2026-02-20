package ru.mitrohinayulya.zabotushka.resource.telegram;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.telegram.Update;
import ru.mitrohinayulya.zabotushka.service.telegram.TelegramService;

/**
 * REST ресурс для обработки Telegram webhook
 */
@Path("/telegram/webhook")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TelegramWebhookResource {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookResource.class);
    private static final String TELEGRAM_SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    @Inject
    @ConfigProperty(name = "app.telegram.webhook.secret")
    String webhookSecret;

    @Inject
    TelegramService telegramService;

    @POST
    public Response handleWebhook(
            @HeaderParam(TELEGRAM_SECRET_HEADER) String secretToken,
            Update update) {
        log.info("Received webhook update: updateId={}", update.updateId());

        // Проверка секретного токена
        if (webhookSecret != null && !webhookSecret.equals(secretToken)) {
            log.warn("Invalid secret token received");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (update.chatJoinRequest() == null) {
            log.debug("Update does not contain chat_join_request, ignoring");
            return Response.ok().build();
        }

        var chatJoinRequest = update.chatJoinRequest();
        log.info("Processing chat join request: chat={}, user={}, userId={}, bio={}",
                chatJoinRequest.chat().title(),
                chatJoinRequest.from().username(),
                chatJoinRequest.from().id(),
                chatJoinRequest.bio());

        // Обработка запроса на вступление в чат
        telegramService.processChatJoinRequest(chatJoinRequest);

        return Response.ok().build();
    }
}
