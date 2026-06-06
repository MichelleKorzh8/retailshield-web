package ru.finuniv.retailshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.finuniv.retailshield.dto.ContactForm;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.service.AssessmentService;

/**
 * Финальный экран мастера. Запускает оценку через AssessmentService и
 * отображает все результаты: скоры по разделам, Score_total, K_неопр,
 * класс риска, индекс киберстрахуемости, FAIR + CascadeFactor, BI,
 * страховую премию со всеми коэффициентами, Loss Ratio, сценарный анализ.
 *
 * GET  /result            — выполняет расчёт и показывает страницу.
 * POST /result/contact    — принимает форму связи с менеджером.
 */
@Controller
public class ResultController {

    private final AssessmentService assessmentService;
    private final SessionAssessment session;

    @Autowired
    public ResultController(AssessmentService assessmentService,
                            SessionAssessment session) {
        this.assessmentService = assessmentService;
        this.session = session;
    }

    @GetMapping("/result")
    public String result(Model model) {
        if (session.getCompany() == null
                || session.getUniversalSections() == null
                || session.getStopFactors() == null) {
            return "redirect:/";
        }

        // Запускаем оценку, если ещё не запускалась в этой сессии
        if (session.getResult() == null) {
            AssessmentResult r = assessmentService.evaluate(
                    session.getCompany(),
                    session.getUniversalSections(),
                    session.getRetailSection(),
                    session.getStopFactors(),
                    session.getPreviousScoreTotal()
            );
            session.setResult(r);
        }

        AssessmentResult result = session.getResult();
        ContactForm preFilledContact = new ContactForm();
        preFilledContact.setCompanyName(result.getCompany().getName());

        model.addAttribute("result", result);
        model.addAttribute("contactForm", preFilledContact);
        model.addAttribute("pageTitle", "Результаты оценки");
        return "result";
    }

    @PostMapping("/result/contact")
    public String submitContact(@ModelAttribute ContactForm contactForm, Model model) {
        // В прототипе логируем, в production — в CRM страховщика
        System.out.println("Заявка с экрана результатов: "
                + contactForm.getName() + " / " + contactForm.getPhone()
                + " / " + contactForm.getCompanyName() + " / " + contactForm.getComment());

        model.addAttribute("contactForm", contactForm);
        model.addAttribute("pageTitle", "Заявка отправлена");
        return "result-contact-success";
    }
}