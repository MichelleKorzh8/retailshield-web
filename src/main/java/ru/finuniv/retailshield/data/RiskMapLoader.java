package ru.finuniv.retailshield.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.finuniv.retailshield.model.RiskMap;

import java.io.InputStream;

/**
 * Загружает retail_risk_map.json из ресурсов проекта при старте Spring-контекста.
 * Кэшируется: повторные вызовы getMap() возвращают тот же объект.
 *
 * Используется ScenarioAnalysisService, FairService (для базового TEF)
 * и DashboardController при формировании дашборда карты рисков.
 */
@Component
public class RiskMapLoader {

    private RiskMap cached;

    public synchronized RiskMap getMap() {
        if (cached == null) {
            try (InputStream in = new ClassPathResource("retail_risk_map.json").getInputStream()) {
                cached = new ObjectMapper().readValue(in, RiskMap.class);
            } catch (Exception ex) {
                throw new IllegalStateException(
                        "Не удалось загрузить retail_risk_map.json: " + ex.getMessage(), ex);
            }
        }
        return cached;
    }
}