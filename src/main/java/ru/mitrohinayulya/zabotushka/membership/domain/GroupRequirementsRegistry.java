package ru.mitrohinayulya.zabotushka.membership.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

import java.util.List;
import java.util.Optional;

/// Port: maps a platform chat ID to its qualification requirements.
///
/// <h2>Problem this solves</h2>
/// Currently {@code TelegramChatGroupRequirements} and {@code MaxChatGroupRequirements}
/// are enums with hardcoded chat IDs in source code. Every new platform needs a new enum.
/// Every new group requires a code change and redeployment. Service classes and processors
/// import these enums directly, creating compile-time coupling between the service layer
/// and the configuration layer.
///
/// <h2>DDD approach</h2>
/// This port expresses what the domain needs: "given a chatId and platform, give me the
/// requirements." The implementation can be backed by:
/// <ul>
///   <li>The existing enums (transitional, zero-risk first step)</li>
///   <li>A YAML/properties config (next step, enables runtime changes)</li>
///   <li>A database table (eventual, enables admin UI)</li>
/// </ul>
/// The domain and application layers never change — only the infrastructure implementation.
public interface GroupRequirementsRegistry {

    Optional<GroupRequirements> findByChatId(long chatId, Platform platform);

    /// Returns all managed chat IDs for the given platform.
    /// Used by the qualification scheduler to know which groups to check.
    List<Long> chatIdsForPlatform(Platform platform);

    /// Resolves a human-readable group name for user notifications.
    /// Returns a fallback if the chatId is not managed.
    String resolveGroupName(long chatId, Platform platform);
}
