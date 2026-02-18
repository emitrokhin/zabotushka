package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeTelegramRequest;
import ru.mitrohinayulya.zabotushka.service.AuthorizedTelegramUserService;
import ru.mitrohinayulya.zabotushka.service.GreenwayAuthorizationService;

/**
 * REST ресурс для авторизации партнеров MyGreenway через Telegram
 */
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
        var ops = new GreenwayAuthorizationService.PlatformUserOps() {
            @Override
            public boolean existsByPlatformId(Long platformId) {
                return telegramUserService.existsByTelegramId(platformId);
            }

            @Override
            public boolean matchesStoredData(Long platformId, Long greenwayId, String regDate) {
                return telegramUserService.matchesStoredData(platformId, greenwayId, regDate);
            }

            @Override
            public void saveUser(Long platformId, Long greenwayId, String regDate) {
                telegramUserService.saveAuthorizedUser(platformId, greenwayId, regDate);
            }

            @Override
            public boolean existsByGreenwayId(Long greenwayId) {
                return telegramUserService.existsByGreenwayIdAcrossPlatforms(greenwayId);
            }
        };

        return authorizationService.authorize(ops, request.telegramId(), request.greenwayId(), request.regDate(), "telegram");
    }
}
