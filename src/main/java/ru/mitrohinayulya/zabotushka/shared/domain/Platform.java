package ru.mitrohinayulya.zabotushka.shared.domain;

/// Shared kernel value: the messaging platform a user belongs to.
///
/// This enum is the canonical definition used across all bounded contexts.
/// The existing {@code entity.Platform} should be migrated to reference this one,
/// or replaced by this class during the DDD refactoring.
///
/// Adding a new platform (e.g. VK) requires only adding an entry here — no other
/// shared-kernel change is needed. Each bounded context adapts independently.
public enum Platform {
    TELEGRAM,
    MAX,
    VK
}
