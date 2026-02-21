package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;

import java.time.LocalDateTime;
import java.util.List;

/// Шаблонный обработчик ежемесячной проверки квалификации для конкретной платформы.
public abstract class AbstractPlatformQualificationProcessor<U> implements PlatformQualificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(AbstractPlatformQualificationProcessor.class);

    protected abstract List<Long> chatIds();

    protected abstract U findAuthorizedUser(long platformUserId);

    protected abstract long getPlatformUserId(U user);

    protected abstract long getGreenwayId(U user);

    protected abstract void checkAndRemoveIfNotQualified(long chatId, long userId, long greenwayId);

    protected abstract void removeMemberFromChat(long chatId, long userId);

    @Override
    public QualificationProcessStats processQualifications() {
        int checked = 0;
        int removed = 0;
        int orphanedRemoved = 0;
        int errors = 0;

        for (var chatId : chatIds()) {
            var memberships = UserGroupMembership.findByChatIdAndPlatform(chatId, platform());
            log.info("Checking group qualification: platform={}, chatId={}, members={}",
                    platform(), chatId, memberships.size());

            for (var membership : memberships) {
                checked++;
                var platformUserId = membership.platformUserId;

                try {
                    var user = findAuthorizedUser(platformUserId);
                    if (user == null) {
                        log.warn("User not found in authorized users, removing orphaned membership: platform={}, chatId={}, userId={}",
                                platform(), chatId, platformUserId);
                        handleOrphanedMembership(membership, chatId, platformUserId);
                        removed++;
                        orphanedRemoved++;
                        continue;
                    }

                    var userId = getPlatformUserId(user);
                    checkAndRemoveIfNotQualified(chatId, userId, getGreenwayId(user));

                    if (UserGroupMembership.exists(userId, chatId, platform())) {
                        membership.lastCheckedAt = LocalDateTime.now();
                        membership.persist();
                    } else {
                        removed++;
                    }
                } catch (Exception e) {
                    errors++;
                    log.error("Error checking user qualification: platform={}, chatId={}, userId={}",
                            platform(), chatId, platformUserId, e);
                }
            }
        }

        return new QualificationProcessStats(checked, removed, orphanedRemoved, errors);
    }

    private void handleOrphanedMembership(UserGroupMembership membership,
                                          long chatId,
                                          long platformUserId) {
        try {
            removeMemberFromChat(chatId, platformUserId);
        } catch (Exception e) {
            log.error("Failed to remove orphaned member from chat: platform={}, chatId={}, userId={}",
                    platform(), chatId, platformUserId, e);
        }

        membership.delete();
    }
}
