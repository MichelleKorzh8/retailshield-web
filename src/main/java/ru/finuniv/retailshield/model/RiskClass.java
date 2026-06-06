package ru.finuniv.retailshield.model;

import ru.finuniv.retailshield.data.Coefficients;

/**
 * Класс риска по итоговому Score_total с учётом K_неопр.
 * Калибровка по Блоку 1, таблица А.20.
 */
public enum RiskClass {

    LOW("Низкий",
            0.0, Coefficients.THRESHOLD_LOW,
            Coefficients.K_SCORE_LOW,
            "Стандартный полис со скидкой 30% к базовой премии"),

    MEDIUM("Умеренный",
            Coefficients.THRESHOLD_LOW + 0.01, Coefficients.THRESHOLD_MEDIUM,
            Coefficients.K_SCORE_MEDIUM,
            "Стандартный полис без надбавок"),

    HIGH("Высокий",
            Coefficients.THRESHOLD_MEDIUM + 0.01, Coefficients.THRESHOLD_HIGH,
            Coefficients.K_SCORE_HIGH,
            "Надбавка 30% к премии и повышенная франшиза. "
                    + "Устранение недостатков в течение 90 дней."),

    VERY_HIGH("Очень высокий",
            Coefficients.THRESHOLD_HIGH + 0.01, Coefficients.THRESHOLD_VERY_HIGH,
            Coefficients.K_SCORE_VERY_HIGH,
            "Надбавка 60% к премии и четырёхкратная франшиза. "
                    + "Устранение критических недостатков в течение 30 дней."),

    CRITICAL("Критический",
            Coefficients.THRESHOLD_VERY_HIGH + 0.01, Double.MAX_VALUE,
            Double.NaN,
            "Отказ в страховании в стандартных условиях.");

    private final String label;
    private final double minScore;
    private final double maxScore;
    private final double kScore;
    private final String underwriterDecision;

    RiskClass(String label, double minScore, double maxScore,
              double kScore, String underwriterDecision) {
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.kScore = kScore;
        this.underwriterDecision = underwriterDecision;
    }

    public String getLabel() {
        return label;
    }

    public double getKScore() {
        return kScore;
    }

    public String getUnderwriterDecision() {
        return underwriterDecision;
    }

    public boolean isAcceptable() {
        return this != CRITICAL;
    }

    /** Возвращает класс риска по значению скора (после применения K_неопр и отсечки). */
    public static RiskClass byScore(double score) {
        for (RiskClass rc : values()) {
            if (score >= rc.minScore && score <= rc.maxScore) {
                return rc;
            }
        }
        return CRITICAL;
    }
}