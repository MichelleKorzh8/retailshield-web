package ru.finuniv.retailshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.finuniv.retailshield.data.QuestionnaireData;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.Question;
import ru.finuniv.retailshield.model.Section;
import ru.finuniv.retailshield.service.AssessmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Универсальный контроллер анкеты. Показывает разделы А-Е по очереди:
 *
 * GET  /questionnaire             — перенаправляет на первый раздел.
 * GET  /questionnaire/{index}     — показывает раздел с индексом 0..N-1
 *                                   (0=А, 1=Б, 2=В, 3=Г, 4=Д, 5=Е для ритейла).
 * POST /questionnaire/{index}     — сохраняет ответы и переходит к следующему разделу
 *                                   либо к экрану результатов на последнем разделе.
 */
@Controller
public class QuestionnaireController {

    private final AssessmentService assessmentService;
    private final SessionAssessment session;

    @Autowired
    public QuestionnaireController(AssessmentService assessmentService,
                                   SessionAssessment session) {
        this.assessmentService = assessmentService;
        this.session = session;
    }

    @GetMapping("/questionnaire")
    public String start() {
        return "redirect:/questionnaire/0";
    }

    @GetMapping("/questionnaire/{index}")
    public String show(@PathVariable int index, Model model) {
        // Проверяем, что компания введена
        Company company = session.getCompany();
        if (company == null || company.getName() == null) {
            return "redirect:/company";
        }

        // При первом заходе инициализируем разделы
        if (session.getUniversalSections() == null) {
            session.setUniversalSections(QuestionnaireData.buildUniversalSections());
        }
        if (company.getIndustry() != null && company.getIndustry().isRetailFamily()
                && session.getRetailSection() == null) {
            session.setRetailSection(
                    QuestionnaireData.buildRetailSection(company.getIndustry()));
        }

        List<Section> allSections = collectAllSections();

        // Защита от выхода за границы
        if (index < 0 || index >= allSections.size()) {
            return "redirect:/result";
        }

        Section current = allSections.get(index);

        model.addAttribute("section", current);
        model.addAttribute("sectionIndex", index);
        model.addAttribute("totalSections", allSections.size());
        model.addAttribute("isLast", index == allSections.size() - 1);
        model.addAttribute("pageTitle",
                "Раздел " + current.getCode() + ". " + current.getTitle());

        return "questionnaire";
    }

    @PostMapping("/questionnaire/{index}")
    public String submit(@PathVariable int index,
                         @RequestParam Map<String, String> answers,
                         Model model) {

        List<Section> allSections = collectAllSections();
        if (index < 0 || index >= allSections.size()) {
            return "redirect:/result";
        }

        Section current = allSections.get(index);

        // Проверка, что на все вопросы дан ответ
        for (Question q : current.getQuestions()) {
            String value = answers.get(q.getId());
            if (value == null || value.isBlank()) {
                model.addAttribute("section", current);
                model.addAttribute("sectionIndex", index);
                model.addAttribute("totalSections", allSections.size());
                model.addAttribute("isLast", index == allSections.size() - 1);
                model.addAttribute("error",
                        "Дайте ответ на все вопросы раздела перед переходом дальше.");
                model.addAttribute("pageTitle",
                        "Раздел " + current.getCode() + ". " + current.getTitle());
                return "questionnaire";
            }
        }

        // Сохраняем ответы
        for (Question q : current.getQuestions()) {
            try {
                int chosen = Integer.parseInt(answers.get(q.getId()));
                q.setSelectedIndex(chosen);
            } catch (NumberFormatException ignored) {
                // некорректный индекс — пропускаем
            }
        }

        // Если это последний раздел — переходим к расчётам
        if (index == allSections.size() - 1) {
            return "redirect:/result";
        }
        return "redirect:/questionnaire/" + (index + 1);
    }

    private List<Section> collectAllSections() {
        List<Section> all = new ArrayList<>(session.getUniversalSections());
        if (session.getRetailSection() != null) {
            all.add(session.getRetailSection());
        }
        return all;
    }
}