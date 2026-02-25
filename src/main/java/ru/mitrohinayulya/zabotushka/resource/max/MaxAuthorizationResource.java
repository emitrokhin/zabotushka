package ru.mitrohinayulya.zabotushka.resource.max;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeMaxRequest;
import ru.mitrohinayulya.zabotushka.service.max.AuthorizedMaxUserService;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayAuthorizationService;

/// REST resource for authorizing MyGreenway partners via Max
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
