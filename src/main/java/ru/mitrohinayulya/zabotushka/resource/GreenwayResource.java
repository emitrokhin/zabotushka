package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeRequest;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.GreenwayService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST ресурс для работы с MyGreenway API
 */
@Path("/greenway")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class GreenwayResource {

    private static final Logger log = LoggerFactory.getLogger(GreenwayResource.class);

    @Inject
    GreenwayService greenwayService;

    /**
     * Метод для авторизации партнера MyGreenway
     * <p>
     * Проверяет существование партнера и совпадение даты регистрации.
     * Если партнер найден и дата регистрации совпадает, возвращает статус "authorized",
     * иначе "not_authorized".
     *
     * @param request Запрос с ID партнера и датой регистрации
     * @return Ответ со статусом авторизации
     */
    @POST
    @Path("/authorize")
    public Response authorize(@Valid AuthorizeRequest request) {
        log.info("Authorizing partner with greenwayId={}, regDate={}",
                request.greenwayId(), request.regDate());

        try {
            var partnerListResponse = greenwayService.getPartnerList(request.greenwayId(), 0);

            if (partnerListResponse == null
                    || partnerListResponse.partners() == null
                    || partnerListResponse.partners().isEmpty()) {
                log.warn("Partner list is empty for greenwayId={}", request.greenwayId());
                return buildNotAuthorizedResponse(Response.Status.NOT_FOUND);
            }

            // Ищем партнера и проверяем дату регистрации
            return findPartnerById(partnerListResponse.partners(), request.greenwayId())
                    .map(partner -> authorizePartner(partner, request))
                    .orElseGet(() -> {
                        log.warn("Partner not found with greenwayId={}", request.greenwayId());
                        return buildNotAuthorizedResponse(Response.Status.NOT_FOUND);
                    });

        } catch (GreenwayApiException e) {
            log.error("Greenway API error during authorization: greenwayId={}",
                    request.greenwayId(), e);
            return buildNotAuthorizedResponse(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error during authorization: greenwayId={}",
                    request.greenwayId(), e);
            return buildNotAuthorizedResponse(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Проверяет авторизацию партнера по дате регистрации
     */
    private Response authorizePartner(Partner partner, AuthorizeRequest request) {
        boolean isAuthorized = Objects.equals(partner.regDate(), request.regDate());

        if (isAuthorized) {
            log.info("Partner authorized successfully: greenwayId={}", request.greenwayId());
            return Response.ok(AuthorizeResponse.createAuthorized()).build();
        }

        log.warn("Partner authorization failed: greenwayId={}, expected regDate={}, actual regDate={}",
                request.greenwayId(), request.regDate(), partner.regDate());
        return buildNotAuthorizedResponse(Response.Status.UNAUTHORIZED);
    }

    /**
     * Поиск партнера по ID в списке партнеров
     */
    private Optional<Partner> findPartnerById(List<Partner> partners, Long greenwayId) {
        if (partners == null || partners.isEmpty() || greenwayId == null) {
            return Optional.empty();
        }

        return partners.stream()
                .filter(partner -> partner.number() != null)
                .filter(partner -> partner.number().equals(greenwayId.intValue()))
                .findFirst();
    }

    /**
     * Создает ответ с not_authorized и нужным статусом
     */
    private Response buildNotAuthorizedResponse(Response.Status status) {
        return Response.status(status).entity(AuthorizeResponse.createNotAuthorized()).build();
    }
}
