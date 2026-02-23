package ru.mitrohinayulya.zabotushka.membership.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

import java.time.LocalDateTime;
import java.util.UUID;

/// Aggregate root for the Group Membership bounded context.
///
/// Tracks that a specific platform user is a member of a specific group.
/// The lifecycle (join/leave/check) is managed here, not scattered across services.
///
/// Plain domain object — no JPA, no Panache. Persistence via {@link GroupMembershipRepository}.
public final class GroupMembership {

    private final UUID id;
    private final long platformUserId;
    private final long chatId;
    private final Platform platform;
    private final LocalDateTime joinedAt;
    private LocalDateTime lastCheckedAt;

    public GroupMembership(UUID id, long platformUserId, long chatId, Platform platform,
                           LocalDateTime joinedAt, LocalDateTime lastCheckedAt) {
        this.id = id;
        this.platformUserId = platformUserId;
        this.chatId = chatId;
        this.platform = platform;
        this.joinedAt = joinedAt;
        this.lastCheckedAt = lastCheckedAt;
    }

    public static GroupMembership create(long platformUserId, long chatId, Platform platform) {
        return new GroupMembership(
                UUID.randomUUID(), platformUserId, chatId, platform,
                LocalDateTime.now(), null
        );
    }

    /// Domain operation: record that this membership was checked right now.
    public void recordCheck() {
        this.lastCheckedAt = LocalDateTime.now();
    }

    public UUID id()                      { return id; }
    public long platformUserId()          { return platformUserId; }
    public long chatId()                  { return chatId; }
    public Platform platform()            { return platform; }
    public LocalDateTime joinedAt()       { return joinedAt; }
    public LocalDateTime lastCheckedAt()  { return lastCheckedAt; }
}
