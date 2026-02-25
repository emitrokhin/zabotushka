package ru.mitrohinayulya.zabotushka.dto.greenway;

/// Ответ с квалификацией партнера
public record QualificationResponse(
        String qualification
) {
    public static QualificationResponse of(String qualification) {
        return new QualificationResponse(qualification);
    }

    public static QualificationResponse of(QualificationLevel level) {
        return new QualificationResponse(level.getValue());
    }
}
