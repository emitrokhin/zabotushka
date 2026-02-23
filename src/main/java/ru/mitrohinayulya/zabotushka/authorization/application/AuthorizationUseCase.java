package ru.mitrohinayulya.zabotushka.authorization.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.authorization.domain.AuthorizedUser;
import ru.mitrohinayulya.zabotushka.authorization.domain.AuthorizedUserRepository;
import ru.mitrohinayulya.zabotushka.qualification.domain.GreenwayPartnerPort;

/// Application service for the Partner Authorization bounded context.
///
/// <h2>Problem this solves</h2>
/// The old design passes {@code PlatformAuthorizationService} as a parameter to
/// {@code GreenwayAuthorizationService#authorize(...)}. That is a procedural callback
/// pattern: the "service" isn't a service, it's a strategy object, and the caller
/// must know which strategy to pass. There is no clear owner of the authorization
/// decision.
///
/// <h2>DDD approach</h2>
/// This use case owns the authorization decision. It receives a command (data only),
/// uses the {@link AuthorizedUserRepository} port (domain contract) and the
/// {@link GreenwayPartnerPort} (anti-corruption layer port) to make the decision,
/// and returns a typed {@link AuthorizationResult} — no HTTP, no ORM leaking in.
///
/// The resource layer ({@code TelegramAuthorizationResource}, {@code MaxAuthorizationResource})
/// calls this use case and maps the result to HTTP responses. Adding VK requires only
/// a new resource class — no changes here.
@ApplicationScoped
public class AuthorizationUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationUseCase.class);

    @Inject
    AuthorizedUserRepository repository;

    @Inject
    GreenwayPartnerPort greenwayPartnerPort;

    public AuthorizationResult authorize(AuthorizeUserCommand cmd) {
        log.info("Authorizing user: platform={}, platformUserId={}, greenwayId={}",
                cmd.platform(), cmd.platformUserId(), cmd.greenwayId());

        // Re-authorization: user already linked — just verify credentials haven't changed
        var existing = repository.findByPlatformUserId(cmd.platformUserId(), cmd.platform());
        if (existing.isPresent()) {
            if (existing.get().matchesCredentials(cmd.greenwayId(), cmd.regDate())) {
                log.info("Re-authorization successful: platform={}, platformUserId={}",
                        cmd.platform(), cmd.platformUserId());
                return new AuthorizationResult.AlreadyAuthorized();
            }
            log.warn("Authorization rejected: credential mismatch for platform={}, platformUserId={}",
                    cmd.platform(), cmd.platformUserId());
            return new AuthorizationResult.DataMismatch();
        }

        // Cross-platform Greenway ID uniqueness check
        if (repository.existsByGreenwayId(cmd.greenwayId())) {
            log.warn("Authorization rejected: greenwayId={} already linked to another account",
                    cmd.greenwayId());
            return new AuthorizationResult.GreenwayIdConflict();
        }

        // Verify against Greenway API
        var partnerRegDate = greenwayPartnerPort.findRegistrationDate(cmd.greenwayId());
        if (partnerRegDate.isEmpty()) {
            log.warn("Partner not found in Greenway: greenwayId={}", cmd.greenwayId());
            return new AuthorizationResult.PartnerNotFound();
        }

        if (!datesMatch(partnerRegDate.get(), cmd.regDate())) {
            log.warn("Authorization failed: regDate mismatch for greenwayId={}", cmd.greenwayId());
            return new AuthorizationResult.InvalidCredentials();
        }

        var user = AuthorizedUser.create(cmd.platformUserId(), cmd.platform(), cmd.greenwayId(), cmd.regDate());
        repository.save(user);
        log.info("Authorization successful: platform={}, platformUserId={}, greenwayId={}",
                cmd.platform(), cmd.platformUserId(), cmd.greenwayId());
        return new AuthorizationResult.Authorized();
    }

    private boolean datesMatch(String greenwayDate, String supplied) {
        if (greenwayDate == null || supplied == null) return false;
        if (greenwayDate.equals(supplied)) return true;
        try {
            var fmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return java.time.LocalDate.parse(greenwayDate, fmt)
                    .equals(java.time.LocalDate.parse(supplied, fmt));
        } catch (java.time.format.DateTimeParseException _) {
            return false;
        }
    }
}
