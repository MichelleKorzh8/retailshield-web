package ru.finuniv.retailshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.finuniv.retailshield.data.PackageData;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.model.PackageType;

/**
 * Экран 1: выбор тарифного пакета. Показывает четыре пакета с подробным
 * описанием. По клику «Выбрать» пакет сохраняется в сессии и пользователь
 * переходит к экрану ввода данных компании.
 *
 * GET /packages              — список пакетов с подсветкой рекомендованного.
 * GET /packages/select/{p}   — выбор пакета и переход к данным компании.
 */
@Controller
public class PackagesController {

    private final SessionAssessment session;

    @Autowired
    public PackagesController(SessionAssessment session) {
        this.session = session;
    }

    @GetMapping("/packages")
    public String packages(Model model) {
        model.addAttribute("packages", PackageData.getAll());
        model.addAttribute("pageTitle", "Тарифные пакеты — выбор");
        return "packages";
    }

    @GetMapping("/packages/select/{packageType}")
    public String selectPackage(@org.springframework.web.bind.annotation.PathVariable
                                String packageType) {
        // Запоминаем выбор пакета: создадим временный Company, который заполнится дальше
        try {
            PackageType pt = PackageType.valueOf(packageType);
            // создаём пустой Company с проставленным пакетом, чтобы CompanyForm
            // на следующем экране открывался с уже выбранным пакетом
            ru.finuniv.retailshield.model.Company c =
                    session.getCompany() != null ? session.getCompany()
                            : new ru.finuniv.retailshield.model.Company();
            c.setPackageType(pt);
            session.setCompany(c);
        } catch (IllegalArgumentException ignored) {
            // некорректное имя пакета — просто игнорируем
        }
        return "redirect:/company";
    }
}