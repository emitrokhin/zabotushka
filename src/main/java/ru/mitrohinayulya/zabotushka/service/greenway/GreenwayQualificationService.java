package ru.mitrohinayulya.zabotushka.service.greenway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.dto.greenway.Partner;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

/**
 * Общая логика определения лучшей квалификации пользователя.
 */
@ApplicationScoped
public class GreenwayQualificationService {

    private static final Logger log = LoggerFactory.getLogger(GreenwayQualificationService.class);

    @Inject
    GreenwayService greenwayService;

    public QualificationLevel getBestQualification(long greenwayId) {
        try {
            var previousPeriod = greenwayService.getPreviousPeriod();

            var currentPartnerList = greenwayService.getPartnerList(greenwayId, 0);
            var previousPartnerList = greenwayService.getPartnerList(greenwayId, previousPeriod);

            var currentQual = greenwayService.findPartnerById(currentPartnerList, greenwayId)
                    .map(Partner::qualification)
                    .map(QualificationLevel::fromString)
                    .orElse(QualificationLevel.NO);

            var previousQual = greenwayService.findPartnerById(previousPartnerList, greenwayId)
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
