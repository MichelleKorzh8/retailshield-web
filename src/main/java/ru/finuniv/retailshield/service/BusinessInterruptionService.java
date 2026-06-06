package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.BusinessInterruption;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.Industry;

/**
 * Сервис расчёта убытка от перерыва деятельности (Блок 3, раздел 3.1 — правка 9).
 *
 * Базовая формула:
 *     BI = (M_день + FC_день) × T_простоя × k_охвата
 *
 * Если клиент не предоставил собственные показатели восстановления,
 * применяется отраслевой ориентир, который рассчитывается из выручки
 * клиента и типичных значений по сегменту. В этом случае флаг
 * biUsedIndustryEstimate = true и в финальной премии должен применяться
 * повышающий коэффициент (учитывается на стороне PremiumService).
 */
@Service
public class BusinessInterruptionService {

    public void calculate(AssessmentResult result, Company company) {
        BusinessInterruption bi = company.getBusinessInterruption();

        if (bi != null) {
            // Расчёт по фактическим данным клиента
            double loss = bi.calculate();
            result.setBusinessInterruptionLoss(loss);
            result.setBiUsedIndustryEstimate(bi.isIndustryEstimate());
        } else {
            // Клиент не предоставил данные — отраслевой ориентир
            double loss = estimateByIndustry(company);
            result.setBusinessInterruptionLoss(loss);
            result.setBiUsedIndustryEstimate(true);
        }
    }

    /**
     * Отраслевой ориентир BI по выручке.
     *
     * Предположения для типичного ритейлера:
     *   — дневная выручка = годовая / 365,
     *   — маржинальная прибыль ~ 15% от выручки (M_день),
     *   — постоянные расходы ~ 25% от выручки (FC_день),
     *   — типовое время простоя по сегменту: 1–3 дня,
     *   — k_охвата = 0,7 (большинство клиентов затронуто частично, но не полностью).
     *
     * Эти доли подкорректированы для разных типов ритейла, где доля онлайн
     * выручки и время восстановления различаются.
     */
    private double estimateByIndustry(Company company) {
        double annualRevenue = company.getAnnualRevenueRub();
        double dailyRevenue = annualRevenue / 365.0;

        double marginalShare;
        double fixedShare;
        double typicalDowntimeDays;
        double coverageShare;

        Industry ind = company.getIndustry();
        if (ind == Industry.RETAIL_OFFLINE) {
            marginalShare = 0.18;
            fixedShare = 0.28;
            typicalDowntimeDays = 1.5;
            coverageShare = 0.6;
        } else if (ind == Industry.RETAIL_ECOMMERCE) {
            marginalShare = 0.15;
            fixedShare = 0.22;
            typicalDowntimeDays = 2.0;
            coverageShare = 0.85;
        } else if (ind == Industry.RETAIL_MARKETPLACE) {
            marginalShare = 0.12;
            fixedShare = 0.20;
            typicalDowntimeDays = 2.5;
            coverageShare = 0.9;
        } else {
            // отрасли вне семейства ритейла — обобщённые средние значения
            marginalShare = 0.15;
            fixedShare = 0.25;
            typicalDowntimeDays = 2.0;
            coverageShare = 0.7;
        }

        double mDay = dailyRevenue * marginalShare;
        double fcDay = dailyRevenue * fixedShare;
        return (mDay + fcDay) * typicalDowntimeDays * coverageShare;
    }
}