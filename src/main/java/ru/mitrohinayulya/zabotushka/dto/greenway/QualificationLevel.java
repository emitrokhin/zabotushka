package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonValue;

/// MyGreenway partner qualification level
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

    @JsonValue
    public String getValue() {
        return name();
    }

    /// Extracts the qualification letter from a full qualification string
    /// For example, "S1" -> S, "L3" -> L, "M2" -> M, "GM4" -> GM
    public static QualificationLevel fromString(String qualification) {
        if (qualification == null || qualification.isBlank()) {
            return NO;
        }

        String upper = qualification.toUpperCase().trim();

        if (upper.startsWith("GM")) {
            return GM;
        } else if (upper.startsWith("M")) {
            return M;
        } else if (upper.startsWith("L")) {
            return L;
        } else if (upper.startsWith("S")) {
            return S;
        } else {
            return NO;
        }
    }

    /// Returns true if this qualification level meets or exceeds the required level
    public boolean isAtLeast(QualificationLevel required) {
        return this.rank >= required.rank;
    }

    /// Returns the better of two qualifications
    public static QualificationLevel best(QualificationLevel q1, QualificationLevel q2) {
        return q1.rank > q2.rank ? q1 : q2;
    }
}
