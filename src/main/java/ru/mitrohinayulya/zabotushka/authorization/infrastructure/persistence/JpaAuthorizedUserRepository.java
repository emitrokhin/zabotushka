package ru.mitrohinayulya.zabotushka.authorization.infrastructure.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.mitrohinayulya.zabotushka.authorization.domain.AuthorizedUser;
import ru.mitrohinayulya.zabotushka.authorization.domain.AuthorizedUserRepository;
import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

import java.util.List;
import java.util.Optional;

/// Infrastructure implementation of {@link AuthorizedUserRepository}.
///
/// <h2>Migration strategy (two-phase)</h2>
///
/// <h3>Phase 1 — Delegate to existing services (zero DB change)</h3>
/// Delegate to {@code AuthorizedTelegramUserService} and {@code AuthorizedMaxUserService}
/// via a platform switch. This lets the application layer migrate to the new port
/// without any schema change:
/// <pre>
///   public Optional&lt;AuthorizedUser&gt; findByPlatformUserId(long id, Platform platform) {
///       return switch (platform) {
///           case TELEGRAM -> Optional.ofNullable(telegramService.findByTelegramId(id))
///               .map(u -> new AuthorizedUser(u.id, u.telegramId, TELEGRAM, u.greenwayId, u.regDate, u.creationDate));
///           case MAX -> Optional.ofNullable(maxService.findByMaxId(id))
///               .map(u -> new AuthorizedUser(u.id, u.maxId, MAX, u.greenwayId, u.regDate, u.creationDate));
///           case VK -> Optional.empty();
///       };
///   }
/// </pre>
///
/// <h3>Phase 2 — Unified table</h3>
/// Add a Flyway migration creating a single {@code authorized_users} table with a
/// {@code platform} discriminator column. Migrate data from the two existing tables.
/// Replace the delegation with direct Panache/JDBC queries on the new table.
/// Delete {@code AuthorizedTelegramUser}, {@code AuthorizedMaxUser}, and their services.
///
/// The application layer (use cases, resources) is identical across both phases.
@ApplicationScoped
public class JpaAuthorizedUserRepository implements AuthorizedUserRepository {

    // Phase 1: inject the two existing services and delegate
    // Phase 2: use a unified Panache entity

    @Override
    public Optional<AuthorizedUser> findByPlatformUserId(long platformUserId, Platform platform) {
        // TODO Phase 1: delegate to existing services
        throw new UnsupportedOperationException("Implement in Phase 1 by delegating to existing services");
    }

    @Override
    public boolean existsByPlatformUserId(long platformUserId, Platform platform) {
        throw new UnsupportedOperationException("Implement in Phase 1 by delegating to existing services");
    }

    @Override
    public boolean existsByGreenwayId(long greenwayId) {
        // Cross-platform: check both tables in Phase 1, unified table in Phase 2
        throw new UnsupportedOperationException("Implement in Phase 1 by delegating to existing services");
    }

    @Override
    public List<AuthorizedUser> findAllByPlatform(Platform platform) {
        throw new UnsupportedOperationException("Implement in Phase 1 by delegating to existing services");
    }

    @Override
    @Transactional
    public void save(AuthorizedUser user) {
        throw new UnsupportedOperationException("Implement in Phase 1 by delegating to existing services");
    }
}
