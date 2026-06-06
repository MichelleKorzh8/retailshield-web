package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;

/**
 * Сервис расчёта индекса эволюции риска (Блок 1, раздел 10.4):
 *     ΔScore = Score_total(t2) − Score_total(t1).
 *
 * Положительный ΔScore — защищённость ухудшилась, премия должна расти.
 * Отрицательный — защищённость улучшилась, премия может снизиться при продлении.
 *
 * Используется при динамическом ценообразовании на продлении полиса
 * (Блок 3, раздел 6, последний абзац о динамическом ценообразовании).
 */
@Service
public class EvolutionService {

    public static class Outcome {
        public final Double previousScore;
        public final Double deltaScore;
        public final String interpretation;

        public Outcome(Double previousScore, Double deltaScore, String interpretation) {
            this.previousScore = previousScore;
            this.deltaScore = deltaScore;
            this.interpretation = interpretation;
        }
    }

    /**
     * Возвращает результат сравнения. Если previousScore null — это первая оценка
     * клиента, ΔScore не определена, динамическое ценообразование не применяется.
     */
    public Outcome compare(Double previousScore, double currentScore) {
        if (previousScore == null) {
            return new Outcome(null, null,
                    "Первая оценка клиента. Индекс эволюции будет рассчитан при продлении полиса.");
        }
        double delta = currentScore - previousScore;
        String interpretation;
        if (delta > 0.5) {
            interpretation = "Защищённость существенно ухудшилась — при продлении применяется надбавка к премии.";
        } else if (delta > 0.1) {
            interpretation = "Защищённость незначительно снизилась — премия пересчитывается.";
        } else if (delta < -0.5) {
            interpretation = "Защищённость существенно улучшилась — клиент получает скидку при продлении.";
        } else if (delta < -0.1) {
            interpretation = "Защищённость улучшилась — премия может быть снижена.";
        } else {
            interpretation = "Защищённость стабильна — премия сохраняется на прежнем уровне.";
        }
        return new Outcome(previousScore, delta, interpretation);
    }
}