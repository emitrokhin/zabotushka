package ru.mitrohinayulya.zabotushka.resource.telegram;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeTelegramRequest;
import ru.mitrohinayulya.zabotushka.service.telegram.AuthorizedTelegramUserService;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayAuthorizationService;

/// REST ресурс для авторизации партнеров MyGreenway через Telegram
@Path("/greenway/authorize/telegram")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class TelegramAuthorizationResource {

    @Inject
    GreenwayAuthorizationService authorizationService;

    @Inject
    AuthorizedTelegramUserService telegramUserService;

    @POST
    public Response authorize(@Valid AuthorizeTelegramRequest request) {
        return authorizationService.authorize(telegramUserService, request.telegramId(), request.greenwayId(), request.regDate(), "telegram");
    }
}
