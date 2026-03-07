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
            var partner = greenwayPartnerService.findCurrentPartner(greenwayId);

            var currentQual = partner.map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var previousQual = partner.map(Partner::prevQualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var bestLevel = QualificationLevel.best(currentQual, previousQual);
            var rawQual = currentQual.isStrictlyBetterThan(previousQual)
                    ? partner.map(Partner::qualification).orElse(null)
                    : partner.map(Partner::prevQualification).orElse(null);

            return new QualificationResult(bestLevel, rawQual);
        } catch (Exception e) {
            log.error("Error during qualification check: greenwayId={}", greenwayId, e);
            return new QualificationResult(QualificationLevel.NO, null);
        }
    }
}
