package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GroupQualificationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(GroupQualificationOrchestrator.class);

    @Inject
    @Any
    Instance<PlatformQualificationProcessor> processors;

    public QualificationProcessStats runMonthlyCheck() {
        var totalStats = QualificationProcessStats.empty();

        for (var processor : processors) {
            var platform = processor.platform();
            try {
                var processorStats = processor.processQualifications();
                totalStats = totalStats.merge(processorStats);
                log.info("Platform qualification check completed: platform={}, checked={}, removed={}, orphanedRemoved={}, errors={}",
                        platform, processorStats.checked(), processorStats.removed(),
                        processorStats.orphanedRemoved(), processorStats.errors());
            } catch (Exception e) {
                totalStats = totalStats.merge(new QualificationProcessStats(0, 0, 0, 1));
                log.error("Platform qualification processor failed: platform={}", platform, e);
            }
        }

        return totalStats;
    }
}
