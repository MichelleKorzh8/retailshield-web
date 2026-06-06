package ru.finuniv.retailshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.finuniv.retailshield.dto.ContactForm;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.model.StopFactor;

import java.util.List;

/**
 * Экран отказа в страховании из-за нарушения стоп-фактора. Содержит причину
 * отказа, обоснование и форму обратной связи для индивидуального рассмотрения
 * через менеджера.
 */
@Controller
public class RefusalController {

    private final SessionAssessment session;

    @Autowired
    public RefusalController(SessionAssessment session) {
        this.session = session;
    }

    @GetMapping("/refusal")
    public String show(@RequestParam String id, Model model) {
        List<StopFactor> factors = session.getStopFactors();
        if (factors == null) {
            return "redirect:/stop-factors";
        }
        StopFactor violation = factors.stream()
                .filter(sf -> sf.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (violation == null) {
            return "redirect:/stop-factors";
        }

        model.addAttribute("violation", violation);
        model.addAttribute("contactForm", new ContactForm());
        model.addAttribute("companyName",
                session.getCompany() != null ? session.getCompany().getName() : "");
        model.addAttribute("pageTitle", "Отказ в стандартных условиях");
        return "refusal";
    }

    @PostMapping("/refusal/contact")
    public String submitContact(@org.springframework.web.bind.annotation.ModelAttribute
                                ContactForm contactForm, Model model) {
        // В прототипе просто логируем; в production отправлялось бы в CRM
        System.out.println("Запрос на индивидуальное рассмотрение: "
                + contactForm.getName() + " / " + contactForm.getPhone()
                + " / " + contactForm.getCompanyName() + " / " + contactForm.getComment());

        model.addAttribute("submitted", true);
        model.addAttribute("contactForm", contactForm);
        model.addAttribute("pageTitle", "Заявка отправлена");
        return "refusal-success";
    }
}