package ru.mitrohinayulya.zabotushka.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.greenway.*;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.GreenwayService;

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
     * Проверяет существование партнера по ID
     *
     * @param userId ID партнера
     * @return ID партнера если существует, иначе 404
     */
    @GET
    @Path("/check-user/{userId}")
    public Response checkUserId(@PathParam("userId") Long userId) {
        log.info("Checking user existence: userId={}", userId);

        try {
            var partnerListResponse = greenwayService.getPartnerList(userId, 0);
            var partnerOpt = greenwayService.findPartnerById(partnerListResponse, userId);

            return partnerOpt
                    .map(_ -> {
                        log.info("Partner found: userId={}", userId);
                        return Response.ok(CheckUserIdResponse.of(userId)).build();
                    })
                    .orElseGet(() -> {
                        log.warn("Partner not found: userId={}", userId);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity(CheckUserIdResponse.notFound())
                                .build();
                    });

        } catch (GreenwayApiException e) {
            log.error("Greenway API error during user check: userId={}", userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(CheckUserIdResponse.notFound())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during user check: userId={}", userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(CheckUserIdResponse.notFound())
                    .build();
        }
    }

    /**
     * Сравнивает ЛО партнера с заданным значением в текущем периоде
     *
     * @param userId ID партнера
     * @param lo     значение для сравнения
     * @return результат сравнения
     */
    @GET
    @Path("/compare-lo/{userId}/{lo}")
    public CompareLOResponse compareLO(@PathParam("userId") Long userId, @PathParam("lo") Double lo) {
        log.info("Comparing LO: userId={}, lo={}", userId, lo);
        return compareValue(userId, lo, 0, true);
    }

    /**
     * Сравнивает ЛО партнера с заданным значением в предыдущем периоде
     *
     * @param userId ID партнера
     * @param lo     значение для сравнения
     * @return результат сравнения
     */
    @GET
    @Path("/compare-lo/period/{userId}/{lo}")
    public CompareLOResponse compareLOPeriod(@PathParam("userId") Long userId, @PathParam("lo") Double lo) {
        var period = greenwayService.getPreviousPeriod();
        log.info("Comparing LO in previous period: userId={}, lo={}, period={}", userId, lo, period);
        return compareValue(userId, lo, period, true);
    }

    /**
     * Сравнивает СГО партнера с заданным значением в текущем периоде
     *
     * @param userId ID партнера
     * @param sgo    значение для сравнения
     * @return результат сравнения
     */
    @GET
    @Path("/compare-sgo/{userId}/{sgo}")
    public CompareSGOResponse compareSGO(@PathParam("userId") Long userId, @PathParam("sgo") Double sgo) {
        log.info("Comparing SGO: userId={}, sgo={}", userId, sgo);
        return compareValue(userId, sgo, 0, false);
    }

    /**
     * Сравнивает СГО партнера с заданным значением в предыдущем периоде
     *
     * @param userId ID партнера
     * @param sgo    значение для сравнения
     * @return результат сравнения
     */
    @GET
    @Path("/compare-sgo/period/{userId}/{sgo}")
    public CompareSGOResponse compareSGOPeriod(@PathParam("userId") Long userId, @PathParam("sgo") Double sgo) {
        var period = greenwayService.getPreviousPeriod();
        log.info("Comparing SGO in previous period: userId={}, sgo={}, period={}", userId, sgo, period);
        return compareValue(userId, sgo, period, false);
    }

    /**
     * Универсальный метод для сравнения значений (ЛО или СГО)
     * Использует pattern matching и sealed interfaces (Java 25)
     */
    @SuppressWarnings("unchecked")
    private <T> T compareValue(Long userId, Double value, int period, boolean isLO) {
        try {
            var partnerListResponse = greenwayService.getPartnerList(userId, period);
            var partnerOpt = greenwayService.findPartnerById(partnerListResponse, userId);

            return (T) partnerOpt
                    .map(partner -> {
                        var actualValue = isLO ? extractLO(partner) : extractSGO(partner);
                        var result = ComparisonResult.compare(actualValue, value);

                        log.info("Comparison result: userId={}, actual={}, expected={}, result={}, period={}",
                                userId, actualValue, value, result, period);

                        return isLO
                                ? CompareLOResponse.of(userId, actualValue, result, period)
                                : CompareSGOResponse.of(userId, actualValue, result, period);
                    })
                    .orElseGet(() -> {
                        log.warn("Partner not found: userId={}, period={}", userId, period);
                        return isLO
                                ? CompareLOResponse.notFound(period)
                                : CompareSGOResponse.notFound(period);
                    });

        } catch (GreenwayApiException e) {
            log.error("Greenway API error during comparison: userId={}, period={}", userId, period, e);
            return (T) (isLO ? CompareLOResponse.notFound(period) : CompareSGOResponse.notFound(period));
        } catch (Exception e) {
            log.error("Unexpected error during comparison: userId={}, period={}", userId, period, e);
            return (T) (isLO ? CompareLOResponse.notFound(period) : CompareSGOResponse.notFound(period));
        }
    }

    /**
     * Получает квалификацию партнера в текущем периоде
     *
     * @param userId ID партнера
     * @return квалификация партнера
     */
    @GET
    @Path("/qualification/{userId}")
    public QualificationResponse getQualification(@PathParam("userId") Long userId) {
        log.info("Getting qualification: userId={}", userId);
        return getQualificationInternal(userId, 0, false);
    }

    /**
     * Получает квалификацию партнера в предыдущем периоде
     *
     * @param userId ID партнера
     * @return квалификация партнера
     */
    @GET
    @Path("/qualification/period/{userId}")
    public QualificationResponse getQualificationPeriod(@PathParam("userId") Long userId) {
        var period = greenwayService.getPreviousPeriod();
        log.info("Getting qualification in previous period: userId={}, period={}", userId, period);
        return getQualificationInternal(userId, period, false);
    }

    /**
     * Получает точную квалификацию партнера (со ступенью) в текущем периоде
     *
     * @param userId ID партнера
     * @return точная квалификация партнера
     */
    @GET
    @Path("/qualification/exact/{userId}")
    public QualificationResponse getQualificationExact(@PathParam("userId") Long userId) {
        log.info("Getting exact qualification: userId={}", userId);
        return getQualificationInternal(userId, 0, true);
    }

    /**
     * Получает точную квалификацию партнера (со ступенью) в предыдущем периоде
     *
     * @param userId ID партнера
     * @return точная квалификация партнера
     */
    @GET
    @Path("/qualification/exact/period/{userId}")
    public QualificationResponse getQualificationExactPeriod(@PathParam("userId") Long userId) {
        var period = greenwayService.getPreviousPeriod();
        log.info("Getting exact qualification in previous period: userId={}, period={}", userId, period);
        return getQualificationInternal(userId, period, true);
    }

    /**
     * Получает лучшую квалификацию партнера из текущего и предыдущего периодов
     * Использует stream API и Optional (Java 25)
     *
     * @param userId ID партнера
     * @return лучшая квалификация
     */
    @GET
    @Path("/qualification/best/{userId}")
    public QualificationResponse getQualificationBest(@PathParam("userId") Long userId) {
        log.info("Getting best qualification: userId={}", userId);

        try {
            var previousPeriod = greenwayService.getPreviousPeriod();

            var currentPartnerList = greenwayService.getPartnerList(userId, 0);
            var previousPartnerList = greenwayService.getPartnerList(userId, previousPeriod);

            var currentQual = greenwayService.findPartnerById(currentPartnerList, userId)
                    .map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var previousQual = greenwayService.findPartnerById(previousPartnerList, userId)
                    .map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var bestQual = QualificationLevel.best(currentQual, previousQual);

            log.info("Best qualification result: userId={}, current={}, previous={}, best={}",
                    userId, currentQual, previousQual, bestQual);

            return QualificationResponse.of(bestQual);

        } catch (Exception e) {
            log.error("Error during qualification check: userId={}", userId, e);
            return QualificationResponse.of(QualificationLevel.NO);
        }
    }

    /**
     * Внутренний метод для получения квалификации
     * Использует switch expression (Java 25) и pattern matching
     */
    private QualificationResponse getQualificationInternal(Long userId, int period, boolean exact) {
        try {
            var partnerListResponse = greenwayService.getPartnerList(userId, period);

            return greenwayService.findPartnerById(partnerListResponse, userId)
                    .map(partner -> {
                        var qualification = exact
                                ? partner.qualification()
                                : QualificationLevel.fromString(partner.qualification()).getValue();

                        log.info("Qualification result: userId={}, qualification={}, period={}, exact={}",
                                userId, qualification, period, exact);

                        return QualificationResponse.of(qualification);
                    })
                    .orElseGet(() -> {
                        log.warn("Partner not found: userId={}, period={}", userId, period);
                        return QualificationResponse.of(QualificationLevel.NO);
                    });

        } catch (Exception e) {
            log.error("Error during qualification retrieval: userId={}, period={}", userId, period, e);
            return QualificationResponse.of(QualificationLevel.NO);
        }
    }

    /**
     * Извлекает ЛО из партнера с использованием Optional.ofNullable (Java 25)
     */
    private Double extractLO(Partner partner) {
        return Optional.ofNullable(partner.lo()).orElse(0.0);
    }

    /**
     * Извлекает СГО из партнера с использованием Optional.ofNullable (Java 25)
     */
    private Double extractSGO(Partner partner) {
        return Optional.ofNullable(partner.sgo()).orElse(0.0);
    }
}
