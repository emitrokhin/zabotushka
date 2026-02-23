package ru.mitrohinayulya.zabotushka.access.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.access.domain.PlatformAccessPort;
import ru.mitrohinayulya.zabotushka.authorization.domain.AuthorizedUserRepository;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupMembership;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupMembershipRepository;
import ru.mitrohinayulya.zabotushka.membership.domain.GroupRequirementsRegistry;
import ru.mitrohinayulya.zabotushka.qualification.domain.QualificationPort;
import ru.mitrohinayulya.zabotushka.shared.domain.Platform;

/// Application service: handles a user's request to join a managed group.
///
/// <h2>What this replaces</h2>
/// {@code AbstractJoinRequestService<E,U>} and its subclasses ({@code TelegramJoinRequestService},
/// {@code MaxJoinRequestService}). The abstract class pattern uses inheritance + generics to
/// share logic, which has two problems:
/// <ol>
///   <li>The two type parameters {@code <E, U>} tie the algorithm to platform-specific types,
///       so the algorithm cannot be understood without the concrete subclass.</li>
///   <li>Testing the base-class algorithm requires instantiating a subclass.</li>
/// </ol>
///
/// Here, all platform-specific behavior is pushed to {@link PlatformAccessPort}.
/// The use case is fully testable in isolation by mocking the ports.
///
/// Platform webhooks ({@code TelegramWebhookResource}, {@code MaxWebhookResource}) call
/// {@link #process(JoinRequest)} after mapping their platform events to this generic command.
@ApplicationScoped
public class JoinRequestUseCase {

    private static final Logger log = LoggerFactory.getLogger(JoinRequestUseCase.class);

    @Inject
    AuthorizedUserRepository authorizedUsers;

    @Inject
    GroupRequirementsRegistry requirementsRegistry;

    @Inject
    QualificationPort qualificationPort;

    @Inject
    GroupMembershipRepository membershipRepository;

    @Inject
    @Any
    Instance<PlatformAccessPort> accessPorts;

    /// Generic join-request command, platform-agnostic.
    /// Each platform webhook maps its event to this record before calling {@link #process}.
    public record JoinRequest(long chatId, long userId, String username, Platform platform) {}

    public void process(JoinRequest request) {
        log.info("Processing join request: platform={}, chatId={}, userId={}, username={}",
                request.platform(), request.chatId(), request.userId(), request.username());

        var requirements = requirementsRegistry.findByChatId(request.chatId(), request.platform());
        if (requirements.isEmpty()) {
            log.warn("No requirements configured for chatId={} on platform={}, declining by default",
                    request.chatId(), request.platform());
            accessPort(request.platform()).declineJoinRequest(
                    request.chatId(), request.userId(), "клуб");
            return;
        }

        var user = authorizedUsers.findByPlatformUserId(request.userId(), request.platform());
        if (user.isEmpty()) {
            log.warn("User not authorized: platform={}, userId={}, declining", request.platform(), request.userId());
            accessPort(request.platform()).declineJoinRequest(
                    request.chatId(), request.userId(), requirements.get().groupName());
            return;
        }

        var level = qualificationPort.getBestQualification(user.get().greenwayId());
        log.info("Qualification check: platform={}, userId={}, greenwayId={}, level={}",
                request.platform(), request.userId(), user.get().greenwayId(), level);

        var port = accessPort(request.platform());
        var groupName = requirements.get().groupName();

        if (requirements.get().isQualificationAllowed(level)) {
            log.info("Approving join request: platform={}, chatId={}, userId={}",
                    request.platform(), request.chatId(), request.userId());
            port.approveJoinRequest(request.chatId(), request.userId(), groupName);
            var membership = GroupMembership.create(request.userId(), request.chatId(), request.platform());
            membershipRepository.save(membership);
        } else {
            log.info("Declining join request: platform={}, chatId={}, userId={}",
                    request.platform(), request.chatId(), request.userId());
            port.declineJoinRequest(request.chatId(), request.userId(), groupName);
        }
    }

    private PlatformAccessPort accessPort(Platform platform) {
        return accessPorts.stream()
                .filter(p -> p.platform() == platform)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No PlatformAccessPort for " + platform));
    }
}
