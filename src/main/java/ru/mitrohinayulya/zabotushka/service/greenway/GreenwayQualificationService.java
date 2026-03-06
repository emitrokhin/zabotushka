package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationResult;

/// Common logic for determining the best user qualification.
@ApplicationScoped
public class GreenwayQualificationService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayQualificationService.class);

    @Inject
    GreenwayPartnerService greenwayPartnerService;

    public QualificationLevel getBestQualification(long greenwayId) {
        return getBestQualificationResult(greenwayId).level();
    }

    public QualificationResult getBestQualificationResult(long greenwayId) {
        try {
            var previousPeriod = greenwayPartnerService.getPreviousPeriod();

            var currentPartnerList = greenwayPartnerService.getPartnerList(greenwayId, 0);
            var previousPartnerList = greenwayPartnerService.getPartnerList(greenwayId, previousPeriod);

            var currentPartner = greenwayPartnerService.findPartnerById(currentPartnerList, greenwayId);
            var previousPartner = greenwayPartnerService.findPartnerById(previousPartnerList, greenwayId);

            var currentQual = currentPartner.map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var previousQual = previousPartner.map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var bestLevel = QualificationLevel.best(currentQual, previousQual);
            var rawQual = currentQual.isStrictlyBetterThan(previousQual)
                    ? currentPartner.map(Partner::qualification).orElse(null)
                    : previousPartner.map(Partner::qualification).orElse(null);

            return new QualificationResult(bestLevel, rawQual);
        } catch (Exception e) {
            log.error("Error during qualification check: greenwayId={}", greenwayId, e);
            return new QualificationResult(QualificationLevel.NO, null);
        }
    }
}
