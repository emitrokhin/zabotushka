package ru.mitrohinayulya.zabotushka.config;

import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

import java.util.List;

/// Common qualification requirements for club groups (without binding to platform chat IDs).
/// Specific chat IDs are defined in platform-specific configurations:
/// @see TelegramChatGroupRequirements
/// @see MaxChatGroupRequirements
public enum ChatGroupRequirements {
    GOLD_CLUB("Золотой клуб", List.of(QualificationLevel.M, QualificationLevel.GM)),
    SILVER_CLUB("Серебряный клуб", List.of(QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM)),
    BRONZE_CLUB("Бронзовый клуб", List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM)),
    CAN_AFFORD("Могу себе позволить", List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM)),
    CAN_AFFORD_CHAT("Могу себе позволить chat", List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM));

    private final String groupName;
    private final List<QualificationLevel> allowedQualifications;

    ChatGroupRequirements(String groupName, List<QualificationLevel> allowedQualifications) {
        this.groupName = groupName;
        this.allowedQualifications = allowedQualifications;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<QualificationLevel> getAllowedQualifications() {
        return allowedQualifications;
    }

    public boolean isQualificationAllowed(QualificationLevel qualification) {
        return allowedQualifications.contains(qualification);
    }
}
