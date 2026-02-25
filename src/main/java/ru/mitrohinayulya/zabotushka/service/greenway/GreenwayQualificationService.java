package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

/// Common logic for determining the best user qualification.
@ApplicationScoped
public class GreenwayQualificationService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayQualificationService.class);

    @Inject
    GreenwayPartnerService greenwayPartnerService;

    public QualificationLevel getBestQualification(long greenwayId) {
        try {
            var previousPeriod = greenwayPartnerService.getPreviousPeriod();

            var currentPartnerList = greenwayPartnerService.getPartnerList(greenwayId, 0);
            var previousPartnerList = greenwayPartnerService.getPartnerList(greenwayId, previousPeriod);

            var currentQual = greenwayPartnerService.findPartnerById(currentPartnerList, greenwayId)
                    .map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var previousQual = greenwayPartnerService.findPartnerById(previousPartnerList, greenwayId)
                    .map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            return QualificationLevel.best(currentQual, previousQual);
        } catch (Exception e) {
            log.error("Error during qualification check: greenwayId={}", greenwayId, e);
            return QualificationLevel.NO;
        }
    }
}
