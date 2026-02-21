package ru.mitrohinayulya.zabotushka.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.scheduler.qualification.GroupQualificationOrchestrator;

/// Планировщик для проверки квалификации пользователей в группах
/// Запускается каждый месяц 8 числа в 00:00
@ApplicationScoped
public class GroupQualificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(GroupQualificationScheduler.class);

    @Inject
    GroupQualificationOrchestrator orchestrator;

    /// Проверяет квалификацию пользователей в группах
    /// Запускается каждый месяц 8 числа в 00:00
    @Scheduled(cron = "0 0 0 8 * ?")
    @Transactional
    public void checkGroupQualifications() {
        log.info("Starting monthly group qualification check");

        try {
            var stats = orchestrator.runMonthlyCheck();

            log.info("Monthly group qualification check completed: totalChecked={}, totalRemoved={}, orphanedRemoved={}, totalErrors={}",
                    stats.checked(), stats.removed(), stats.orphanedRemoved(), stats.errors());
        } catch (Exception e) {
            log.error("Error during monthly group qualification check", e);
        }
    }
}
