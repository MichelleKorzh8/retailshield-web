package ru.finuniv.retailshield.model;

/**
 * Зависимость клиента от подрядчика или внешнего сервиса.
 * Используется при расчёте CascadeFactor (Блок 1, раздел 10.3):
 *     CascadeFactor = 1 + Σ (Dependency_i × Probability_i),
 *     ALE_cascade   = ALE × CascadeFactor.
 */
public class ContractorDependency {

    /** Название подрядчика или внешнего сервиса (для отображения в отчёте). */
    private final String name;

    /** Dependency — доля бизнес-процессов клиента, зависящих от подрядчика (0..1). */
    private final double dependency;

    /** Probability — вероятность компрометации подрядчика (0..1). */
    private final double probability;

    public ContractorDependency(String name, double dependency, double probability) {
        this.name = name;
        this.dependency = clamp(dependency);
        this.probability = clamp(probability);
    }

    public String getName() { return name; }
    public double getDependency() { return dependency; }
    public double getProbability() { return probability; }

    /** Вклад этого подрядчика в CascadeFactor: Dependency × Probability. */
    public double getContribution() {
        return dependency * probability;
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}