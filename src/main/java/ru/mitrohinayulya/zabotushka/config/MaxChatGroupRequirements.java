package ru.mitrohinayulya.zabotushka.config;

import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/// Требования по квалификации для Max-групп с привязкой к ID чатов платформы.
public enum MaxChatGroupRequirements {
    GOLD_CLUB(-71062621438079L, ChatGroupRequirements.GOLD_CLUB);

    private final Long chatId;
    private final ChatGroupRequirements requirements;

    MaxChatGroupRequirements(Long chatId, ChatGroupRequirements requirements) {
        this.chatId = chatId;
        this.requirements = requirements;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getGroupName() {
        return requirements.getGroupName();
    }

    public List<QualificationLevel> getAllowedQualifications() {
        return requirements.getAllowedQualifications();
    }

    public boolean isQualificationAllowed(QualificationLevel qualification) {
        return requirements.isQualificationAllowed(qualification);
    }

    public ChatGroupRequirements getRequirements() {
        return requirements;
    }

    public static Optional<MaxChatGroupRequirements> findByChatId(Long chatId) {
        return Arrays.stream(values())
                .filter(group -> group.getChatId().equals(chatId))
                .findFirst();
    }

    public static String resolveGroupName(Long chatId) {
        return findByChatId(chatId)
                .map(MaxChatGroupRequirements::getGroupName)
                .orElse("клуб");
    }
}
