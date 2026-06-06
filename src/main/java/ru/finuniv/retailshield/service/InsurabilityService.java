package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.data.Coefficients;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.Question;
import ru.finuniv.retailshield.model.Section;
import ru.finuniv.retailshield.model.StopFactor;

import java.util.List;

/**
 * Cyber Insurability Index — индекс киберстрахуемости (Блок 1, раздел 10.1).
 * Реализация правок 3, 12, 13.
 *
 * Индекс — среднее четырёх частных оценок (каждая 0..10):
 *
 *  1. Измеримость — доля ответов вне режима неопределённости (по UNCERTAINTY
 *     варианту АнкетыResponse).
 *  2. Прогнозируемость — есть ли отраслевой TEF и заполнены ли критические
 *     разделы.
 *  3. Управляемость — доля выполненных критических условий восстановимости
 *     и мониторинга по вопросам Г2, Г3, Г5, Д5 (manageabilityGroup).
 *  4. Тарифицируемость — 10, если стоп-факторов нет и класс риска не Critical,
 *     иначе 0.
 *
 * Если CII ниже порогового значения (см. Coefficients.CII_THRESHOLD_INDIVIDUAL_REVIEW),
 * клиент уходит на индивидуальное рассмотрение даже при приемлемом классе риска.
 */
@Service
public class InsurabilityService {

    public void calculate(AssessmentResult result,
                          List<Section> sections,
                          List<StopFactor> stopFactors,
                          Company company) {

        double measurability = calculateMeasurability(sections);
        double predictability = calculatePredictability(sections, company);
        double manageability = calculateManageability(sections);
        double insurability = calculateInsurability(result, stopFactors);

        double cii = (measurability + predictability + manageability + insurability) / 4.0;

        result.setCiiMeasurability(measurability);
        result.setCiiPredictability(predictability);
        result.setCiiManageability(manageability);
        result.setCiiInsurability(insurability);
        result.setCii(cii);
        result.setCiiBelowThreshold(cii < Coefficients.CII_THRESHOLD_INDIVIDUAL_REVIEW
                && result.getRiskClass() != null
                && result.getRiskClass().isAcceptable());
    }

    /**
     * Измеримость: доля ответов БЕЗ режима неопределённости в общем числе
     * отвеченных вопросов, переведённая в шкалу 0..10.
     */
    private double calculateMeasurability(List<Section> sections) {
        int total = 0;
        int certain = 0;
        for (Section s : sections) {
            for (Question q : s.getQuestions()) {
                if (q.isAnswered()) {
                    total++;
                    if (!q.isSelectionUncertain()) {
                        certain++;
                    }
                }
            }
        }
        if (total == 0) return 0.0;
        return Coefficients.CII_COMPONENT_MAX * ((double) certain / total);
    }

    /**
     * Прогнозируемость: 10, если у отрасли задан TEF (т.е. она в нашей карте)
     * и нет пропущенных критических разделов. За каждый отсутствующий
     * критический ответ снижается пропорционально.
     */
    private double calculatePredictability(List<Section> sections, Company company) {
        if (company.getIndustry() == null || company.getIndustry().getTef() <= 0.0) {
            return 0.0;
        }
        int criticalQuestions = 0;
        int criticalAnswered = 0;
        for (Section s : sections) {
            for (Question q : s.getQuestions()) {
                if (q.isCritical()) {
                    criticalQuestions++;
                    if (q.isAnswered()) criticalAnswered++;
                }
            }
        }
        if (criticalQuestions == 0) return Coefficients.CII_COMPONENT_MAX;
        return Coefficients.CII_COMPONENT_MAX * ((double) criticalAnswered / criticalQuestions);
    }

    /**
     * Управляемость: доля «выполненных» (compliant) ответов на вопросы
     * Г2, Г3, Г5, Д5 — те, у которых manageabilityGroup не null.
     * Это и есть расчётная реализация модели недопустимых событий.
     */
    private double calculateManageability(List<Section> sections) {
        int total = 0;
        int compliant = 0;
        for (Section s : sections) {
            for (Question q : s.getQuestions()) {
                if (q.getManageabilityGroup() == null) continue;
                total++;
                if (q.isSelectionCompliant()) {
                    compliant++;
                }
            }
        }
        if (total == 0) return 0.0;
        return Coefficients.CII_COMPONENT_MAX * ((double) compliant / total);
    }

    /**
     * Тарифицируемость: бинарная оценка — 10 при отсутствии стоп-факторов
     * и приемлемом классе риска, 0 при наличии стоп-факторов или классе Critical.
     */
    private double calculateInsurability(AssessmentResult result, List<StopFactor> stopFactors) {
        boolean anyStop = stopFactors.stream().anyMatch(sf -> !sf.isCompliant());
        if (anyStop) return 0.0;
        if (result.getRiskClass() == null) return 0.0;
        return result.getRiskClass().isAcceptable() ? Coefficients.CII_COMPONENT_MAX : 0.0;
    }
}