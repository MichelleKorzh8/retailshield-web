package ru.finuniv.retailshield.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Корень файла retail_risk_map.json. Карта применяется на этапе оценки клиентов
 * из ритейла (RETAIL_OFFLINE, RETAIL_ECOMMERCE, RETAIL_MARKETPLACE) для:
 *   — сценарного анализа (ScenarioAnalysisService),
 *   — построения HTML-дашборда (DashboardController),
 *   — отображения рекомендаций по покрытию в страховом продукте.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskMap {

    private String industry;
    private double industryFactor;
    private double tefBase;
    private String source;
    private List<Threat> threats;
    private Map<String, ScenarioWeights> scenarioWeights;

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public double getIndustryFactor() { return industryFactor; }
    public void setIndustryFactor(double v) { this.industryFactor = v; }

    public double getTefBase() { return tefBase; }
    public void setTefBase(double v) { this.tefBase = v; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<Threat> getThreats() { return threats; }
    public void setThreats(List<Threat> threats) { this.threats = threats; }

    public Map<String, ScenarioWeights> getScenarioWeights() { return scenarioWeights; }
    public void setScenarioWeights(Map<String, ScenarioWeights> m) { this.scenarioWeights = m; }
}