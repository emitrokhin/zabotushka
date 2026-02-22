package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mitrohinayulya.zabotushka.dto.greenway.RefreshTokenResponse;

@RegisterRestClient(configKey = "my-greenway-api")
@Path("/auth")
public interface MyGreenwayAuthApi {

    @POST
    @Path("/session/")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    RefreshTokenResponse createSession(Form form);

    @POST
    @Path("/refresh/")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    RefreshTokenResponse refreshToken(Form form);
}
