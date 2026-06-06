package ru.finuniv.retailshield.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Веса разделов анкеты для одной сценарной подмодели (Блок 1, раздел 9 и
 * Блок 2, глава 9). Шесть сценариев: ransomware, утечка данных, перерыв
 * в бизнесе, компрометация подрядчика, BEC, промышленный простой.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioWeights {

    @JsonProperty("А") private double weightA;
    @JsonProperty("Б") private double weightB;
    @JsonProperty("В") private double weightV;
    @JsonProperty("Г") private double weightG;
    @JsonProperty("Д") private double weightD;
    private String name;
    private String description;

    public double getWeightA() { return weightA; }
    public void setWeightA(double v) { this.weightA = v; }
    public double getWeightB() { return weightB; }
    public void setWeightB(double v) { this.weightB = v; }
    public double getWeightV() { return weightV; }
    public void setWeightV(double v) { this.weightV = v; }
    public double getWeightG() { return weightG; }
    public void setWeightG(double v) { this.weightG = v; }
    public double getWeightD() { return weightD; }
    public void setWeightD(double v) { this.weightD = v; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String s) { this.description = s; }

    public double weightFor(String sectionCode) {
        return switch (sectionCode) {
            case "А" -> weightA;
            case "Б" -> weightB;
            case "В" -> weightV;
            case "Г" -> weightG;
            case "Д" -> weightD;
            default  -> 0.0;
        };
    }
}