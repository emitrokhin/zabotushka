package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.scheduler.GroupQualificationScheduler;

/// REST resource for managing group qualification checks
@Path("/group-qualification")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class GroupQualificationResource {

    private static final Logger log = LoggerFactory.getLogger(GroupQualificationResource.class);

    @Inject
    GroupQualificationScheduler groupQualificationScheduler;

    /// Triggers a qualification check for all users in all groups
    /// @return execution result
    @POST
    @Path("/check")
    public Response checkGroupQualifications() {
        log.info("Manual group qualification check triggered via API");

        try {
            groupQualificationScheduler.checkGroupQualifications();
            log.info("Manual group qualification check completed successfully");
            return Response.ok()
                    .entity(new CheckResponse("success", "Group qualification check completed"))
                    .build();
        } catch (Exception e) {
            log.error("Error during manual group qualification check", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new CheckResponse("error", "Failed to complete group qualification check: " + e.getMessage()))
                    .build();
        }
    }

    /// Response DTO
    public record CheckResponse(String status, String message) {}
}
