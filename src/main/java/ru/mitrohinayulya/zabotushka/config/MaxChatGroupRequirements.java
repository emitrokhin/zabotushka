package ru.mitrohinayulya.zabotushka.config;

import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/// Qualification requirements for Max groups bound to platform chat IDs.
public enum MaxChatGroupRequirements {
    GOLD_CLUB(-71062621438079L, ChatGroupRequirements.GOLD_CLUB);

    private final long chatId;
    private final ChatGroupRequirements requirements;

    MaxChatGroupRequirements(long chatId, ChatGroupRequirements requirements) {
        this.chatId = chatId;
        this.requirements = requirements;
    }

    public long getChatId() {
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

    public static Optional<MaxChatGroupRequirements> findByChatId(long chatId) {
        return Arrays.stream(values())
                .filter(group -> group.getChatId() == chatId)
                .findFirst();
    }

    public static String resolveGroupName(long chatId) {
        return findByChatId(chatId)
                .map(MaxChatGroupRequirements::getGroupName)
                .orElse("клуб");
    }
}
