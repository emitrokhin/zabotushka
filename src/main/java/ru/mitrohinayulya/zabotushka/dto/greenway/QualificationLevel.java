package ru.mitrohinayulya.zabotushka.dto.greenway;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Уровень квалификации партнера MyGreenway
 */
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

    public int getRank() {
        return rank;
    }

    /**
     * Извлекает букву квалификации из полной строки
     * Например: "S1" -> S, "L3" -> L, "M2" -> M, "GM4" -> GM
     */
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

    /**
     * Возвращает лучшую квалификацию из двух
     */
    public static QualificationLevel best(QualificationLevel q1, QualificationLevel q2) {
        return q1.rank > q2.rank ? q1 : q2;
    }
}
