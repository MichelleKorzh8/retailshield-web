package ru.finuniv.retailshield.model;

/**
 * Критерий отказа в страховании (Блок 1, глава 12, таблица А.23).
 *
 * После пересмотра методологии (правка 5) все семь критериев являются
 * жёсткими: их срабатывание означает безусловный отказ.
 *
 * Прежние «условные» критерии (отказ от аудита, отсутствие выделенного ИБ,
 * утечки в OSINT, сокрытие инцидентов) вынесены в коэффициенты премии
 * и баллы анкеты — соответственно K_аудита, баллы раздела В,
 * K_внеш, ст. 944 ГК РФ на стадии урегулирования.
 */
public class StopFactor {

    private final String id;
    private final String text;
    private final String rationale;
    private boolean compliant = true;

    public StopFactor(String id, String text, String rationale) {
        this.id = id;
        this.text = text;
        this.rationale = rationale;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getRationale() { return rationale; }

    public boolean isCompliant() { return compliant; }

    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }
}