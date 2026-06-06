package ru.finuniv.retailshield.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.finuniv.retailshield.dto.CompanyForm;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.Industry;
import ru.finuniv.retailshield.model.LossHistory;
import ru.finuniv.retailshield.model.PackageType;

/**
 * Экран ввода данных компании-страхователя.
 *
 * GET  /company  — форма ввода названия, выручки, отрасли, страховой суммы и пр.
 * POST /company  — валидация и сохранение в сессии, переход к чек-листу.
 */
@Controller
public class CompanyController {

    private final SessionAssessment session;

    @Autowired
    public CompanyController(SessionAssessment session) {
        this.session = session;
    }

    @GetMapping("/company")
    public String form(Model model) {
        // Если пакет не выбран — отправляем обратно на /packages
        if (session.getCompany() == null || session.getCompany().getPackageType() == null) {
            return "redirect:/packages";
        }

        CompanyForm form = new CompanyForm();
        form.setPackageType(session.getCompany().getPackageType());

        // Если сессия уже содержит данные — подставляем для редактирования
        Company existing = session.getCompany();
        if (existing.getName() != null) {
            form.setName(existing.getName());
            form.setAnnualRevenueRub(existing.getAnnualRevenueRub());
            form.setIndustry(existing.getIndustry());
            form.setLossHistory(existing.getLossHistory());
            form.setInsuredSum(existing.getInsuredSum());
            form.setContinuousMonitoring(existing.isContinuousMonitoring());
            form.setComplianceAudit(existing.isComplianceAudit());
            form.setExposure(existing.getExposure());
            form.setHasExternalAudit(existing.isHasExternalAudit());
        }

        prepareModel(model, form);
        return "company";
    }

    @PostMapping("/company")
    public String submit(@Valid @ModelAttribute("form") CompanyForm form,
                         BindingResult binding, Model model) {

        if (binding.hasErrors()) {
            prepareModel(model, form);
            return "company";
        }

        Company company = form.toCompany();
        // Сохраняем выбранный ранее пакет (на случай если форма его не передала)
        if (company.getPackageType() == null && session.getCompany() != null) {
            company.setPackageType(session.getCompany().getPackageType());
        }
        session.setCompany(company);

        return "redirect:/stop-factors";
    }

    private void prepareModel(Model model, CompanyForm form) {
        model.addAttribute("form", form);
        model.addAttribute("industries", Industry.values());
        model.addAttribute("lossHistories", LossHistory.values());
        model.addAttribute("exposures", Company.Exposure.values());
        model.addAttribute("packageLabel",
                form.getPackageType() != null ? form.getPackageType().getLabel() : "");
        model.addAttribute("pageTitle", "Данные компании");
    }
}