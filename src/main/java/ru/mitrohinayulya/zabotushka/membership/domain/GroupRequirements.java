package ru.mitrohinayulya.zabotushka.membership.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.QualificationLevel;

import java.util.List;

/// Value object: the qualification requirements for a single managed group.
///
/// This replaces the current {@code ChatGroupRequirements} enum (which is a domain concept
/// mis-placed in the {@code config} package and depends on the DTO-level
/// {@code QualificationLevel}).
///
/// It is a plain record — immutable, no ORM, no framework annotations.
public record GroupRequirements(String groupName, List<QualificationLevel> allowedQualifications) {

    public boolean isQualificationAllowed(QualificationLevel level) {
        return allowedQualifications.contains(level);
    }

    // Convenience factories matching existing logical groups
    public static GroupRequirements goldClub() {
        return new GroupRequirements("Золотой клуб",
                List.of(QualificationLevel.M, QualificationLevel.GM));
    }

    public static GroupRequirements silverClub() {
        return new GroupRequirements("Серебряный клуб",
                List.of(QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM));
    }

    public static GroupRequirements bronzeClub() {
        return new GroupRequirements("Бронзовый клуб",
                List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM));
    }

    public static GroupRequirements canAfford() {
        return new GroupRequirements("Могу себе позволить",
                List.of(QualificationLevel.S, QualificationLevel.L, QualificationLevel.M, QualificationLevel.GM));
    }
}
