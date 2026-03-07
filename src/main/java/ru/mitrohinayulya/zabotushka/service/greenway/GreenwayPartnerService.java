package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.client.MyGreenwayPartnerApi;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.PartnerListResponse;
import ru.mitrohinayulya.zabotushka.exception.GreenwayApiException;
import ru.mitrohinayulya.zabotushka.interceptor.RefreshTokenOnExpiry;
import ru.mitrohinayulya.zabotushka.service.PeriodCalculator;

import java.util.Optional;

/// Facade for the MyGreenway partner API.
/// Token injection is handled by GreenwayTokenRequestFilter.
/// Retry on 401 with token refresh is handled by @RetryOnUnauthorized interceptor.
@ApplicationScoped
public class GreenwayPartnerService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayPartnerService.class);

    @Inject
    @RestClient
    MyGreenwayPartnerApi apiClient;

    @Inject
    PeriodCalculator periodCalculator;

    @RefreshTokenOnExpiry
    public PartnerListResponse getPartnerList(long partnerId, int previousPeriod) {
        log.debug("Fetching partner list for partnerId={}, period={}", partnerId, previousPeriod);

        var response = apiClient.getPartnerList(
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
    }

    public int getPreviousPeriod() {
        return periodCalculator.calculatePreviousPeriod();
    }

    public Optional<Partner> findCurrentPartner(long partnerId) {
        return findPartnerById(getPartnerList(partnerId, 0), partnerId);
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
