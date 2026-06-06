package ru.finuniv.retailshield.dto;

/**
 * Данные формы анкеты-помощника на лендинге (выбор пакета по галочкам).
 * Поля соответствуют чек-боксам Экрана 1.
 */
public class PackageHelperForm {

    private boolean coverOwnLosses;
    private boolean coverThirdParty;
    private boolean coverReputation;
    private boolean coverRegulatory;
    private boolean coverAdditionalServices;
    private double annualRevenueRub;

    public boolean isCoverOwnLosses() { return coverOwnLosses; }
    public void setCoverOwnLosses(boolean v) { this.coverOwnLosses = v; }

    public boolean isCoverThirdParty() { return coverThirdParty; }
    public void setCoverThirdParty(boolean v) { this.coverThirdParty = v; }

    public boolean isCoverReputation() { return coverReputation; }
    public void setCoverReputation(boolean v) { this.coverReputation = v; }

    public boolean isCoverRegulatory() { return coverRegulatory; }
    public void setCoverRegulatory(boolean v) { this.coverRegulatory = v; }

    public boolean isCoverAdditionalServices() { return coverAdditionalServices; }
    public void setCoverAdditionalServices(boolean v) { this.coverAdditionalServices = v; }

    public double getAnnualRevenueRub() { return annualRevenueRub; }
    public void setAnnualRevenueRub(double v) { this.annualRevenueRub = v; }
}