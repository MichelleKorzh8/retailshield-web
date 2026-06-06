package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.data.Coefficients;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.ContractorDependency;

/**
 * Количественная оценка ущерба по модели FAIR (Блок 1, раздел 8) с учётом
 * каскадного эффекта риска (Блок 1, раздел 10.3 — правка 4).
 *
 * Базовые формулы:
 *     V   = Score_total / Max_Score
 *     LEF = TEF × V
 *     ALE = LEF × LM
 *
 * Каскадная корректировка:
 *     CascadeFactor   = 1 + Σ (Dependency_i × Probability_i)
 *     ALE_cascade     = ALE × CascadeFactor
 *
 * Dependency и Probability собираются с экрана данных компании (список
 * подрядчиков с долями зависимости и вероятностями компрометации).
 * Если клиент не указал подрядчиков, CascadeFactor = 1,00.
 */
@Service
public class FairService {

    public void calculate(AssessmentResult result, Company company) {
        // TEF: для ритейла — из Industry, для других отраслей тоже из Industry
        double tef = company.getIndustry().getTef();

        // V = Score_total / 10
        double v = result.getScoreTotal() / Coefficients.MAX_SCORE;

        // LEF и базовый ALE
        double lef = tef * v;
        double lm = company.getInsuredSum();
        double ale = lef * lm;

        // CascadeFactor: 1 + сумма произведений
        double cascadeFactor = calculateCascadeFactor(company);
        double aleCascade = ale * cascadeFactor;

        // Записываем всё в результат
        result.setTef(tef);
        result.setVulnerability(v);
        result.setLef(lef);
        result.setLossMagnitude(lm);
        result.setAle(ale);
        result.setCascadeFactor(cascadeFactor);
        result.setAleCascade(aleCascade);
    }

    /**
     * CascadeFactor = 1 + Σ (Dependency_i × Probability_i).
     * Зажимается сверху значением 3,0, чтобы избежать неадекватного раздувания
     * ALE при некорректных входных данных (например, клиент указал десять
     * подрядчиков с зависимостью 1,0 и вероятностью 1,0).
     */
    public double calculateCascadeFactor(Company company) {
        double contributions = 0.0;
        for (ContractorDependency d : company.getDependencies()) {
            contributions += d.getContribution();
        }
        double cascade = 1.0 + contributions;
        return Math.min(3.0, cascade);
    }
}