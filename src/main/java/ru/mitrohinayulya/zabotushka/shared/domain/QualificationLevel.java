package ru.mitrohinayulya.zabotushka.shared.domain;

/// Shared kernel value: a Greenway partner's qualification tier.
///
/// This is a core domain concept used across multiple bounded contexts
/// (qualification checking, membership requirements, access decisions).
/// It must NOT live inside {@code dto/greenway/} — that package is
/// infrastructure for the Greenway anti-corruption layer.
///
/// Migration note: replace usages of {@code dto.greenway.QualificationLevel}
/// with this class and delete the old one.
public enum QualificationLevel {
    NO(0),
    S(1),
    L(2),
    M(3),
    GM(4);

    private final int rank;

    QualificationLevel(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    /// Parses a Greenway API qualification string like "S1", "L3", "M2", "GM4".
    public static QualificationLevel fromGreenwayString(String value) {
        if (value == null || value.isBlank()) {
            return NO;
        }
        String upper = value.toUpperCase().trim();
        if (upper.startsWith("GM")) return GM;
        if (upper.startsWith("M"))  return M;
        if (upper.startsWith("L"))  return L;
        if (upper.startsWith("S"))  return S;
        return NO;
    }

    public static QualificationLevel best(QualificationLevel a, QualificationLevel b) {
        return a.rank > b.rank ? a : b;
    }
}
