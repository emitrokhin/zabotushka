package ru.mitrohinayulya.zabotushka.authorization.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

import java.time.LocalDateTime;
import java.util.UUID;

/// Aggregate root for the Partner Authorization bounded context.
///
/// Represents a single link between a platform user identity and a Greenway partner account.
/// This replaces the two separate entities {@code AuthorizedTelegramUser} and
/// {@code AuthorizedMaxUser}, which are structurally identical and would keep multiplying
/// with each new platform.
///
/// The domain object is pure Java — no JPA annotations, no Panache.
/// Persistence is handled by {@link AuthorizedUserRepository} whose implementation
/// lives in {@code authorization.infrastructure.persistence}.
///
/// <h2>Adding VK</h2>
/// No change needed here. Simply add {@code Platform.VK}, implement
/// {@code AuthorizedUserRepository} for it (or reuse the unified table),
/// add a {@code VkAuthorizationResource}, and a {@code VkAccessAdapter}.
public final class AuthorizedUser {

    private final UUID id;
    private final long platformUserId;
    private final Platform platform;
    private final long greenwayId;
    private final String regDate;
    private final LocalDateTime createdAt;

    /// Factory for reconstituting from persistence.
    public AuthorizedUser(UUID id, long platformUserId, Platform platform,
                          long greenwayId, String regDate, LocalDateTime createdAt) {
        this.id = id;
        this.platformUserId = platformUserId;
        this.platform = platform;
        this.greenwayId = greenwayId;
        this.regDate = regDate;
        this.createdAt = createdAt;
    }

    /// Factory for creating a new user (generates ID and timestamp).
    public static AuthorizedUser create(long platformUserId, Platform platform,
                                        long greenwayId, String regDate) {
        return new AuthorizedUser(
                UUID.randomUUID(), platformUserId, platform,
                greenwayId, regDate, LocalDateTime.now()
        );
    }

    /// Domain logic: verifies the supplied credentials match what was stored at authorization time.
    /// Used for re-authorization flow.
    public boolean matchesCredentials(long greenwayId, String regDate) {
        return this.greenwayId == greenwayId && this.regDate.equals(regDate);
    }

    public UUID id()              { return id; }
    public long platformUserId()  { return platformUserId; }
    public Platform platform()    { return platform; }
    public long greenwayId()      { return greenwayId; }
    public String regDate()       { return regDate; }
    public LocalDateTime createdAt() { return createdAt; }
}
