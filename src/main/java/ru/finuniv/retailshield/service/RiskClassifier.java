package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.model.Question;
import ru.finuniv.retailshield.model.RiskClass;
import ru.finuniv.retailshield.model.Section;
import ru.finuniv.retailshield.model.StopFactor;

import java.util.List;

/**
 * Классификатор риска по итоговому Score_total с проверкой семи стоп-факторов
 * из новой главы 12 Блока 1.
 *
 * После правки 5 все семь стоп-факторов — жёсткие. Срабатывание любого из них
 * означает безусловный отказ (класс Critical), независимо от значения скора.
 *
 * Стоп-фактор S7 имеет специфическую логику: он проверяет, можно ли объективно
 * оценить недопустимые бизнес-события клиента. Срабатывает, когда среди ответов
 * на критические вопросы восстановимости и мониторинга (Г2, Г3, Г5, Д5) есть
 * варианты в режиме неопределённости или вопрос вообще остался без ответа.
 */
@Service
public class RiskClassifier {

    public boolean hasViolation(List<StopFactor> stopFactors) {
        return stopFactors.stream().anyMatch(sf -> !sf.isCompliant());
    }

    public StopFactor findFirstViolation(List<StopFactor> stopFactors) {
        return stopFactors.stream()
                .filter(sf -> !sf.isCompliant())
                .findFirst()
                .orElse(null);
    }

    public RiskClass classify(double scoreTotal, List<StopFactor> stopFactors) {
        if (hasViolation(stopFactors)) {
            return RiskClass.CRITICAL;
        }
        return RiskClass.byScore(scoreTotal);
    }

    /**
     * Проверяет специальный стоп-фактор S7 (невозможность объективной оценки
     * недопустимых бизнес-событий). Срабатывает, если хотя бы один вопрос
     * с manageabilityGroup ∈ {RECOVERABILITY, MONITORING} либо не отвечен,
     * либо выбран вариант в режиме неопределённости.
     */
    public boolean isCriticalEventsAssessable(List<Section> sections) {
        for (Section s : sections) {
            for (Question q : s.getQuestions()) {
                if (q.getManageabilityGroup() == null) continue;
                if (!q.isAnswered()) return false;
                if (q.isSelectionUncertain()) return false;
            }
        }
        return true;
    }

    /**
     * Удобный метод: применяет результат isCriticalEventsAssessable к
     * соответствующему стоп-фактору в списке (тот, у которого id = "S7").
     */
    public void applyS7(List<StopFactor> stopFactors, List<Section> sections) {
        boolean assessable = isCriticalEventsAssessable(sections);
        for (StopFactor sf : stopFactors) {
            if ("S7".equals(sf.getId())) {
                sf.setCompliant(assessable);
                return;
            }
        }
    }
}