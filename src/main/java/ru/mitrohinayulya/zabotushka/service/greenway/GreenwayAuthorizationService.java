package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.greenway.AuthorizeResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.ErrorResponse;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.platform.PlatformAuthorizationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Общий сервис авторизации через Greenway API.
 * Содержит shared-логику проверки партнера, вынесенную из GreenwayResource.
 */
@ApplicationScoped
public class GreenwayAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayAuthorizationService.class);

    @Inject
    GreenwayService greenwayService;

    /**
     * Выполняет авторизацию пользователя через Greenway API
     *
     * @param ops        платформо-зависимые операции
     * @param platformId ID пользователя на платформе
     * @param greenwayId ID партнера в Greenway
     * @param regDate    дата регистрации
     * @param platformName название платформы для логирования
     * @return HTTP ответ
     */
    public Response authorize(PlatformAuthorizationService ops, Long platformId, Long greenwayId, String regDate, String platformName) {
        log.info("Authorizing partner with {}Id={}, greenwayId={}, regDate={}",
                platformName, platformId, greenwayId, regDate);

        // Проверяем существование пользователя по platformId
        if (ops.existsByPlatformId(platformId)) {
            if (ops.matchesStoredData(platformId, greenwayId, regDate)) {
                log.info("Re-authorization successful for {}Id={}", platformName, platformId);
                return Response.ok(AuthorizeResponse.createAuthorized()).build();
            } else {
                log.warn("Authorization rejected: data mismatch for {}Id={}", platformName, platformId);
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.of("Authorization data does not match stored credentials"))
                        .build();
            }
        }

        // Проверяем, не используется ли этот greenwayId другим пользователем
        if (ops.existsByGreenwayId(greenwayId)) {
            log.warn("Authorization rejected: greenwayId={} is already associated with another account",
                    greenwayId);
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.of("This Greenway ID is already associated with another account"))
                    .build();
        }

        // Выполняем авторизацию через Greenway API
        try {
            var partnerListResponse = greenwayService.getPartnerList(greenwayId, 0);

            if (partnerListResponse == null
                    || partnerListResponse.partners() == null
                    || partnerListResponse.partners().isEmpty()) {
                log.warn("Partner list is empty for greenwayId={}", greenwayId);
                return buildNotAuthorizedResponse(Response.Status.NOT_FOUND);
            }

            return findPartnerById(partnerListResponse.partners(), greenwayId)
                    .map(partner -> authorizePartner(partner, ops, platformId, greenwayId, regDate, platformName))
                    .orElseGet(() -> {
                        log.warn("Partner not found with greenwayId={}", greenwayId);
                        return buildNotAuthorizedResponse(Response.Status.NOT_FOUND);
                    });

        } catch (GreenwayApiException e) {
            log.error("Greenway API error during authorization: greenwayId={}",
                    greenwayId, e);
            return buildNotAuthorizedResponse(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error during authorization: greenwayId={}",
                    greenwayId, e);
            return buildNotAuthorizedResponse(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Response authorizePartner(Partner partner, PlatformAuthorizationService ops,
                                      Long platformId, Long greenwayId, String regDate, String platformName) {
        boolean isAuthorized = compareDates(partner.regDate(), regDate);

        if (isAuthorized) {
            ops.saveUser(platformId, greenwayId, regDate);
            log.info("Partner authorized successfully and saved to DB: {}Id={}, greenwayId={}",
                    platformName, platformId, greenwayId);
            return Response.ok(AuthorizeResponse.createAuthorized()).build();
        }

        log.warn("Partner authorization failed: greenwayId={}, expected regDate={}, actual regDate={}",
                greenwayId, regDate, partner.regDate());
        return buildNotAuthorizedResponse(Response.Status.UNAUTHORIZED);
    }

    private boolean compareDates(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        if (date1.equals(date2)) {
            return true;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate localDate1 = LocalDate.parse(date1, formatter);
            LocalDate localDate2 = LocalDate.parse(date2, formatter);
            return localDate1.equals(localDate2);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse dates for comparison: date1={}, date2={}", date1, date2, e);
            return false;
        }
    }

    private Optional<Partner> findPartnerById(List<Partner> partners, Long greenwayId) {
        if (partners == null || partners.isEmpty() || greenwayId == null) {
            return Optional.empty();
        }

        return partners.stream()
                .filter(partner -> partner.number() != null)
                .filter(partner -> partner.number().longValue() == greenwayId)
                .findFirst();
    }

    private Response buildNotAuthorizedResponse(Response.Status status) {
        return Response.status(status).entity(AuthorizeResponse.createNotAuthorized()).build();
    }
}
