package ru.finuniv.retailshield.model;

/**
 * Данные клиента для расчёта убытка от перерыва деятельности (Блок 3, п. 3.1):
 *     BI = (M_день + FC_день) × T_простоя × k_охвата,
 * где M_день — недополученная маржинальная прибыль за день,
 *     FC_день — постоянные расходы за день,
 *     T_простоя — фактическое время простоя в днях,
 *     k_охвата — доля затронутого сервиса в выручке (0..1).
 *
 * Если клиент не отслеживает собственные показатели — применяется
 * отраслевой ориентир и повышающий коэффициент к премии.
 */
public class BusinessInterruption {

    private final double dailyMarginalProfit;
    private final double dailyFixedCosts;
    private final double downtimeDays;
    private final double coverageShare;
    private final boolean industryEstimate;

    public BusinessInterruption(double dailyMarginalProfit,
                                double dailyFixedCosts,
                                double downtimeDays,
                                double coverageShare,
                                boolean industryEstimate) {
        this.dailyMarginalProfit = dailyMarginalProfit;
        this.dailyFixedCosts = dailyFixedCosts;
        this.downtimeDays = downtimeDays;
        this.coverageShare = clamp01(coverageShare);
        this.industryEstimate = industryEstimate;
    }

    public double getDailyMarginalProfit() { return dailyMarginalProfit; }
    public double getDailyFixedCosts() { return dailyFixedCosts; }
    public double getDowntimeDays() { return downtimeDays; }
    public double getCoverageShare() { return coverageShare; }

    /** true, если показатели взяты по отраслевому ориентиру (а не из данных клиента). */
    public boolean isIndustryEstimate() { return industryEstimate; }

    /** Расчёт BI по базовой формуле. */
    public double calculate() {
        return (dailyMarginalProfit + dailyFixedCosts) * downtimeDays * coverageShare;
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}