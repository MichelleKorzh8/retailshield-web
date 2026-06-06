package ru.finuniv.retailshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.data.QuestionnaireData;
import ru.finuniv.retailshield.data.StopFactorsData;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.RiskClass;
import ru.finuniv.retailshield.model.Section;
import ru.finuniv.retailshield.model.StopFactor;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный оркестратор оценки. Контроллер передаёт в этот сервис:
 *   — заполненную Company,
 *   — заполненные универсальные разделы (А-Д),
 *   — заполненный отраслевой раздел Е (или null для не-ритейла),
 *   — список стоп-факторов с проставленными compliant,
 *   — опциональный предыдущий скор (для индекса эволюции).
 *
 * Сервис прогоняет все этапы методологии в правильном порядке и
 * возвращает заполненный AssessmentResult.
 *
 * Порядок шагов:
 *   1. Применяем правила согласованности (А.26) → K_неопр.
 *   2. Считаем нормализованные скоры разделов.
 *   3. Считаем Score_total с K_неопр и отсечкой по 10.
 *   4. Считаем IndustryFactor с учётом раздела Е.
 *   5. Применяем стоп-фактор S7 (объективная оценка недопустимых событий).
 *   6. Классифицируем по классу риска с учётом семи стоп-факторов.
 *   7. Считаем индекс киберстрахуемости (CII).
 *   8. Считаем FAIR + CascadeFactor.
 *   9. Считаем BI.
 *  10. Считаем премию со всеми коэффициентами.
 *  11. Считаем Loss Ratio.
 *  12. Запускаем сценарный анализ (для ритейла).
 *  13. Считаем индекс эволюции.
 */
@Service
public class AssessmentService {

    private final ConsistencyService consistencyService;
    private final ScoringService scoringService;
    private final RiskClassifier riskClassifier;
    private final InsurabilityService insurabilityService;
    private final FairService fairService;
    private final BusinessInterruptionService biService;
    private final PremiumService premiumService;
    private final LossRatioService lossRatioService;
    private final ScenarioAnalysisService scenarioService;
    private final EvolutionService evolutionService;

    @Autowired
    public AssessmentService(ConsistencyService consistencyService,
                             ScoringService scoringService,
                             RiskClassifier riskClassifier,
                             InsurabilityService insurabilityService,
                             FairService fairService,
                             BusinessInterruptionService biService,
                             PremiumService premiumService,
                             LossRatioService lossRatioService,
                             ScenarioAnalysisService scenarioService,
                             EvolutionService evolutionService) {
        this.consistencyService = consistencyService;
        this.scoringService = scoringService;
        this.riskClassifier = riskClassifier;
        this.insurabilityService = insurabilityService;
        this.fairService = fairService;
        this.biService = biService;
        this.premiumService = premiumService;
        this.lossRatioService = lossRatioService;
        this.scenarioService = scenarioService;
        this.evolutionService = evolutionService;
    }

    /**
     * Полный расчёт оценки клиента.
     *
     * @param company           заполненная компания
     * @param universalSections заполненные универсальные разделы А-Д
     * @param retailSection     заполненный отраслевой раздел Е (или null)
     * @param stopFactors       стоп-факторы с проставленными compliant
     * @param previousScore     предыдущий Score_total для индекса эволюции (или null)
     * @return AssessmentResult
     */
    public AssessmentResult evaluate(Company company,
                                     List<Section> universalSections,
                                     Section retailSection,
                                     List<StopFactor> stopFactors,
                                     Double previousScore) {

        AssessmentResult result = new AssessmentResult();
        result.setCompany(company);

        // === 1. Правила согласованности и K_неопр ===
        List<Section> allSectionsForRules = collectAllSections(universalSections, retailSection);
        ConsistencyService.Outcome consistency = consistencyService.evaluate(allSectionsForRules);
        result.setKUncertainty(consistency.kUncertainty);
        result.getConsistencyWarnings().addAll(consistency.triggeredRules);

        // === 2. Скоры разделов и Score_total ===
        for (Section s : universalSections) {
            result.getSectionScores().put(s.getCode(), s.getNormalizedScore());
        }
        if (retailSection != null) {
            result.getSectionScores().put(retailSection.getCode(),
                    retailSection.getNormalizedScore());
        }

        double rawSum = scoringService.calculateRawSum(universalSections);
        double scoreTotal = scoringService.calculateTotalScore(universalSections,
                consistency.kUncertainty);
        result.setRawScoreTotal(rawSum);
        result.setScoreTotal(scoreTotal);

        // === 3. IndustryFactor ===
        double industryFactor = scoringService.calculateIndustryFactor(retailSection,
                company.getIndustry().getIndustryFactor());
        result.setIndustryFactor(industryFactor);

        // === 4. Стоп-фактор S7 (объективная оценка недопустимых событий) ===
        riskClassifier.applyS7(stopFactors, allSectionsForRules);

        // === 5. Класс риска ===
        RiskClass riskClass = riskClassifier.classify(scoreTotal, stopFactors);
        result.setRiskClass(riskClass);

        StopFactor firstViolation = riskClassifier.findFirstViolation(stopFactors);
        if (firstViolation != null) {
            result.setStopFactorTriggered(true);
            result.setStopFactorReason(firstViolation.getId() + ": " + firstViolation.getText());
        }

        // === 6. Индекс киберстрахуемости ===
        insurabilityService.calculate(result, allSectionsForRules, stopFactors, company);

        // === 7. FAIR + CascadeFactor ===
        fairService.calculate(result, company);

        // === 8. BI ===
        biService.calculate(result, company);

        // === 9. Премия ===
        premiumService.calculate(result, company);
        result.setSizeFactor(company.getSizeFactor());

        // === 10. Loss Ratio ===
        lossRatioService.calculate(result);

        // === 11. Сценарный анализ (для ритейла) ===
        scenarioService.analyze(result, universalSections);

        // === 12. Индекс эволюции ===
        EvolutionService.Outcome evolution = evolutionService.compare(previousScore, scoreTotal);
        result.setPreviousScoreTotal(evolution.previousScore);
        result.setDeltaScore(evolution.deltaScore);

        return result;
    }

    private List<Section> collectAllSections(List<Section> universal, Section retail) {
        List<Section> all = new ArrayList<>(universal);
        if (retail != null) {
            all.add(retail);
        }
        return all;
    }

    /** Удобный метод для контроллеров: возвращает свежую анкету для новой сессии. */
    public List<Section> freshUniversalSections() {
        return QuestionnaireData.buildUniversalSections();
    }

    /** Удобный метод: свежий список стоп-факторов для новой сессии. */
    public List<StopFactor> freshStopFactors() {
        return StopFactorsData.getAll();
    }
}