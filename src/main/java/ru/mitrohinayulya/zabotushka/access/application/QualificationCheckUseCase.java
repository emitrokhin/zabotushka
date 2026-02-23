package ru.mitrohinayulya.zabotushka.access.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.access.domain.PlatformAccessPort;
import ru.mitrohinayulya.zabotushka.authorization.domain.AuthorizedUserRepository;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupMembershipRepository;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupRequirementsRegistry;
import ru.mitrohinayulya.zabotushka.qualification.domain.QualificationPort;
import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

/// Application service: runs the monthly qualification recheck for all managed groups
/// across all platforms.
///
/// <h2>What this replaces</h2>
/// {@code AbstractPlatformQualificationProcessor<U>} and its subclasses
/// ({@code TelegramQualificationProcessor}, {@code MaxQualificationProcessor}).
/// The abstract processor uses a type parameter {@code <U>} (the platform user entity)
/// to bridge domain logic and entity-level data access. This forces the processor to
/// know about {@code AuthorizedTelegramUser}, {@code AuthorizedMaxUser}, and
/// {@code UserGroupMembership} entity classes simultaneously.
///
/// Here, all data access goes through ports. The single use case handles all platforms
/// via CDI {@code Instance<PlatformAccessPort>}, just as the orchestrator currently
/// collects {@code PlatformQualificationProcessor} beans.
///
/// <h2>Stats</h2>
/// Returns {@link QualificationCheckStats} instead of the scheduler-specific
/// {@code QualificationProcessStats}, keeping the access context independent of
/// the scheduler infrastructure.
@ApplicationScoped
public class QualificationCheckUseCase {

    private static final Logger log = LoggerFactory.getLogger(QualificationCheckUseCase.class);

    @Inject
    @Any
    Instance<PlatformAccessPort> accessPorts;

    @Inject
    AuthorizedUserRepository authorizedUsers;

    @Inject
    GroupMembershipRepository membershipRepository;

    @Inject
    GroupRequirementsRegistry requirementsRegistry;

    @Inject
    QualificationPort qualificationPort;

    public record QualificationCheckStats(int checked, int removed, int orphaned, int errors) {
        public static QualificationCheckStats empty() { return new QualificationCheckStats(0, 0, 0, 0); }

        public QualificationCheckStats merge(QualificationCheckStats other) {
            return new QualificationCheckStats(
                    checked + other.checked, removed + other.removed,
                    orphaned + other.orphaned, errors + other.errors);
        }
    }

    public QualificationCheckStats runForAllPlatforms() {
        var total = QualificationCheckStats.empty();
        for (var port : accessPorts) {
            var stats = runForPlatform(port);
            total = total.merge(stats);
        }
        return total;
    }

    private QualificationCheckStats runForPlatform(PlatformAccessPort port) {
        var platform = port.platform();
        int checked = 0, removed = 0, orphaned = 0, errors = 0;

        for (var chatId : requirementsRegistry.chatIdsForPlatform(platform)) {
            var memberships = membershipRepository.findByChatIdAndPlatform(chatId, platform);
            log.info("Monthly qualification check: platform={}, chatId={}, members={}",
                    platform, chatId, memberships.size());

            for (var membership : memberships) {
                checked++;
                var userId = membership.platformUserId();
                try {
                    var user = authorizedUsers.findByPlatformUserId(userId, platform);
                    if (user.isEmpty()) {
                        log.warn("Orphaned membership (no authorized user record): platform={}, chatId={}, userId={}",
                                platform, chatId, userId);
                        safeRemove(port, chatId, userId, platform);
                        removed++;
                        orphaned++;
                        continue;
                    }

                    if (!port.isMember(chatId, userId)) {
                        log.info("User left voluntarily: platform={}, chatId={}, userId={}", platform, chatId, userId);
                        membershipRepository.remove(userId, chatId, platform);
                        removed++;
                        continue;
                    }

                    var level = qualificationPort.getBestQualification(user.get().greenwayId());
                    var requirements = requirementsRegistry.findByChatId(chatId, platform);

                    if (requirements.isPresent() && !requirements.get().isQualificationAllowed(level)) {
                        log.info("Removing unqualified member: platform={}, chatId={}, userId={}, level={}",
                                platform, chatId, userId, level);
                        var groupName = requirementsRegistry.resolveGroupName(chatId, platform);
                        port.removeMember(chatId, userId);
                        port.notifyRemoved(userId, groupName);
                        membershipRepository.remove(userId, chatId, platform);
                        removed++;
                    } else {
                        membership.recordCheck();
                        membershipRepository.save(membership);
                    }
                } catch (Exception e) {
                    errors++;
                    log.error("Error during qualification check: platform={}, chatId={}, userId={}",
                            platform, chatId, userId, e);
                }
            }
        }
        return new QualificationCheckStats(checked, removed, orphaned, errors);
    }

    private void safeRemove(PlatformAccessPort port, long chatId, long userId, Platform platform) {
        try {
            port.removeMember(chatId, userId);
        } catch (Exception e) {
            log.error("Failed to remove orphaned member from platform: platform={}, chatId={}, userId={}",
                    platform, chatId, userId, e);
        }
        membershipRepository.remove(userId, chatId, platform);
    }
}
