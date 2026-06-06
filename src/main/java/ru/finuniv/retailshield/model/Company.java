package ru.finuniv.retailshield.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Данные оцениваемой компании-страхователя. Заполняется по экранам мастера:
 * на лендинге выбирается пакет (PackageType), затем вводятся название, выручка,
 * отрасль, история инцидентов, страховая сумма, флаги сервисов.
 *
 * Поля для новых правок:
 *  - exposure: внешняя экспозиция (K_внеш по результатам OSINT),
 *  - hasExternalAudit: наличие независимого аудита ИБ (K_аудита),
 *  - dependencies: список подрядчиков для CascadeFactor (Блок 1 п. 10.3),
 *  - businessInterruption: данные для расчёта BI (Блок 3 п. 3.1).
 */
public class Company {

    /** Уровни внешней экспозиции (правка 10, K_внеш). */
    public enum Exposure {
        NONE("Утечек учётных данных не выявлено"),
        OLD("Единичные устаревшие утечки"),
        FRESH("Подтверждённые свежие утечки учётных данных");

        private final String label;
        Exposure(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    // === Основные данные ===
    private String name;
    private double annualRevenueRub;
    private Industry industry;
    private LossHistory lossHistory = LossHistory.NO_HISTORY;
    private PackageType packageType;
    private double insuredSum;

    // === Сервисные опции ===
    private boolean continuousMonitoring;   // K_сервиса = 0,85 для не-Лайт пакетов
    private boolean complianceAudit;        // K_сервиса = 0,90 для пакета Лайт
    /** Pre-incident: обучение сотрудников и регулярные фишинг-тесты (от партнёра). */
    private boolean phishingTraining;

    /** Реагирование: подключение круглосуточной горячей линии с SLA 1 час. */
    private boolean hotline24x7;

    /** Post-incident: tabletop exercises (ролевые учения) и пен-тест. */
    private boolean tabletopExercises;

    // === Новые поля для формул премии и расчётов ===
    private Exposure exposure = Exposure.NONE;
    private boolean hasExternalAudit;
    private final List<ContractorDependency> dependencies = new ArrayList<>();
    private BusinessInterruption businessInterruption;

    // === Геттеры/сеттеры ===

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAnnualRevenueRub() { return annualRevenueRub; }
    public void setAnnualRevenueRub(double v) { this.annualRevenueRub = v; }

    public Industry getIndustry() { return industry; }
    public void setIndustry(Industry industry) { this.industry = industry; }

    public LossHistory getLossHistory() { return lossHistory; }
    public void setLossHistory(LossHistory h) { this.lossHistory = h; }

    public PackageType getPackageType() { return packageType; }
    public void setPackageType(PackageType packageType) { this.packageType = packageType; }

    public double getInsuredSum() { return insuredSum; }
    public void setInsuredSum(double v) { this.insuredSum = v; }

    public boolean isContinuousMonitoring() { return continuousMonitoring; }
    public void setContinuousMonitoring(boolean v) { this.continuousMonitoring = v; }

    public boolean isComplianceAudit() { return complianceAudit; }
    public void setComplianceAudit(boolean v) { this.complianceAudit = v; }

    public boolean isPhishingTraining() { return phishingTraining; }
    public void setPhishingTraining(boolean v) { this.phishingTraining = v; }

    public boolean isHotline24x7() { return hotline24x7; }
    public void setHotline24x7(boolean v) { this.hotline24x7 = v; }

    public boolean isTabletopExercises() { return tabletopExercises; }
    public void setTabletopExercises(boolean v) { this.tabletopExercises = v; }

    public Exposure getExposure() { return exposure; }
    public void setExposure(Exposure exposure) { this.exposure = exposure; }

    public boolean isHasExternalAudit() { return hasExternalAudit; }
    public void setHasExternalAudit(boolean v) { this.hasExternalAudit = v; }

    public List<ContractorDependency> getDependencies() { return dependencies; }

    public BusinessInterruption getBusinessInterruption() { return businessInterruption; }
    public void setBusinessInterruption(BusinessInterruption b) {
        this.businessInterruption = b;
    }

    // === Производные характеристики ===

    /**
     * SizeFactor по таблице А.14 Блока 1.
     * 0,8 микро (до 50 млн) — 1,4 очень крупный (свыше 5 млрд).
     */
    public double getSizeFactor() {
        double r = annualRevenueRub;
        if (r < 50_000_000d)        return 0.8;
        if (r < 500_000_000d)       return 1.0;
        if (r < 2_000_000_000d)     return 1.2;
        if (r < 5_000_000_000d)     return 1.3;
        return 1.4;
    }

    public String getSizeLabel() {
        double r = annualRevenueRub;
        if (r < 50_000_000d)        return "Микробизнес";
        if (r < 500_000_000d)       return "Малый бизнес";
        if (r < 2_000_000_000d)     return "Средний бизнес";
        if (r < 5_000_000_000d)     return "Крупный бизнес";
        return "Очень крупный бизнес";
    }
}