package ru.mitrohinayulya.zabotushka.config;

import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import java.util.Arrays;
import java.util.Optional;

/// Qualification requirements for Telegram groups bound to platform chat IDs.
public enum TelegramChatGroupRequirements {
    GOLD_CLUB(-1001968543887L, ChatGroupRequirements.GOLD_CLUB),
    SILVER_CLUB(-1001891048040L, ChatGroupRequirements.SILVER_CLUB),
    BRONZE_CLUB(-1001835476759L, ChatGroupRequirements.BRONZE_CLUB),
    CAN_AFFORD(-1001811106801L, ChatGroupRequirements.CAN_AFFORD),
    CAN_AFFORD_CHAT(-1001929076200L, ChatGroupRequirements.CAN_AFFORD_CHAT);

    private final long chatId;
    private final ChatGroupRequirements requirements;

    TelegramChatGroupRequirements(long chatId, ChatGroupRequirements requirements) {
        this.chatId = chatId;
        this.requirements = requirements;
    }

    public long getChatId() {
        return chatId;
    }

    public String getGroupName() {
        return requirements.getGroupName();
    }

    public QualificationLevel getMinimumLevel() {
        return requirements.getMinimumLevel();
    }

    public boolean isQualificationAllowed(QualificationLevel qualification) {
        return requirements.isQualificationAllowed(qualification);
    }

    public ChatGroupRequirements getRequirements() {
        return requirements;
    }

    public static Optional<TelegramChatGroupRequirements> findByChatId(long chatId) {
        return Arrays.stream(values())
                .filter(group -> group.getChatId() == chatId)
                .findFirst();
    }

    public static String resolveGroupName(long chatId) {
        return findByChatId(chatId)
                .map(TelegramChatGroupRequirements::getGroupName)
                .orElse("клуб");
    }
}
