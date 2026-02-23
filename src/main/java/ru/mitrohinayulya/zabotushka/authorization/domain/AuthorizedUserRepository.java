package ru.mitrohinayulya.zabotushka.authorization.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

import java.util.List;
import java.util.Optional;

/// Repository port for the authorization bounded context.
///
/// Defines the persistence contract in domain terms — no JPA, no SQL.
/// The implementation in {@code authorization.infrastructure.persistence} can use
/// Panache, JDBC, or any other mechanism without leaking into the domain.
///
/// <h2>Current duplication this replaces</h2>
/// Static Panache methods scattered across {@code AuthorizedTelegramUser} and
/// {@code AuthorizedMaxUser} (e.g. {@code findByTelegramId}, {@code existsByMaxId}).
/// Those are infrastructure concerns surfaced inside the entity — a classic Active Record
/// anti-pattern that makes the domain depend on the ORM.
public interface AuthorizedUserRepository {

    Optional<AuthorizedUser> findByPlatformUserId(long platformUserId, Platform platform);

    boolean existsByPlatformUserId(long platformUserId, Platform platform);

    /// Cross-platform uniqueness check: a Greenway ID may not be linked to two different
    /// platform accounts, regardless of platform.
    boolean existsByGreenwayId(long greenwayId);

    List<AuthorizedUser> findAllByPlatform(Platform platform);

    void save(AuthorizedUser user);
}
