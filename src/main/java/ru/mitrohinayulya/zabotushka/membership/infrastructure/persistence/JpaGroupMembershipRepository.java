package ru.mitrohinayulya.zabotushka.membership.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.entity.UserGroupMembership;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupMembership;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupMembershipRepository;

import java.util.List;
import java.util.Optional;

/// Infrastructure implementation of {@link GroupMembershipRepository}.
///
/// Wraps the existing {@code UserGroupMembership} Panache entity, which remains
/// unchanged during migration. This is a pure adapter — no business logic.
///
/// The translation between shared-kernel {@code Platform} (in {@code shared.domain})
/// and the entity-level {@code Platform} enum (in {@code entity}) is handled here;
/// once the entity package is cleaned up, both enums can be unified.
@ApplicationScoped
public class JpaGroupMembershipRepository implements GroupMembershipRepository {

    @Override
    public boolean exists(long platformUserId, long chatId, ru.mitrohinayulya.zabotushka.shared.domain.Platform platform) {
        return UserGroupMembership.exists(platformUserId, chatId, toEntityPlatform(platform));
    }

    @Override
    public Optional<GroupMembership> find(long platformUserId, long chatId, ru.mitrohinayulya.zabotushka.shared.domain.Platform platform) {
        return UserGroupMembership.findByChatIdAndPlatform(chatId, toEntityPlatform(platform))
                .stream()
                .filter(m -> m.platformUserId == platformUserId)
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<GroupMembership> findByChatIdAndPlatform(long chatId, ru.mitrohinayulya.zabotushka.shared.domain.Platform platform) {
        return UserGroupMembership.findByChatIdAndPlatform(chatId, toEntityPlatform(platform))
                .stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void save(GroupMembership membership) {
        if (!UserGroupMembership.exists(membership.platformUserId(), membership.chatId(), toEntityPlatform(membership.platform()))) {
            var entity = new UserGroupMembership();
            entity.platformUserId = membership.platformUserId();
            entity.chatId = membership.chatId();
            entity.platform = toEntityPlatform(membership.platform());
            entity.joinedAt = membership.joinedAt();
            entity.lastCheckedAt = membership.lastCheckedAt();
            entity.persist();
        } else {
            // Update lastCheckedAt
            UserGroupMembership.findByChatIdAndPlatform(membership.chatId(), toEntityPlatform(membership.platform()))
                    .stream()
                    .filter(m -> m.platformUserId == membership.platformUserId())
                    .findFirst()
                    .ifPresent(m -> {
                        m.lastCheckedAt = membership.lastCheckedAt();
                        m.persist();
                    });
        }
    }

    @Override
    @Transactional
    public boolean remove(long platformUserId, long chatId, ru.mitrohinayulya.zabotushka.shared.domain.Platform platform) {
        return UserGroupMembership.removeMembership(platformUserId, chatId, toEntityPlatform(platform));
    }

    private GroupMembership toDomain(UserGroupMembership entity) {
        return new GroupMembership(
                entity.id, entity.platformUserId, entity.chatId,
                toSharedPlatform(entity.platform), entity.joinedAt, entity.lastCheckedAt
        );
    }

    private Platform toEntityPlatform(ru.mitrohinayulya.zabotushka.shared.domain.Platform p) {
        return Platform.valueOf(p.name());
    }

    private ru.mitrohinayulya.zabotushka.shared.domain.Platform toSharedPlatform(Platform p) {
        return ru.mitrohinayulya.zabotushka.shared.domain.Platform.valueOf(p.name());
    }
}
