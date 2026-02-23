package ru.mitrohinayulya.zabotushka.qualification.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.qualification.domain.GreenwayPartnerPort;
import ru.mitrohinayulya.zabotushka.qualification.domain.QualificationPort;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayPartnerService;
import ru.mitrohinayulya.zabotushka.shared.domain.QualificationLevel;

import java.util.Optional;

/// Anti-corruption layer: adapts the Greenway infrastructure to the domain ports.
///
/// This class is the ONLY place that knows about {@code GreenwayPartnerService}
/// and translates between Greenway's data model and the shared-kernel types.
/// All domain code uses {@link QualificationPort} and {@link GreenwayPartnerPort} instead.
///
/// Note: the existing {@code GreenwayQualificationService} can be replaced by this adapter,
/// or this adapter can delegate to it during the transition period.
@ApplicationScoped
public class GreenwayQualificationAdapter implements QualificationPort, GreenwayPartnerPort {

    private static final Logger log = LoggerFactory.getLogger(GreenwayQualificationAdapter.class);

    @Inject
    GreenwayPartnerService partnerService;

    @Override
    public QualificationLevel getBestQualification(long greenwayId) {
        try {
            var previousPeriod = partnerService.getPreviousPeriod();
            var current = partnerService.getPartnerList(greenwayId, 0);
            var previous = partnerService.getPartnerList(greenwayId, previousPeriod);

            var currentLevel = partnerService.findPartnerById(current, greenwayId)
                    .map(p -> QualificationLevel.fromGreenwayString(p.qualification()))
                    .orElse(QualificationLevel.NO);
            var previousLevel = partnerService.findPartnerById(previous, greenwayId)
                    .map(p -> QualificationLevel.fromGreenwayString(p.qualification()))
                    .orElse(QualificationLevel.NO);

            return QualificationLevel.best(currentLevel, previousLevel);
        } catch (Exception e) {
            log.error("Greenway qualification check failed: greenwayId={}", greenwayId, e);
            return QualificationLevel.NO;
        }
    }

    @Override
    public Optional<String> findRegistrationDate(long greenwayId) {
        try {
            var partnerList = partnerService.getPartnerList(greenwayId, 0);
            if (partnerList == null || partnerList.partners() == null) return Optional.empty();
            return partnerService.findPartnerById(partnerList, greenwayId)
                    .map(p -> p.regDate());
        } catch (Exception e) {
            log.error("Greenway partner lookup failed: greenwayId={}", greenwayId, e);
            return Optional.empty();
        }
    }
}
