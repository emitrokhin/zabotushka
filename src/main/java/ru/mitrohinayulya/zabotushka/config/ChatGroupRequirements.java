package ru.mitrohinayulya.zabotushka.config;

import ru.mitrohinayulya.zabotushka.dto.greenway.QualificationLevel;

/// Common qualification requirements for club groups (without binding to platform chat IDs).
/// Specific chat IDs are defined in platform-specific configurations:
/// @see TelegramChatGroupRequirements
/// @see MaxChatGroupRequirements
public enum ChatGroupRequirements {
    GOLD_CLUB("Золотой клуб", QualificationLevel.M),
    SILVER_CLUB("Серебряный клуб", QualificationLevel.L),
    BRONZE_CLUB("Бронзовый клуб", QualificationLevel.S),
    CAN_AFFORD("Могу себе позволить", QualificationLevel.S),
    CAN_AFFORD_CHAT("Могу себе позволить chat", QualificationLevel.S);

    private final String groupName;
    private final QualificationLevel minimumLevel;

    ChatGroupRequirements(String groupName, QualificationLevel minimumLevel) {
        this.groupName = groupName;
        this.minimumLevel = minimumLevel;
    }

    public String getGroupName() {
        return groupName;
    }

    public QualificationLevel getMinimumLevel() {
        return minimumLevel;
    }

    public boolean isQualificationAllowed(QualificationLevel qualification) {
        return qualification.isAtLeast(minimumLevel);
    }
}
