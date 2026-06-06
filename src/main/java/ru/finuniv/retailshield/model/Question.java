package ru.finuniv.retailshield.model;

import java.util.List;

/**
 * Вопрос анкеты Блока 1 или Блока 2 (отраслевой раздел Е).
 * Содержит идентификатор (А1, Б3, Г2 и т.д.), вес внутри раздела (1,0 обычный, 2,0 критический),
 * список вариантов и текущий выбранный индекс.
 */
public class Question {

    private final String id;
    private final String text;
    private final double weight;
    private final boolean critical;
    private final List<AnswerOption> options;

    /**
     * Группа критического условия восстановимости и мониторинга для индекса
     * киберстрахуемости (CII, правка 12 и 13). Используется для вопросов
     * Г2 (резервные копии), Г3 (тестирование восстановления),
     * Г5 (RTO критических сервисов), Д5 (SOC).
     * Может быть null для прочих вопросов.
     */
    private final String manageabilityGroup;

    private int selectedIndex = -1;

    public Question(String id, String text, double weight, boolean critical,
                    List<AnswerOption> options) {
        this(id, text, weight, critical, options, null);
    }

    public Question(String id, String text, double weight, boolean critical,
                    List<AnswerOption> options, String manageabilityGroup) {
        this.id = id;
        this.text = text;
        this.weight = weight;
        this.critical = critical;
        this.options = options;
        this.manageabilityGroup = manageabilityGroup;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public double getWeight() { return weight; }
    public boolean isCritical() { return critical; }
    public List<AnswerOption> getOptions() { return options; }
    public String getManageabilityGroup() { return manageabilityGroup; }

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public boolean isAnswered() {
        return selectedIndex >= 0 && selectedIndex < options.size();
    }

    public double getSelectedScore() {
        return isAnswered() ? options.get(selectedIndex).getScore() : 0.0;
    }

    /**
     * Возвращает true, если выбранный вариант помечен как «заявлено, но не подтверждено».
     * Используется ConsistencyService для подсчёта K_неопр.
     */
    public boolean isSelectionUncertain() {
        return isAnswered() && options.get(selectedIndex).isUncertain();
    }

    /**
     * Возвращает true, если выбранный вариант означает выполнение защитной меры
     * (по соглашению — балл ≤ 3,0). Используется при расчёте компонента
     * «управляемость» индекса киберстрахуемости.
     */
    public boolean isSelectionCompliant() {
        return isAnswered() && getSelectedScore() <= 3.0;
    }
}