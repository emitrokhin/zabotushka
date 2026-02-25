package ru.mitrohinayulya.zabotushka.service.platform;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;

import java.util.Optional;

/// Template method for common logic of handling group join requests.
/// Steps 1–4 (finding requirements, authorization, qualification) are implemented here.
/// Platform-specific approve/decline actions are implemented in subclasses.
public abstract class AbstractJoinRequestService<E, U> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJoinRequestService.class);

    @Inject
    protected GreenwayQualificationService qualificationService;

    @Inject
    protected PlatformGroupMembershipService membershipService;

    protected abstract long extractChatId(E event);

    protected abstract long extractUserId(E event);

    protected abstract String extractUsername(E event);

    protected abstract Optional<ChatGroupRequirements> findRequirements(long chatId);

    protected abstract U findAuthorizedUser(long platformUserId);

    protected abstract long getGreenwayId(U user);

    protected abstract void onApproved(E event, U user, ChatGroupRequirements req);

    protected abstract void onDeclined(E event);

    protected final void process(E event) {
        var chatId = extractChatId(event);
        var userId = extractUserId(event);
        var username = extractUsername(event);

        log.info("Processing join request: chatId={}, userId={}, username={}", chatId, userId, username);

        var req = findRequirements(chatId);
        if (req.isEmpty()) {
            log.warn("No requirements found for chatId={}, declining by default", chatId);
            onDeclined(event);
            return;
        }

        var user = findAuthorizedUser(userId);
        if (user == null) {
            log.warn("User not authorized: userId={}, declining", userId);
            onDeclined(event);
            return;
        }

        var qual = qualificationService.getBestQualification(getGreenwayId(user));

        log.info("User qualification: userId={}, greenwayId={}, qualification={}", userId, getGreenwayId(user), qual);

        if (req.get().isQualificationAllowed(qual)) {
            log.info("Qualification meets requirements, approving: chatId={}, userId={}", chatId, userId);
            onApproved(event, user, req.get());
        } else {
            log.info("Qualification does not meet requirements, declining: chatId={}, userId={}", chatId, userId);
            onDeclined(event);
        }
    }
}
