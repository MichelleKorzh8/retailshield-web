package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.data.Coefficients;
import ru.finuniv.retailshield.model.Section;

import java.util.List;

/**
 * Сервис расчёта итогового базового риск-скора (Блок 1, раздел 7.2).
 *
 * Формула с учётом правки 13:
 *     Score_total = min(10; K_неопр × max(0; Σ S_раздела × вес))
 *
 * Логика:
 *  1. Каждый раздел уже знает свой нормализованный скор (Section.getNormalizedScore).
 *  2. Здесь суммируется взвешенно: 0,10·А + 0,30·Б + 0,15·В + 0,20·Г + 0,25·Д.
 *  3. Отрицательная сумма (за счёт бонусных ответов с отрицательными баллами,
 *     например А9 о наличии действующего полиса) обнуляется через max(0;...).
 *  4. Результат домножается на K_неопр и обрезается верхней границей шкалы.
 */
@Service
public class ScoringService {

    /** Итоговый Score_total с применением K_неопр и отсечкой по 10. */
    public double calculateTotalScore(List<Section> sections, double kUncertainty) {
        double rawSum = calculateRawSum(sections);
        double floored = Math.max(0.0, rawSum);
        double adjusted = kUncertainty * floored;
        return Math.min(Coefficients.MAX_SCORE, adjusted);
    }

    /** Сырая взвешенная сумма до применения K_неопр (для отображения «до/после» в отчёте). */
    public double calculateRawSum(List<Section> sections) {
        double sum = 0.0;
        for (Section s : sections) {
            sum += s.getNormalizedScore() * s.getWeight();
        }
        return sum;
    }

    /**
     * IndustryFactor с учётом отраслевой части (раздел Е) при её наличии.
     * Чем выше отраслевой скор раздела Е, тем выше итоговый множитель,
     * но не более 1,60. Базовое значение — из Industry.
     */
    public double calculateIndustryFactor(Section retailSection, double baseIndustryFactor) {
        if (retailSection == null || !retailSection.isFullyAnswered()) {
            return baseIndustryFactor;
        }
        double sectionScore = retailSection.getNormalizedScore();
        double adjusted = baseIndustryFactor + (sectionScore / Coefficients.MAX_SCORE) * 0.35;
        return Math.min(1.60, adjusted);
    }
}