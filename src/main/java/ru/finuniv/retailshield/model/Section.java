package ru.finuniv.retailshield.model;

import java.util.List;

/**
 * Раздел анкеты: А, Б, В, Г, Д (универсальные) и Е (отраслевой для ритейла).
 * Хранит код раздела, заголовок, вес в формуле Score_total и список вопросов.
 */
public class Section {

    private final String code;
    private final String title;
    private final double weight;
    private final List<Question> questions;

    public Section(String code, String title, double weight, List<Question> questions) {
        this.code = code;
        this.title = title;
        this.weight = weight;
        this.questions = questions;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public double getWeight() { return weight; }
    public List<Question> getQuestions() { return questions; }

    /**
     * Нормализованный скор раздела (Блок 1, формула 7.2):
     *     S_раздела = Σ(балл × вес) / Σ(весов)
     * Возвращает значение в диапазоне 0..10.
     */
    public double getNormalizedScore() {
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (Question q : questions) {
            if (q.isAnswered()) {
                weightedSum += q.getSelectedScore() * q.getWeight();
                weightTotal += q.getWeight();
            }
        }
        return weightTotal == 0.0 ? 0.0 : weightedSum / weightTotal;
    }

    public boolean isFullyAnswered() {
        return questions.stream().allMatch(Question::isAnswered);
    }
}