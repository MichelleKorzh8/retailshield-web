package ru.finuniv.retailshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.finuniv.retailshield.data.PackageData;
import ru.finuniv.retailshield.dto.PackageHelperForm;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.service.PackageRecommenderService;
import ru.finuniv.retailshield.service.PackageRecommenderService.Recommendation;
import ru.finuniv.retailshield.service.PackageRecommenderService.Request;

/**
 * Лендинг (Экран 0) и анкета-помощник выбора пакета.
 *
 * GET  /            — главная страница: что за продукт, преимущества, тарифы, кейсы.
 * GET  /helper      — экран анкеты-помощника (галочки «что страховать»).
 * POST /helper      — обработка ответов помощника и редирект на /packages
 *                     с подсветкой рекомендованного пакета.
 */
@Controller
public class LandingController {

    private final PackageRecommenderService recommenderService;
    private final SessionAssessment session;

    @Autowired
    public LandingController(PackageRecommenderService recommenderService,
                             SessionAssessment session) {
        this.recommenderService = recommenderService;
        this.session = session;
    }

    @GetMapping("/")
    public String landing(Model model) {
        // На лендинге показываем 4 карточки пакетов как тизер
        model.addAttribute("packages", PackageData.getAll());
        model.addAttribute("pageTitle", "Ритейл.Щит Актив — киберстрахование для ритейла");
        return "landing";
    }

    @GetMapping("/helper")
    public String helperForm(Model model) {
        model.addAttribute("form", new PackageHelperForm());
        model.addAttribute("pageTitle", "Анкета-помощник — подобрать тариф");
        return "package-helper";
    }

    @PostMapping("/helper")
    public String helperSubmit(PackageHelperForm form, Model model) {
        Request req = new Request();
        req.coverOwnLosses = form.isCoverOwnLosses();
        req.coverThirdParty = form.isCoverThirdParty();
        req.coverReputation = form.isCoverReputation();
        req.coverRegulatory = form.isCoverRegulatory();
        req.coverAdditionalServices = form.isCoverAdditionalServices();
        req.annualRevenueRub = form.getAnnualRevenueRub();

        Recommendation rec = recommenderService.recommend(req);

        // Передаём рекомендацию через redirect-параметр, а не сессию,
        // чтобы пользователь мог свободно вернуться и переотметить галочки.
        return "redirect:/packages?recommended=" + rec.recommended.name()
                + "&rationale=" + java.net.URLEncoder.encode(rec.rationale,
                java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Сбросить состояние мастера и начать заново. */
    @GetMapping("/reset")
    public String reset() {
        session.reset();
        return "redirect:/";
    }
    /** Сбросить состояние и сразу перейти к выбору пакета. */
    @GetMapping("/start")
    public String start() {
        session.reset();
        return "redirect:/packages";
    }
}