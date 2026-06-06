package ru.finuniv.retailshield.model;

/**
 * Отрасль компании-страхователя.
 *
 * Три типа ритейла (правка 6, Блок 2 п. 1.1):
 *   RETAIL_OFFLINE — сетевая розничная торговля (офлайн-сети, омниканальные ритейлеры),
 *   RETAIL_ECOMMERCE — электронная коммерция (онлайн-магазины),
 *   RETAIL_MARKETPLACE — мультивендорные торговые платформы.
 *
 * Все три используют одну и ту же отраслевую анкету (раздел Е), но получают
 * разные базовые TEF и IndustryFactor.
 */
public enum Industry {

    // === Целевые отрасли продукта ===
    RETAIL_OFFLINE("Сетевая розничная торговля", 1.25, 0.17, true),
    RETAIL_ECOMMERCE("Электронная коммерция (онлайн-магазин)", 1.25, 0.22, true),
    RETAIL_MARKETPLACE("Маркетплейс (мультивендорная платформа)", 1.25, 0.23, true),

    // === Прочие отрасли ===
    FINANCE("Финансы и страхование", 1.60, 0.25, false),
    LOGISTICS("Логистика и транспорт", 1.20, 0.20, false),
    MANUFACTURING("Промышленность", 1.30, 0.18, false),
    HEALTHCARE("Здравоохранение", 1.40, 0.22, false),
    EDUCATION("Образование", 1.00, 0.06, false),
    OTHER("Иная отрасль", 1.10, 0.15, false);

    private final String label;
    private final double industryFactor;
    private final double tef;
    private final boolean retailFamily;

    Industry(String label, double industryFactor, double tef, boolean retailFamily) {
        this.label = label;
        this.industryFactor = industryFactor;
        this.tef = tef;
        this.retailFamily = retailFamily;
    }

    public String getLabel() {
        return label;
    }

    public double getIndustryFactor() {
        return industryFactor;
    }

    public double getTef() {
        return tef;
    }

    /** true для всех трёх типов ритейла — для них применяется раздел Е и карта рисков ритейла. */
    public boolean isRetailFamily() {
        return retailFamily;
    }

    @Override
    public String toString() {
        return label;
    }
}