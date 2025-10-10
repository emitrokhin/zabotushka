package ru.mitrohinayulya;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/telegram-webhook")
public class TelegramWebhookResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public TelegramUpdate getUpdate() {
        return "Hello from Quarkus REST";
    }
}
