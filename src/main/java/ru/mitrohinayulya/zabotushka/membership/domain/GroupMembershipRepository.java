package ru.mitrohinayulya.zabotushka.membership.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

import java.util.List;
import java.util.Optional;

/// Repository port for the Group Membership bounded context.
///
/// <h2>What this replaces</h2>
/// Static Panache methods on {@code UserGroupMembership} entity:
/// {@code findByChatIdAndPlatform}, {@code exists}, {@code removeMembership}.
/// Those methods are called from the scheduler, service classes, and the abstract
/// service hierarchy — meaning 4+ classes know about the JPA entity structure.
///
/// With this port, only the infrastructure implementation knows about Panache/JPA.
public interface GroupMembershipRepository {

    boolean exists(long platformUserId, long chatId, Platform platform);

    Optional<GroupMembership> find(long platformUserId, long chatId, Platform platform);

    List<GroupMembership> findByChatIdAndPlatform(long chatId, Platform platform);

    void save(GroupMembership membership);

    /// Removes the membership record. Returns true if a record was deleted.
    boolean remove(long platformUserId, long chatId, Platform platform);
}
