package ru.mitrohinayulya.zabotushka.resource.max;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeMaxRequest;
import ru.mitrohinayulya.zabotushka.service.max.AuthorizedMaxUserService;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayAuthorizationService;

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
        return authorizationService.authorize(maxUserService, request.maxId(), request.greenwayId(), request.regDate(), "max");
    }
}
