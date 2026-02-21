package ru.mitrohinayulya.zabotushka.service.platform;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mitrohinayulya.zabotushka.config.ChatGroupRequirements;
import ru.mitrohinayulya.zabotushka.entity.Platform;
import ru.mitrohinayulya.zabotushka.service.greenway.GreenwayQualificationService;

import java.util.Optional;

///Шаблонный метод для общей логики проверки и удаления участников по квалификации.
///Платформо-специфичные детали реализуются в подклассах.
public abstract class AbstractGroupAccessService implements PlatformGroupAccessService {

    private static final Logger log = LoggerFactory.getLogger(AbstractGroupAccessService.class);

    @Inject
    protected GreenwayQualificationService qualificationService;

    @Inject
    protected PlatformGroupMembershipService membershipService;

    protected abstract Platform getPlatform();

    protected abstract Optional<ChatGroupRequirements> findRequirements(long chatId);

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

        var qualification = qualificationService.getBestQualification(greenwayId);

        log.info("User qualification check: platform={}, chatId={}, userId={}, greenwayId={}, qualification={}",
                getPlatform(), chatId, userId, greenwayId, qualification);

        if (!groupRequirements.get().isQualificationAllowed(qualification)) {
            log.info("Qualification does not meet requirements, removing user: platform={}, chatId={}, userId={}, qualification={}",
                    getPlatform(), chatId, userId, qualification);
            removeMemberFromChat(chatId, userId);
        } else {
            log.info("Qualification meets requirements: platform={}, chatId={}, userId={}, qualification={}",
                    getPlatform(), chatId, userId, qualification);
        }
    }
}
