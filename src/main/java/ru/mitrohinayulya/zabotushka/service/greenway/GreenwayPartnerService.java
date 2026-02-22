package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.service.PeriodCalculator;

import java.util.Optional;

/// Facade for the MyGreenway partner API. Handles fetching partner data with
/// token-refresh retry on HTTP 401.
@ApplicationScoped
public class GreenwayPartnerService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayPartnerService.class);

    @Inject
    @RestClient
    MyGreenwayApi apiClient;

    @Inject
    PeriodCalculator periodCalculator;

    @Inject
    GreenwayTokenStore tokenStore;

    @Inject
    GreenwaySessionManager sessionManager;

    public PartnerListResponse getPartnerList(long partnerId, int previousPeriod) {
        return getPartnerList(partnerId, previousPeriod, false);
    }

    private PartnerListResponse getPartnerList(long partnerId, int previousPeriod, boolean isRetry) {
        var currentAccessToken = tokenStore.getAccessToken();

        if (currentAccessToken == null) {
            throw new IllegalStateException("Access token is not available. Please login first.");
        }

        log.debug("Fetching partner list for partnerId={}, period={}, isRetry={}",
                partnerId, previousPeriod, isRetry);

        try {
            var response = apiClient.getPartnerList(
                    "Bearer " + currentAccessToken,
                    partnerId,
                    previousPeriod > 0 ? previousPeriod : null
            );

            if (response != null && response.code() != null) {
                log.error("Failed to get partner list for partnerId={}, period={}: code={}, detail={}",
                        partnerId, previousPeriod, response.code(), response.detail());
                throw new GreenwayApiException(
                        "Failed to get partner list for partnerId=%d, period=%d".formatted(partnerId, previousPeriod),
                        response.code(), response.detail());
            }

            return response;
        } catch (WebApplicationException e) {
            log.error("HTTP error fetching partner list for partnerId={}, period={}", partnerId, previousPeriod, e);

            if (e.getResponse().getStatus() != Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new GreenwayApiException(
                        "Failed to get partner list for partnerId=%d, period=%d".formatted(partnerId, previousPeriod), e);
            }

            if (isRetry) {
                throw new GreenwayApiException(
                        "Failed to get partner list for partnerId=%d, period=%d after token refresh retry"
                                .formatted(partnerId, previousPeriod), e);
            }

            log.info("Received 401, refreshing token and retrying for partnerId={}", partnerId);
            try {
                sessionManager.refreshToken();
                return getPartnerList(partnerId, previousPeriod, true);
            } catch (GreenwayApiException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new GreenwayApiException(
                        "Failed to get partner list for partnerId=%d, period=%d after token refresh retry"
                                .formatted(partnerId, previousPeriod), ex);
            }
        }
    }

    public int getPreviousPeriod() {
        return periodCalculator.calculatePreviousPeriod();
    }

    public Optional<Partner> findPartnerById(PartnerListResponse response, long partnerId) {
        if (response == null || response.partners() == null || response.partners().isEmpty()) {
            return Optional.empty();
        }

        return response.partners().stream()
                .filter(partner -> partner.number() != null)
                .filter(partner -> partner.number().longValue() == partnerId)
                .findFirst();
    }
}
