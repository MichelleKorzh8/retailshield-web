package ru.finuniv.retailshield.dto;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.Section;
import ru.finuniv.retailshield.model.StopFactor;

import java.util.List;

/**
 * Состояние мастера оценки в рамках одной HTTP-сессии пользователя.
 *
 * Spring создаёт по экземпляру на каждого посетителя сайта и автоматически
 * вкладывает в нужные контроллеры через @Autowired. Когда пользователь
 * переходит между экранами, данные сохраняются здесь, а не теряются.
 *
 * Для production-стенда сессии живут в памяти приложения; этого достаточно
 * для конкурсного прототипа. При деплое на хостинг Render с одним инстансом
 * приложения сессии работают корректно из коробки.
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionAssessment {

    private Company company;
    private List<Section> universalSections;
    private Section retailSection;
    private List<StopFactor> stopFactors;
    private AssessmentResult result;
    private Double previousScoreTotal;

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public List<Section> getUniversalSections() { return universalSections; }
    public void setUniversalSections(List<Section> s) { this.universalSections = s; }

    public Section getRetailSection() { return retailSection; }
    public void setRetailSection(Section retailSection) { this.retailSection = retailSection; }

    public List<StopFactor> getStopFactors() { return stopFactors; }
    public void setStopFactors(List<StopFactor> s) { this.stopFactors = s; }

    public AssessmentResult getResult() { return result; }
    public void setResult(AssessmentResult result) { this.result = result; }

    public Double getPreviousScoreTotal() { return previousScoreTotal; }
    public void setPreviousScoreTotal(Double v) { this.previousScoreTotal = v; }

    public void reset() {
        this.company = null;
        this.universalSections = null;
        this.retailSection = null;
        this.stopFactors = null;
        this.result = null;
        this.previousScoreTotal = null;
    }
}