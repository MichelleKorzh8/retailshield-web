package ru.finuniv.retailshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.data.Coefficients;
import ru.finuniv.retailshield.data.RiskMapLoader;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.AssessmentResult.ScenarioOutcome;
import ru.finuniv.retailshield.model.AssessmentResult.ThreatContribution;
import ru.finuniv.retailshield.model.RiskMap;
import ru.finuniv.retailshield.model.ScenarioWeights;
import ru.finuniv.retailshield.model.Section;
import ru.finuniv.retailshield.model.Threat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Сценарный анализ риска (Блок 1, раздел 9 и Блок 2, глава 9).
 *
 * Для каждого из шести сценариев пересчитывает риск-скор клиента с использованием
 * специфических для сценария весов разделов из retail_risk_map.json. Затем
 * рассчитывает ALE по угрозам, привязанным к этому сценарию через поле scenario.
 *
 * Результаты не дублируют основной Score_total: они показывают, как клиент
 * выглядит относительно конкретного типа инцидента. Используется страховщиком
 * при формировании сублимитов и индивидуализации покрытия.
 */
@Service
public class ScenarioAnalysisService {

    private final RiskMapLoader riskMapLoader;

    @Autowired
    public ScenarioAnalysisService(RiskMapLoader riskMapLoader) {
        this.riskMapLoader = riskMapLoader;
    }

    /**
     * Запускает сценарный анализ для всех шести сценариев. Заполняет поле
     * scenarios в AssessmentResult. Применяется только для клиентов из ритейла,
     * для других отраслей метод возвращает пустую карту.
     */
    public void analyze(AssessmentResult result, List<Section> universalSections) {
        if (result.getCompany() == null
                || !result.getCompany().getIndustry().isRetailFamily()) {
            return;
        }

        RiskMap riskMap = riskMapLoader.getMap();
        Map<String, ScenarioOutcome> outcomes = new LinkedHashMap<>();

        for (Map.Entry<String, ScenarioWeights> e : riskMap.getScenarioWeights().entrySet()) {
            String code = e.getKey();
            ScenarioWeights weights = e.getValue();
            outcomes.put(code, analyzeOne(code, weights, universalSections, riskMap));
        }
        result.setScenarios(outcomes);
    }

    private ScenarioOutcome analyzeOne(String code, ScenarioWeights weights,
                                       List<Section> sections, RiskMap riskMap) {

        // 1. Пересчитанный риск-скор с весами этого сценария
        double scenarioScore = 0.0;
        for (Section s : sections) {
            scenarioScore += s.getNormalizedScore() * weights.weightFor(s.getCode());
        }
        scenarioScore = Math.max(0.0, scenarioScore);
        scenarioScore = Math.min(Coefficients.MAX_SCORE, scenarioScore);

        double v = scenarioScore / Coefficients.MAX_SCORE;

        // 2. Сумма ALE по всем угрозам этого сценария
        double totalAle = 0.0;
        ScenarioOutcome outcome = new ScenarioOutcome();
        outcome.code = code;
        outcome.name = weights.getName();
        outcome.description = weights.getDescription();
        outcome.scenarioScore = scenarioScore;
        outcome.vulnerability = v;

        for (Threat t : riskMap.getThreats()) {
            if (code.equals(t.getScenario())) {
                double ale = t.getTef() * v * t.getLossAverage();
                ThreatContribution tc = new ThreatContribution();
                tc.code = t.getCode();
                tc.name = t.getName();
                tc.ale = ale;
                outcome.threats.add(tc);
                totalAle += ale;
            }
        }
        outcome.totalAle = totalAle;
        return outcome;
    }
}