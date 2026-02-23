package ru.mitrohinayulya.zabotushka.access.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

/// Port: what the Access Control bounded context needs from each messaging platform.
///
/// <h2>Problem this solves</h2>
/// The current abstract hierarchy ({@code AbstractGroupAccessService},
/// {@code AbstractJoinRequestService}) mixes the platform-agnostic algorithm with
/// platform-specific operations via inheritance. The abstract classes cannot be tested
/// without platform dependencies, and adding a new platform means subclassing two
/// abstract classes and threading through both hierarchies.
///
/// <h2>DDD approach — Ports & Adapters (Hexagonal)</h2>
/// The Access application services ({@link JoinRequestUseCase}, {@link QualificationCheckUseCase})
/// depend on this port. Each platform provides one adapter implementing this port:
/// <ul>
///   <li>{@code platform.telegram.TelegramAccessAdapter}</li>
///   <li>{@code platform.max.MaxAccessAdapter}</li>
///   <li>{@code platform.vk.VkAccessAdapter} — add when VK is needed</li>
/// </ul>
///
/// <h2>Adding a new platform</h2>
/// <ol>
///   <li>Add {@code Platform.VK} to the shared kernel.</li>
///   <li>Implement this port as {@code VkAccessAdapter}.</li>
///   <li>Add a {@code VkWebhookResource} that calls {@code JoinRequestUseCase}.</li>
///   <li>Zero changes to the use cases, domain objects, or scheduler.</li>
/// </ol>
public interface PlatformAccessPort {

    Platform platform();

    boolean isMember(long chatId, long userId);

    void removeMember(long chatId, long userId);

    void notifyRemoved(long userId, String groupName);

    void approveJoinRequest(long chatId, long userId, String groupName);

    void declineJoinRequest(long chatId, long userId, String groupName);
}
