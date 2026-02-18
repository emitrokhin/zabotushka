package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeMaxRequest;
import ru.mitrohinayulya.zabotushka.service.AuthorizedMaxUserService;
import ru.mitrohinayulya.zabotushka.service.GreenwayAuthorizationService;

/**
 * REST ресурс для авторизации партнеров MyGreenway через Max
 */
@Path("/greenway/authorize/max")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class MaxAuthorizationResource {

    @Inject
    GreenwayAuthorizationService authorizationService;

    @Inject
    AuthorizedMaxUserService maxUserService;

    @POST
    public Response authorize(@Valid AuthorizeMaxRequest request) {
        var ops = new GreenwayAuthorizationService.PlatformUserOps() {
            @Override
            public boolean existsByPlatformId(Long platformId) {
                return maxUserService.existsByMaxId(platformId);
            }

            @Override
            public boolean matchesStoredData(Long platformId, Long greenwayId, String regDate) {
                return maxUserService.matchesStoredData(platformId, greenwayId, regDate);
            }

            @Override
            public void saveUser(Long platformId, Long greenwayId, String regDate) {
                maxUserService.saveAuthorizedUser(platformId, greenwayId, regDate);
            }

            @Override
            public boolean existsByGreenwayId(Long greenwayId) {
                return maxUserService.existsByGreenwayIdAcrossPlatforms(greenwayId);
            }
        };

        return authorizationService.authorize(ops, request.maxId(), request.greenwayId(), request.regDate(), "max");
    }
}
