package ru.mitrohinayulya.zabotushka.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ru.mitrohinayulya.zabotushka.client.filter.GreenwayTokenRequestFilter;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;

@RegisterRestClient(configKey = "my-greenway-api")
@RegisterProvider(GreenwayTokenRequestFilter.class)
@Path("/")
public interface MyGreenwayPartnerApi {

    @GET
    @Path("/greenway/office/partner/list/")
    @Produces(MediaType.APPLICATION_JSON)
    PartnerListResponse getPartnerList(
            @QueryParam("name_or_id") long partnerId,
            @QueryParam("period") Integer previousPeriod
    );
}
