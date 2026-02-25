package ru.mitrohinayulya.zabotushka.scheduler.qualification;

/// Aggregated statistics for a qualification check.
public record QualificationProcessStats(int checked, int removed, int orphanedRemoved, int errors) {

    public static QualificationProcessStats empty() {
        return new QualificationProcessStats(0, 0, 0, 0);
    }

    public QualificationProcessStats merge(QualificationProcessStats other) {
        return new QualificationProcessStats(
                checked + other.checked,
                removed + other.removed,
                orphanedRemoved + other.orphanedRemoved,
                errors + other.errors
        );
    }
}
