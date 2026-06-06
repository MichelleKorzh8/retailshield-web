package ru.finuniv.retailshield.model;

/**
 * Вариант ответа на вопрос анкеты.
 *
 * Поле uncertain — поддержка правки K_неопр (правка 2, Блок 1 п. 7.2):
 * если вариант помечен как unconfirmed/uncertain, то выбор этого варианта
 * добавляет к набору флагов неопределённости один элемент, и итоговый
 * Score_total получает повышающий коэффициент K_неопр (1,10–1,20).
 */
public class AnswerOption {

    private final String text;
    private final double score;
    private final boolean uncertain;

    public AnswerOption(String text, double score) {
        this(text, score, false);
    }

    public AnswerOption(String text, double score, boolean uncertain) {
        this.text = text;
        this.score = score;
        this.uncertain = uncertain;
    }

    public String getText() {
        return text;
    }

    public double getScore() {
        return score;
    }

    public boolean isUncertain() {
        return uncertain;
    }

    /** Фабрика для подтверждённой меры (нет режима неопределённости). */
    public static AnswerOption confirmed(String text, double score) {
        return new AnswerOption(text, score, false);
    }

    /** Фабрика для меры, заявленной без подтверждения (попадает в K_неопр). */
    public static AnswerOption unconfirmed(String text, double score) {
        return new AnswerOption(text, score, true);
    }
}