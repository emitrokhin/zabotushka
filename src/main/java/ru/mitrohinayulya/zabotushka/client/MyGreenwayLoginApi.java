package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

@RegisterRestClient
@ClientHeaderParam(name = "User-Agent", value = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
@ClientHeaderParam(name = "Accept", value = "application/json, text/javascript, */*; q=0.01")
@ClientHeaderParam(name = "Accept-Language", value = "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
@ClientHeaderParam(name = "X-Requested-With", value = "XMLHttpRequest")
@ClientHeaderParam(name = "Origin", value = "https://greenwaystart.com")
@ClientHeaderParam(name = "Referer", value = "https://greenwaystart.com/rus/")
@Path("/s/l/")
public interface MyGreenwayLoginApi {

    @POST
    @Path("/") // Important: trailing slash must be preserved, otherwise the server responds with 301 and does not return cookies
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response login(Form form);
}
