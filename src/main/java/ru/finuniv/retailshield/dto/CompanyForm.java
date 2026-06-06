package ru.finuniv.retailshield.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.Industry;
import ru.finuniv.retailshield.model.LossHistory;
import ru.finuniv.retailshield.model.PackageType;

/**
 * Данные формы экрана «Сведения о компании».
 * После валидации преобразуется в Company методом toCompany().
 */
public class CompanyForm {

    @NotBlank(message = "Укажите название компании")
    private String name;

    @NotNull(message = "Укажите годовую выручку")
    @Min(value = 1, message = "Выручка должна быть положительным числом")
    private Double annualRevenueRub;

    @NotNull(message = "Выберите отрасль")
    private Industry industry;

    @NotNull(message = "Выберите историю инцидентов")
    private LossHistory lossHistory = LossHistory.NO_HISTORY;

    @NotNull(message = "Выберите тарифный пакет")
    private PackageType packageType;

    @NotNull(message = "Укажите желаемую страховую сумму")
    @Min(value = 1, message = "Страховая сумма должна быть положительной")
    private Double insuredSum;

    private boolean continuousMonitoring;
    private boolean complianceAudit;
    private boolean phishingTraining;
    private boolean hotline24x7;
    private boolean tabletopExercises;
    private Company.Exposure exposure = Company.Exposure.NONE;
    private boolean hasExternalAudit;

    // === Геттеры/сеттеры ===

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getAnnualRevenueRub() { return annualRevenueRub; }
    public void setAnnualRevenueRub(Double v) { this.annualRevenueRub = v; }

    public Industry getIndustry() { return industry; }
    public void setIndustry(Industry industry) { this.industry = industry; }

    public LossHistory getLossHistory() { return lossHistory; }
    public void setLossHistory(LossHistory lossHistory) { this.lossHistory = lossHistory; }

    public PackageType getPackageType() { return packageType; }
    public void setPackageType(PackageType packageType) { this.packageType = packageType; }

    public Double getInsuredSum() { return insuredSum; }
    public void setInsuredSum(Double insuredSum) { this.insuredSum = insuredSum; }

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

    public Company.Exposure getExposure() { return exposure; }
    public void setExposure(Company.Exposure exposure) { this.exposure = exposure; }

    public boolean isHasExternalAudit() { return hasExternalAudit; }
    public void setHasExternalAudit(boolean v) { this.hasExternalAudit = v; }

    /** Преобразует форму в Company. */
    public Company toCompany() {
        Company c = new Company();
        c.setName(name);
        c.setAnnualRevenueRub(annualRevenueRub == null ? 0 : annualRevenueRub);
        c.setIndustry(industry);
        c.setLossHistory(lossHistory);
        c.setPackageType(packageType);
        c.setInsuredSum(insuredSum == null ? 0 : insuredSum);
        c.setContinuousMonitoring(continuousMonitoring);
        c.setComplianceAudit(complianceAudit);
        c.setExposure(exposure);
        c.setHasExternalAudit(hasExternalAudit);
        c.setPhishingTraining(phishingTraining);
        c.setHotline24x7(hotline24x7);
        c.setTabletopExercises(tabletopExercises);
        return c;
    }
}