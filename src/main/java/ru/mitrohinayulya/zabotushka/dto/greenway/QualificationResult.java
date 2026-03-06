package ru.mitrohinayulya.zabotushka.dto.greenway;

/// Holds both the resolved qualification level and the raw qualification string from Greenway API
public record QualificationResult(QualificationLevel level, String rawQual) {
}
