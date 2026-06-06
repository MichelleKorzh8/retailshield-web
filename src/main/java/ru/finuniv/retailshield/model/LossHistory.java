package ru.finuniv.retailshield.model;

/**
 * История киберинцидентов компании за последние 12 месяцев.
 * Используется для расчёта K_истории в формуле премии (Блок 3, глава 6).
 */
public enum LossHistory {

    NONE("Инцидентов не было (безубыточность 12 месяцев)", 0.90),
    NO_HISTORY("История отсутствует (новый клиент)", 1.00),
    MINOR("Был инцидент с ущербом до 1 млн руб.", 1.10),
    MEDIUM("Был инцидент с ущербом 1-10 млн руб.", 1.20),
    MAJOR("Был инцидент с ущербом свыше 10 млн руб.", 1.30);

    private final String label;
    private final double kHistory;

    LossHistory(String label, double kHistory) {
        this.label = label;
        this.kHistory = kHistory;
    }

    public String getLabel() {
        return label;
    }

    public double getKHistory() {
        return kHistory;
    }

    @Override
    public String toString() {
        return label;
    }
}