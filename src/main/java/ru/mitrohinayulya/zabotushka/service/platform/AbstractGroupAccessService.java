package ru.mitrohinayulya.zabotushka.service.platform;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationResult;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;

import java.util.Optional;

/// Template method for common logic of checking and removing members based on qualification.
/// Platform-specific details are implemented in subclasses.
public abstract class AbstractGroupAccessService implements PlatformGroupAccessService {

    private static final Logger log = LoggerFactory.getLogger(AbstractGroupAccessService.class);

    @Inject
    protected GreenwayQualificationService qualificationService;

    @Inject
    protected PlatformGroupMembershipService membershipService;

    protected abstract Platform getPlatform();

    protected abstract Optional<ChatGroupRequirements> findRequirements(long chatId);

    protected void onUserRemainsQualified(long chatId, long userId, QualificationResult result) {
        // no-op by default
    }

    @Override
    public final void checkAndRemoveIfNotQualified(long chatId, long userId, long greenwayId) {
        log.info("Checking qualification: platform={}, chatId={}, userId={}, greenwayId={}",
                getPlatform(), chatId, userId, greenwayId);

        if (!isMemberOfChat(chatId, userId)) {
            log.info("User is not a member of chat (left by themselves): platform={}, chatId={}, userId={}",
                    getPlatform(), chatId, userId);
            membershipService.removeMembership(chatId, userId, getPlatform());
            return;
        }

        var groupRequirements = findRequirements(chatId);
        if (groupRequirements.isEmpty()) {
            log.warn("No requirements found for chatId={}", chatId);
            return;
        }

        var qualResult = qualificationService.getBestQualificationResult(greenwayId);

        log.info("User qualification check: platform={}, chatId={}, userId={}, greenwayId={}, qualification={}",
                getPlatform(), chatId, userId, greenwayId, qualResult.level());

        if (!groupRequirements.get().isQualificationAllowed(qualResult.level())) {
            log.info("Qualification does not meet requirements, removing user: platform={}, chatId={}, userId={}, qualification={}",
                    getPlatform(), chatId, userId, qualResult.level());
            removeMemberFromChat(chatId, userId);
        } else {
            log.info("Qualification meets requirements: platform={}, chatId={}, userId={}, qualification={}",
                    getPlatform(), chatId, userId, qualResult.level());
            onUserRemainsQualified(chatId, userId, qualResult);
        }
    }
}
