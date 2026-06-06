package ru.finuniv.retailshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.model.StopFactor;
import ru.finuniv.retailshield.service.AssessmentService;

import java.util.List;
import java.util.Map;

/**
 * Экран 3: чек-лист предварительного отбора (7 жёстких стоп-факторов
 * из обновлённой главы 12 Блока 1).
 *
 * GET  /stop-factors          — показывает чек-лист.
 * POST /stop-factors          — обрабатывает ответы. Если хотя бы один пункт
 *                                не соблюдается → переход на /refusal с обоснованием.
 *                                Если все семь соблюдаются → переход на /questionnaire.
 */
@Controller
public class StopFactorsController {

    private final AssessmentService assessmentService;
    private final SessionAssessment session;

    @Autowired
    public StopFactorsController(AssessmentService assessmentService,
                                 SessionAssessment session) {
        this.assessmentService = assessmentService;
        this.session = session;
    }

    @GetMapping("/stop-factors")
    public String show(Model model) {
        if (session.getCompany() == null || session.getCompany().getName() == null) {
            return "redirect:/company";
        }

        if (session.getStopFactors() == null) {
            session.setStopFactors(assessmentService.freshStopFactors());
        }

        model.addAttribute("stopFactors", session.getStopFactors());
        model.addAttribute("companyName", session.getCompany().getName());
        model.addAttribute("pageTitle", "Чек-лист предварительного отбора");
        return "stop-factors";
    }

    /**
     * Принимает Map: ключ — id стоп-фактора (S1, S2, ...), значение — "yes" или "no".
     * Spring соберёт эту карту автоматически из всех радио-кнопок формы.
     */
    @PostMapping("/stop-factors")
    public String submit(@RequestParam Map<String, String> answers, Model model) {
        List<StopFactor> factors = session.getStopFactors();
        if (factors == null) {
            return "redirect:/stop-factors";
        }

        // Проверяем, что ответ дан по каждому пункту
        boolean allAnswered = true;
        for (StopFactor sf : factors) {
            String ans = answers.get(sf.getId());
            if (ans == null || ans.isBlank()) {
                allAnswered = false;
            }
        }
        if (!allAnswered) {
            model.addAttribute("stopFactors", factors);
            model.addAttribute("companyName", session.getCompany().getName());
            model.addAttribute("error",
                    "Дайте ответ по каждому пункту чек-листа.");
            model.addAttribute("pageTitle", "Чек-лист предварительного отбора");
            return "stop-factors";
        }

        // Применяем ответы
        StopFactor firstViolation = null;
        for (StopFactor sf : factors) {
            boolean compliant = "yes".equalsIgnoreCase(answers.get(sf.getId()));
            sf.setCompliant(compliant);
            if (!compliant && firstViolation == null) {
                firstViolation = sf;
            }
        }

        // Если есть нарушение — переход на экран отказа
        if (firstViolation != null) {
            return "redirect:/refusal?id=" + firstViolation.getId();
        }

        // Иначе — переход к анкете
        return "redirect:/questionnaire";
    }
}