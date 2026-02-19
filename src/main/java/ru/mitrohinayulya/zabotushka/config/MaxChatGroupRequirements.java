package ru.mitrohinayulya.zabotushka.config;

import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Требования по квалификации для групп
 */
public enum MaxChatGroupRequirements {
    GROUP_1(-1001968543887L, List.of(QualificationLevel.M, QualificationLevel.GM)),
    GROUP_2(-1001891048040L, List.of(QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM)),
    GROUP_3(-1001835476759L, List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM)),
    GROUP_4(-1001811106801L, List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM)),
    GROUP_5(-1001929076200L, List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM));

    private final Long chatId;
    private final List<QualificationLevel> allowedQualifications;

    MaxChatGroupRequirements(Long chatId, List<QualificationLevel> allowedQualifications) {
        this.chatId = chatId;
        this.allowedQualifications = allowedQualifications;
    }

    public Long getChatId() {
        return chatId;
    }

    public List<QualificationLevel> getAllowedQualifications() {
        return allowedQualifications;
    }

    /**
     * Проверяет, соответствует ли квалификация требованиям группы
     */
    public boolean isQualificationAllowed(QualificationLevel qualification) {
        return allowedQualifications.contains(qualification);
    }

    /**
     * Находит требования для группы по её ID
     */
    public static Optional<MaxChatGroupRequirements> findByChatId(Long chatId) {
        return Arrays.stream(values())
                .filter(group -> group.getChatId().equals(chatId))
                .findFirst();
    }
}
