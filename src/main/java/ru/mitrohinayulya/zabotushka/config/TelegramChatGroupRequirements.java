package ru.mitrohinayulya.zabotushka.config;

import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Требования по квалификации для Telegram-групп с привязкой к ID чатов платформы.
 */
public enum TelegramChatGroupRequirements {
    GOLD_CLUB(-1001968543887L, ChatGroupRequirements.GOLD_CLUB),
    SILVER_CLUB(-1001891048040L, ChatGroupRequirements.SILVER_CLUB),
    BRONZE_CLUB(-1001835476759L, ChatGroupRequirements.BRONZE_CLUB),
    CAN_AFFORD(-1001811106801L, ChatGroupRequirements.CAN_AFFORD),
    CAN_AFFORD_CHAT(-1001929076200L, ChatGroupRequirements.CAN_AFFORD_CHAT);

    private final Long chatId;
    private final ChatGroupRequirements requirements;

    TelegramChatGroupRequirements(Long chatId, ChatGroupRequirements requirements) {
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

    public static Optional<TelegramChatGroupRequirements> findByChatId(Long chatId) {
        return Arrays.stream(values())
                .filter(group -> group.getChatId().equals(chatId))
                .findFirst();
    }

    public static String resolveGroupName(Long chatId) {
        return findByChatId(chatId)
                .map(TelegramChatGroupRequirements::getGroupName)
                .orElse("клуб");
    }
}
