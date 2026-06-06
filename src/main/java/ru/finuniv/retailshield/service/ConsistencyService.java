package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.data.Coefficients;
import ru.finuniv.retailshield.data.ConsistencyRules;
import ru.finuniv.retailshield.data.ConsistencyRules.Level;
import ru.finuniv.retailshield.data.ConsistencyRules.Rule;
import ru.finuniv.retailshield.data.ConsistencyRules.Trigger;
import ru.finuniv.retailshield.model.Question;
import ru.finuniv.retailshield.model.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис применения правил согласованности (Блок 1, раздел 6.1, таблица А.26).
 * Реализация правок 2 и 13.
 *
 * По заполненной анкете возвращает:
 *  — K_неопр в диапазоне [1,00; 1,20];
 *  — флаг наличия жёсткой блокировки (HARD_BLOCK);
 *  — список текстовых описаний сработавших правил для отчёта.
 *
 * K_неопр считается так: за каждое сработавшее правило уровня UNCERTAINTY
 * к 1,00 прибавляется uncertaintyDelta правила, затем результат обрезается
 * по нижней и верхней границам.
 */
@Service
public class ConsistencyService {

    public static class Outcome {
        public final double kUncertainty;
        public final boolean hardBlocked;
        public final List<String> triggeredRules;

        public Outcome(double kUncertainty, boolean hardBlocked, List<String> triggeredRules) {
            this.kUncertainty = kUncertainty;
            this.hardBlocked = hardBlocked;
            this.triggeredRules = triggeredRules;
        }
    }

    public Outcome evaluate(List<Section> sections) {
        // 1. Строим карту "id вопроса → выбранный индекс" для быстрого поиска
        Map<String, Integer> selectedIndexByQuestion = new HashMap<>();
        for (Section s : sections) {
            for (Question q : s.getQuestions()) {
                if (q.isAnswered()) {
                    selectedIndexByQuestion.put(q.getId(), q.getSelectedIndex());
                }
            }
        }

        // 2. Проходим по правилам
        double kUncertainty = Coefficients.K_UNCERTAINTY_BASE;
        boolean hardBlocked = false;
        List<String> triggered = new ArrayList<>();

        for (Rule rule : ConsistencyRules.getAll()) {
            if (!ruleMatches(rule, selectedIndexByQuestion)) {
                continue;
            }
            triggered.add(rule.id() + ": " + rule.description());

            if (rule.level() == Level.HARD_BLOCK) {
                hardBlocked = true;
            } else if (rule.level() == Level.UNCERTAINTY) {
                kUncertainty += rule.uncertaintyDelta();
            }
            // CONFIRMATION в текущей реализации только фиксируется в списке
            // (выводится пользователю на экране результатов как уточняющий комментарий)
        }

        // 3. Зажимаем K_неопр в допустимый диапазон
        if (kUncertainty > Coefficients.K_UNCERTAINTY_BASE) {
            kUncertainty = Math.max(Coefficients.K_UNCERTAINTY_MIN,
                    Math.min(Coefficients.K_UNCERTAINTY_MAX, kUncertainty));
        }

        return new Outcome(kUncertainty, hardBlocked, triggered);
    }

    /** Правило срабатывает, когда ВСЕ его триггеры активны на ответах клиента. */
    private boolean ruleMatches(Rule rule, Map<String, Integer> selected) {
        for (Trigger t : rule.triggers()) {
            Integer chosen = selected.get(t.questionId());
            if (chosen == null) return false;
            if (!t.selectedOptionIndexes().contains(chosen)) return false;
        }
        return true;
    }
}