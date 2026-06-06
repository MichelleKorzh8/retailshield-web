package ru.finuniv.retailshield.data;

/**
 * Константы методологии. Все весовые коэффициенты, тарифные ставки,
 * пороги классификации и границы коэффициентов собраны в одном месте,
 * чтобы при калибровке методологии менять их только здесь.
 *
 * Соответствует разделам 7-12 Блока 1 и главе 6 Блока 3 работы.
 */
public final class Coefficients {

    private Coefficients() {
    }

    // ===== Шкала скора =====

    /** Максимальный балл шкалы (используется для V = Score_total / Max_Score). */
    public static final double MAX_SCORE = 10.0;

    // ===== Веса разделов в Score_total (Блок 1, формула 7.2) =====

    public static final double WEIGHT_SECTION_A = 0.10;
    public static final double WEIGHT_SECTION_B = 0.30;
    public static final double WEIGHT_SECTION_V = 0.15;
    public static final double WEIGHT_SECTION_G = 0.20;
    public static final double WEIGHT_SECTION_D = 0.25;

    // ===== Коэффициент неопределённости K_неопр (Блок 1, раздел 7.2) =====

    /** Базовое значение K_неопр, когда правила А.26 не сработали. */
    public static final double K_UNCERTAINTY_BASE = 1.00;

    /** Минимальное значение K_неопр при хотя бы одном сработавшем правиле. */
    public static final double K_UNCERTAINTY_MIN = 1.10;

    /** Максимальное значение K_неопр (диапазон до 1,20 согласно работе). */
    public static final double K_UNCERTAINTY_MAX = 1.20;

    // ===== Границы классов риска (Блок 1, таблица А.20) =====

    public static final double THRESHOLD_LOW       = 2.0;
    public static final double THRESHOLD_MEDIUM    = 4.0;
    public static final double THRESHOLD_HIGH      = 6.0;
    public static final double THRESHOLD_VERY_HIGH = 8.0;

    // ===== K_скора (Блок 3, таблица Д.5) =====

    public static final double K_SCORE_LOW       = 0.70;
    public static final double K_SCORE_MEDIUM    = 1.00;
    public static final double K_SCORE_HIGH      = 1.30;
    public static final double K_SCORE_VERY_HIGH = 1.60;

    // ===== K_сервиса (Блок 3, глава 6) =====

    public static final double K_SERVICE_DEFAULT          = 1.00;
    public static final double K_SERVICE_MONITORING_ON    = 0.85; // непрерывный мониторинг
    public static final double K_SERVICE_LIGHT_COMPLIANCE = 0.90; // комплаенс-аудит (Лайт)

    // ===== K_внеш (внешняя экспозиция, OSINT) =====

    public static final double K_EXPOSURE_NONE      = 1.00; // утечек нет
    public static final double K_EXPOSURE_OLD       = 1.50; // единичные устаревшие
    public static final double K_EXPOSURE_FRESH     = 2.00; // подтверждённые свежие утечки

    // ===== K_аудита (независимый внешний аудит) =====

    public static final double K_AUDIT_PRESENT = 0.90; // действующее заключение
    public static final double K_AUDIT_ABSENT  = 1.00; // нет аудита

    // ===== Индекс киберстрахуемости (CII) =====

    /** Порог CII, ниже которого клиент уходит на индивидуальное рассмотрение. */
    public static final double CII_THRESHOLD_INDIVIDUAL_REVIEW = 5.0;

    /** Максимальное значение каждой из четырёх частных оценок CII. */
    public static final double CII_COMPONENT_MAX = 10.0;
}