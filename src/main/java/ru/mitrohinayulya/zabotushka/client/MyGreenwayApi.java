package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.RefreshTokenResponse;

@RegisterRestClient
@Path("/")
public interface MyGreenwayApi {

    @POST
    @Path("/auth/session/")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    RefreshTokenResponse createSession(Form form);

    @POST
    @Path("/auth/refresh/")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    RefreshTokenResponse refreshToken(Form form);

    @GET
    @Path("/greenway/office/partner/list/")
    @Produces(MediaType.APPLICATION_JSON)
    PartnerListResponse getPartnerList(
            @HeaderParam("Authorization") String authorization,
            @QueryParam("name_or_id") long partnerId,
            @QueryParam("period") Integer previousPeriod
    );
}
