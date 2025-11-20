package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.RefreshTokenResponse;

@RegisterRestClient
@ClientHeaderParam(name = "User-Agent", value = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
@ClientHeaderParam(name = "Accept", value = "application/json, text/plain, */*")
@ClientHeaderParam(name = "sec-ch-ua", value = "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"")
@ClientHeaderParam(name = "sec-ch-ua-mobile", value = "?0")
@ClientHeaderParam(name = "sec-ch-ua-platform", value = "\"Windows\"")
@ClientHeaderParam(name = "Origin", value = "https://greenwaystart.com")
@ClientHeaderParam(name = "Referer", value = "https://greenwaystart.com/")
@ClientHeaderParam(name = "Content-Language", value = "ru-RU")
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
