package ru.finuniv.retailshield.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Отраслевая угроза карты рисков ритейла (Блок 2, таблица В.3).
 * Восемь угроз с количественной оценкой по FAIR (Блок 2, глава 4).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Threat {

    private String code;
    private String name;
    private String description;
    private String frequencyCategory;
    private double tef;
    private double lossMin;
    private double lossMax;
    private List<String> factorGroups;
    private String scenario;
    private String coverageRecommendation;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrequencyCategory() { return frequencyCategory; }
    public void setFrequencyCategory(String s) { this.frequencyCategory = s; }

    public double getTef() { return tef; }
    public void setTef(double tef) { this.tef = tef; }

    public double getLossMin() { return lossMin; }
    public void setLossMin(double v) { this.lossMin = v; }

    public double getLossMax() { return lossMax; }
    public void setLossMax(double v) { this.lossMax = v; }

    public List<String> getFactorGroups() { return factorGroups; }
    public void setFactorGroups(List<String> g) { this.factorGroups = g; }

    public String getScenario() { return scenario; }
    public void setScenario(String s) { this.scenario = s; }

    public String getCoverageRecommendation() { return coverageRecommendation; }
    public void setCoverageRecommendation(String s) { this.coverageRecommendation = s; }

    /** Среднее значение диапазона ущерба — используется как LM в расчёте ALE. */
    public double getLossAverage() {
        return (lossMin + lossMax) / 2.0;
    }
}