package ru.finuniv.retailshield.model;

/**
 * Тарифные пакеты продукта «Ритейл.Щит Актив» (Блок 3, глава 1).
 * Базовая ставка T соответствует разделу 6 Блока 3 (1,0% / 1,2% / 1,5% / 1,8%).
 */
public enum PackageType {

    LIGHT("Лайт",
            0.010,
            "Регуляторный фокус: юридическая защита при утечках ПДн, "
                    + "комплаенс-аудит. Подходит для бизнеса с доминирующим "
                    + "регуляторным риском (до 1 млрд ₽ выручки, малая доля онлайн).",
            "До 5 млн ₽",
            true),

    BASIC("Базовый",
            0.012,
            "Базовое покрытие собственных убытков и ответственности перед третьими "
                    + "лицами. Подходит для небольших ритейлеров с типовой инфраструктурой.",
            "До 30 млн ₽",
            false),

    STANDARD("Стандарт",
            0.015,
            "Полное покрытие: собственные убытки, ответственность, репутация, "
                    + "юридическая защита, базовый сервисный пакет. Подходит для "
                    + "средних ритейлеров и маркетплейсов.",
            "До 100 млн ₽",
            false),

    PREMIUM("Премиум",
            0.018,
            "Максимальное покрытие с расширенными сублимитами, непрерывный "
                    + "мониторинг, параметрический аванс, кризисные коммуникации. "
                    + "Для крупных ритейлеров и маркетплейсов с высокой зависимостью "
                    + "от цифровых сервисов.",
            "До 500 млн ₽",
            false);

    private final String label;
    private final double baseRate;
    private final String description;
    private final String insuredSumRange;
    private final boolean regulatoryFocus;

    PackageType(String label, double baseRate, String description,
                String insuredSumRange, boolean regulatoryFocus) {
        this.label = label;
        this.baseRate = baseRate;
        this.description = description;
        this.insuredSumRange = insuredSumRange;
        this.regulatoryFocus = regulatoryFocus;
    }

    public String getLabel() { return label; }
    public double getBaseRate() { return baseRate; }
    public String getDescription() { return description; }
    public String getInsuredSumRange() { return insuredSumRange; }
    public boolean isRegulatoryFocus() { return regulatoryFocus; }

    @Override
    public String toString() {
        return label + " (T = " + (baseRate * 100) + "%)";
    }
}