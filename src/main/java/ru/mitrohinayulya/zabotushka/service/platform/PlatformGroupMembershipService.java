package ru.mitrohinayulya.zabotushka.service.platform;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.mapper.UserGroupMembershipMapper;

import java.time.LocalDateTime;

/// Общая логика работы с членством пользователей в группах.
@ApplicationScoped
public class PlatformGroupMembershipService {

    private static final Logger log = LoggerFactory.getLogger(PlatformGroupMembershipService.class);

    @Inject
    UserGroupMembershipMapper membershipMapper;

    @Transactional
    public void saveMembership(long chatId, long userId, Platform platform) {
        try {
            if (!UserGroupMembership.exists(userId, chatId, platform)) {
                var membership = membershipMapper.toEntity(userId, chatId, platform, LocalDateTime.now());
                membership.persist();

                log.info("{} membership saved: chatId={}, userId={}", platform, chatId, userId);
            } else {
                log.debug("{} membership already exists: chatId={}, userId={}", platform, chatId, userId);
            }
        } catch (Exception e) {
            log.error("Error saving membership: platform={}, chatId={}, userId={}", platform, chatId, userId, e);
        }
    }

    @Transactional
    public boolean removeMembership(long chatId, long userId, Platform platform) {
        try {
            boolean removed = UserGroupMembership.removeMembership(userId, chatId, platform);
            if (removed) {
                log.info("Membership removed from DB: platform={}, chatId={}, userId={}", platform, chatId, userId);
            } else {
                log.warn("Membership not found in DB: platform={}, chatId={}, userId={}", platform, chatId, userId);
            }
            return removed;
        } catch (Exception e) {
            log.error("Error removing membership from DB: platform={}, chatId={}, userId={}", platform, chatId, userId, e);
            return false;
        }
    }
}
