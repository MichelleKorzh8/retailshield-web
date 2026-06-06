package ru.finuniv.retailshield.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Итоговый результат оценки. Заполняется AssessmentService (оркестратор)
 * через вызов сервисов скоринга, FAIR, премии, сценарного анализа,
 * индекса киберстрахуемости.
 *
 * Используется на экране результата и в HTML-отчёте, который клиент
 * скачивает в конце работы мастера.
 */
public class AssessmentResult {

    // === Профиль клиента (копия для отчёта) ===
    private Company company;

    // === Скоры по разделам и итог ===
    private final Map<String, Double> sectionScores = new LinkedHashMap<>();
    private double rawScoreTotal;        // до K_неопр
    private double kUncertainty = 1.0;   // K_неопр в диапазоне 1,00..1,20
    private double scoreTotal;           // итоговый, после K_неопр и min(10)
    private RiskClass riskClass;

    // === Стоп-факторы (новый список из 7) ===
    private boolean stopFactorTriggered;
    private String stopFactorReason;

    // === Список сработавших правил согласованности (А.26) ===
    private final List<String> consistencyWarnings = new ArrayList<>();

    // === Отраслевые коэффициенты ===
    private double industryFactor;
    private double sizeFactor;

    // === FAIR + CascadeFactor (правка 4) ===
    private double tef;
    private double vulnerability;
    private double lef;
    private double lossMagnitude;
    private double ale;
    private double cascadeFactor = 1.0;
    private double aleCascade;          // ALE × CascadeFactor

    // === BI (правка 9) ===
    private double businessInterruptionLoss;
    private boolean biUsedIndustryEstimate;

    // === Страховая премия (правка 10 с K_внеш и K_аудита) ===
    private double baseRate;
    private double kScore;
    private double kHistory;
    private double kService;
    private double kExposure = 1.0;
    private double kAudit    = 1.0;
    private double annualPremium;
    private double effectiveRate;       // премия / страховая сумма

    // === Loss Ratio (правка 11) ===
    private double lossRatio;

    // === Индекс киберстрахуемости (CII) ===
    private double cii;
    private double ciiMeasurability;
    private double ciiPredictability;
    private double ciiManageability;
    private double ciiInsurability;
    private boolean ciiBelowThreshold;  // true → индивидуальное рассмотрение

    // === Индекс эволюции риска (для повторных оценок) ===
    private Double previousScoreTotal;  // null для первой оценки
    private Double deltaScore;          // null для первой оценки

    // === Сценарный анализ ===
    private Map<String, ScenarioOutcome> scenarios = new LinkedHashMap<>();

    // === Внутренний класс для результата по одному сценарию ===
    public static class ScenarioOutcome {
        public String code;
        public String name;
        public String description;
        public double scenarioScore;
        public double vulnerability;
        public double totalAle;
        public List<ThreatContribution> threats = new ArrayList<>();
    }

    public static class ThreatContribution {
        public String code;
        public String name;
        public double ale;
    }

    // === Геттеры/сеттеры ===

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Map<String, Double> getSectionScores() { return sectionScores; }

    public double getRawScoreTotal() { return rawScoreTotal; }
    public void setRawScoreTotal(double v) { this.rawScoreTotal = v; }

    public double getKUncertainty() { return kUncertainty; }
    public void setKUncertainty(double v) { this.kUncertainty = v; }

    public double getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(double v) { this.scoreTotal = v; }

    public RiskClass getRiskClass() { return riskClass; }
    public void setRiskClass(RiskClass rc) { this.riskClass = rc; }

    public boolean isStopFactorTriggered() { return stopFactorTriggered; }
    public void setStopFactorTriggered(boolean v) { this.stopFactorTriggered = v; }

    public String getStopFactorReason() { return stopFactorReason; }
    public void setStopFactorReason(String s) { this.stopFactorReason = s; }

    public List<String> getConsistencyWarnings() { return consistencyWarnings; }

    public double getIndustryFactor() { return industryFactor; }
    public void setIndustryFactor(double v) { this.industryFactor = v; }

    public double getSizeFactor() { return sizeFactor; }
    public void setSizeFactor(double v) { this.sizeFactor = v; }

    public double getTef() { return tef; }
    public void setTef(double v) { this.tef = v; }

    public double getVulnerability() { return vulnerability; }
    public void setVulnerability(double v) { this.vulnerability = v; }

    public double getLef() { return lef; }
    public void setLef(double v) { this.lef = v; }

    public double getLossMagnitude() { return lossMagnitude; }
    public void setLossMagnitude(double v) { this.lossMagnitude = v; }

    public double getAle() { return ale; }
    public void setAle(double v) { this.ale = v; }

    public double getCascadeFactor() { return cascadeFactor; }
    public void setCascadeFactor(double v) { this.cascadeFactor = v; }

    public double getAleCascade() { return aleCascade; }
    public void setAleCascade(double v) { this.aleCascade = v; }

    public double getBusinessInterruptionLoss() { return businessInterruptionLoss; }
    public void setBusinessInterruptionLoss(double v) { this.businessInterruptionLoss = v; }

    public boolean isBiUsedIndustryEstimate() { return biUsedIndustryEstimate; }
    public void setBiUsedIndustryEstimate(boolean v) { this.biUsedIndustryEstimate = v; }

    public double getBaseRate() { return baseRate; }
    public void setBaseRate(double v) { this.baseRate = v; }

    public double getKScore() { return kScore; }
    public void setKScore(double v) { this.kScore = v; }

    public double getKHistory() { return kHistory; }
    public void setKHistory(double v) { this.kHistory = v; }

    public double getKService() { return kService; }
    public void setKService(double v) { this.kService = v; }

    public double getKExposure() { return kExposure; }
    public void setKExposure(double v) { this.kExposure = v; }

    public double getKAudit() { return kAudit; }
    public void setKAudit(double v) { this.kAudit = v; }

    public double getAnnualPremium() { return annualPremium; }
    public void setAnnualPremium(double v) { this.annualPremium = v; }

    public double getEffectiveRate() { return effectiveRate; }
    public void setEffectiveRate(double v) { this.effectiveRate = v; }

    public double getLossRatio() { return lossRatio; }
    public void setLossRatio(double v) { this.lossRatio = v; }

    public double getCii() { return cii; }
    public void setCii(double v) { this.cii = v; }

    public double getCiiMeasurability() { return ciiMeasurability; }
    public void setCiiMeasurability(double v) { this.ciiMeasurability = v; }

    public double getCiiPredictability() { return ciiPredictability; }
    public void setCiiPredictability(double v) { this.ciiPredictability = v; }

    public double getCiiManageability() { return ciiManageability; }
    public void setCiiManageability(double v) { this.ciiManageability = v; }

    public double getCiiInsurability() { return ciiInsurability; }
    public void setCiiInsurability(double v) { this.ciiInsurability = v; }

    public boolean isCiiBelowThreshold() { return ciiBelowThreshold; }
    public void setCiiBelowThreshold(boolean v) { this.ciiBelowThreshold = v; }

    public Double getPreviousScoreTotal() { return previousScoreTotal; }
    public void setPreviousScoreTotal(Double v) { this.previousScoreTotal = v; }

    public Double getDeltaScore() { return deltaScore; }
    public void setDeltaScore(Double v) { this.deltaScore = v; }

    public Map<String, ScenarioOutcome> getScenarios() { return scenarios; }
    public void setScenarios(Map<String, ScenarioOutcome> m) { this.scenarios = m; }
}