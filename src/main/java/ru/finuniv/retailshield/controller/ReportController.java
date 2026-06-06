package ru.finuniv.retailshield.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.finuniv.retailshield.dto.SessionAssessment;
import ru.finuniv.retailshield.model.AnswerOption;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.Question;
import ru.finuniv.retailshield.model.Section;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Скачивание HTML-отчёта по оценке (правка 1 из файла правок после защиты).
 *
 * Отчёт включает:
 *  — данные компании и выбранный пакет,
 *  — все ответы на все вопросы анкеты А-Е,
 *  — итоговые результаты: класс риска, скоры, CII, FAIR, BI, премию, сценарии,
 *  — сработавшие правила согласованности.
 *
 * GET /report — формирует HTML-файл и отдаёт как attachment для скачивания.
 */
@Controller
public class ReportController {

    private final SessionAssessment session;
    private final DecimalFormat money;
    private final DecimalFormat score;

    @Autowired
    public ReportController(SessionAssessment session) {
        this.session = session;
        DecimalFormatSymbols ru = new DecimalFormatSymbols(new Locale("ru", "RU"));
        ru.setDecimalSeparator(',');
        ru.setGroupingSeparator(' ');
        this.money = new DecimalFormat("#,##0", ru);
        this.score = new DecimalFormat("0.00", ru);
    }

    @GetMapping("/report")
    public void download(HttpServletResponse response) throws Exception {
        AssessmentResult result = session.getResult();
        if (result == null) {
            response.sendRedirect("/");
            return;
        }

        String filename = buildFileName(result.getCompany().getName());
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encoded);

        try (PrintWriter w = response.getWriter()) {
            writeReport(w, result);
        }
    }

    private String buildFileName(String companyName) {
        String safe = (companyName == null ? "report" : companyName)
                .replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (safe.isEmpty()) safe = "report";
        return "Отчёт_" + safe + ".html";
    }

    private void writeReport(PrintWriter w, AssessmentResult r) {
        w.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">");
        w.println("<title>Отчёт по оценке: " + esc(r.getCompany().getName()) + "</title>");
        w.println(styles());
        w.println("</head><body><main>");

        writeHeader(w, r);
        writeResultsBlock(w, r);
        writeAnswersBlock(w, r);
        writeFooter(w);

        w.println("</main></body></html>");
    }

    private void writeHeader(PrintWriter w, AssessmentResult r) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        w.println("<h1>Отчёт по оценке киберрисков</h1>");
        w.println("<div class=\"meta\">Компания: " + esc(r.getCompany().getName())
                + " &middot; Пакет: " + esc(r.getCompany().getPackageType().getLabel())
                + " &middot; Дата формирования: " + now + "</div>");
    }

    private void writeResultsBlock(PrintWriter w, AssessmentResult r) {

        // === Класс риска и Score_total ===
        w.println("<section class=\"card\"><h2>Класс риска и скор</h2>");
        String cls = "risk" + (r.getRiskClass().isAcceptable() ? "" : " risk-critical");
        w.println("<div class=\"" + cls + "\">Класс риска: "
                + esc(r.getRiskClass().getLabel()) + "</div>");
        row(w, "Итоговый Score_total", score.format(r.getScoreTotal()) + " / 10");
        if (r.getKUncertainty() > 1.0) {
            row(w, "Коэффициент неопределённости K_неопр", score.format(r.getKUncertainty()));
        }
        row(w, "Решение андеррайтера", esc(r.getRiskClass().getUnderwriterDecision()));
        if (r.isStopFactorTriggered()) {
            w.println("<div class=\"warn\">Сработал стоп-фактор: "
                    + esc(r.getStopFactorReason()) + "</div>");
        }
        w.println("</section>");

        // === Индекс киберстрахуемости ===
        w.println("<section class=\"card\"><h2>Индекс киберстрахуемости (CII)</h2>");
        w.println("<div class=\"risk\">" + score.format(r.getCii()) + " / 10</div>");
        row(w, "Измеримость", score.format(r.getCiiMeasurability()));
        row(w, "Прогнозируемость", score.format(r.getCiiPredictability()));
        row(w, "Управляемость", score.format(r.getCiiManageability()));
        row(w, "Тарифицируемость", score.format(r.getCiiInsurability()));
        if (r.isCiiBelowThreshold()) {
            w.println("<div class=\"warn\">Индекс ниже порога 5,0 — требуется "
                    + "индивидуальное рассмотрение.</div>");
        }
        w.println("</section>");

        // === Сведения о компании ===
        w.println("<section class=\"card\"><h2>Сведения о компании</h2>");
        row(w, "Название", esc(r.getCompany().getName()));
        row(w, "Отрасль", esc(r.getCompany().getIndustry().getLabel()));
        row(w, "Годовая выручка", money.format(r.getCompany().getAnnualRevenueRub()) + " ₽");
        row(w, "Размер бизнеса", esc(r.getCompany().getSizeLabel()));
        row(w, "История инцидентов", esc(r.getCompany().getLossHistory().getLabel()));
        row(w, "Страховая сумма", money.format(r.getCompany().getInsuredSum()) + " ₽");
        row(w, "Тарифный пакет", esc(r.getCompany().getPackageType().getLabel()));
        row(w, "Внешняя экспозиция (OSINT)", esc(r.getCompany().getExposure().getLabel()));
        row(w, "Внешний аудит ИБ",
                r.getCompany().isHasExternalAudit() ? "есть" : "нет");
        w.println("</section>");

        // === Скоры по разделам ===
        w.println("<section class=\"card\"><h2>Скоры по разделам анкеты</h2>");
        for (Map.Entry<String, Double> e : r.getSectionScores().entrySet()) {
            row(w, "Раздел " + e.getKey(), score.format(e.getValue()));
        }
        w.println("</section>");

        // === Правила согласованности ===
        if (!r.getConsistencyWarnings().isEmpty()) {
            w.println("<section class=\"card\"><h2>Правила согласованности (А.26)</h2><ul>");
            for (String warn : r.getConsistencyWarnings()) {
                w.println("<li>" + esc(warn) + "</li>");
            }
            w.println("</ul></section>");
        }

        // === FAIR ===
        w.println("<section class=\"card\"><h2>FAIR: количественная оценка ущерба</h2>");
        row(w, "TEF", score.format(r.getTef()));
        row(w, "V (уязвимость)", score.format(r.getVulnerability()));
        row(w, "LEF", score.format(r.getLef()));
        row(w, "LM", money.format(r.getLossMagnitude()) + " ₽");
        row(w, "ALE", money.format(r.getAle()) + " ₽");
        if (r.getCascadeFactor() > 1.0) {
            row(w, "CascadeFactor", score.format(r.getCascadeFactor()));
            row(w, "ALE_cascade", money.format(r.getAleCascade()) + " ₽");
        }
        w.println("</section>");

        // === BI ===
        w.println("<section class=\"card\"><h2>Убыток от перерыва деятельности (BI)</h2>");
        row(w, "BI (годовой)", money.format(r.getBusinessInterruptionLoss()) + " ₽");
        if (r.isBiUsedIndustryEstimate()) {
            w.println("<div class=\"hint\">Использован отраслевой ориентир.</div>");
        }
        w.println("</section>");

        // === Премия ===
        w.println("<section class=\"card\"><h2>Расчёт страховой премии</h2>");
        if (!r.getRiskClass().isAcceptable()) {
            w.println("<div class=\"warn\">Премия не рассчитывается — клиент в классе Critical.</div>");
        } else {
            row(w, "Страховая сумма S", money.format(r.getCompany().getInsuredSum()) + " ₽");
            row(w, "Базовая ставка T", score.format(r.getBaseRate() * 100) + "%");
            row(w, "K_отрасли", score.format(r.getIndustryFactor()));
            row(w, "K_скора", score.format(r.getKScore()));
            row(w, "K_размера", score.format(r.getSizeFactor()));
            row(w, "K_истории", score.format(r.getKHistory()));
            row(w, "K_сервиса", score.format(r.getKService()));
            row(w, "K_внеш", score.format(r.getKExposure()));
            row(w, "K_аудита", score.format(r.getKAudit()));
            w.println("<div class=\"hint\">P = S × T × K_отрасли × K_скора × K_размера × "
                    + "K_истории × K_сервиса × K_внеш × K_аудита</div>");
            w.println("<div class=\"risk\">Годовая премия: "
                    + money.format(r.getAnnualPremium()) + " ₽</div>");
            row(w, "Эффективная ставка", score.format(r.getEffectiveRate() * 100) + "%");
            row(w, "Loss Ratio", score.format(r.getLossRatio()));
        }
        w.println("</section>");

        // === Сценарный анализ ===
        if (!r.getScenarios().isEmpty()) {
            w.println("<section class=\"card\"><h2>Сценарный анализ</h2>");
            for (var sc : r.getScenarios().values()) {
                w.println("<div class=\"scenario\">");
                w.println("<div class=\"scenario-name\">" + esc(sc.name) + "</div>");
                w.println("<div class=\"hint\">" + esc(sc.description) + "</div>");
                row(w, "Сценарный риск-скор", score.format(sc.scenarioScore));
                row(w, "ALE по сценарию", money.format(sc.totalAle) + " ₽");
                w.println("</div>");
            }
            w.println("</section>");
        }
    }

    private void writeAnswersBlock(PrintWriter w, AssessmentResult r) {
        List<Section> all = new ArrayList<>(session.getUniversalSections());
        if (session.getRetailSection() != null) {
            all.add(session.getRetailSection());
        }

        w.println("<section class=\"card\"><h2>Ответы на вопросы анкеты</h2>");
        w.println("<p class=\"hint\">Полный перечень вопросов с выбранными вариантами ответов "
                + "и баллами по шкале 0–10.</p>");

        for (Section s : all) {
            w.println("<h3>Раздел " + esc(s.getCode()) + ". " + esc(s.getTitle()) + "</h3>");
            w.println("<table class=\"answers\">");
            w.println("<thead><tr><th>ID</th><th>Вопрос</th><th>Ответ</th><th>Балл</th></tr></thead>");
            w.println("<tbody>");
            for (Question q : s.getQuestions()) {
                String answerText = "—";
                String scoreText = "—";
                if (q.isAnswered()) {
                    AnswerOption opt = q.getOptions().get(q.getSelectedIndex());
                    answerText = esc(opt.getText());
                    if (opt.isUncertain()) {
                        answerText += " <span class=\"uncertain\">(без подтверждения)</span>";
                    }
                    scoreText = score.format(opt.getScore());
                }
                w.println("<tr>");
                w.println("<td>" + esc(q.getId())
                        + (q.isCritical() ? " <span class=\"crit\">★</span>" : "")
                        + "</td>");
                w.println("<td>" + esc(q.getText()) + "</td>");
                w.println("<td>" + answerText + "</td>");
                w.println("<td>" + scoreText + "</td>");
                w.println("</tr>");
            }
            w.println("</tbody></table>");
        }
        w.println("</section>");
    }

    private void writeFooter(PrintWriter w) {
        w.println("<div class=\"footer\">Прототип конкурсной работы команды «КиберКаска», "
                + "Финансовый университет, 2026.</div>");
    }

    private void row(PrintWriter w, String label, String value) {
        w.println("<div class=\"row\"><span class=\"l\">" + esc(label)
                + "</span><span class=\"v\">" + value + "</span></div>");
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String styles() {
        return """
                <style>
                  body { background:#f7f7f5; color:#1c1c1c;
                         font-family:"Segoe UI","Inter",Arial,sans-serif;
                         font-size:14px; line-height:1.55; margin:0; padding:24px; }
                  main { max-width:980px; margin:0 auto; }
                  h1 { font-size:24px; color:#111; margin:0 0 4px 0; }
                  h2 { font-size:18px; color:#111; margin:0 0 12px 0; }
                  h3 { font-size:15px; color:#111; margin:16px 0 6px 0; }
                  .meta { color:#555; font-size:13px; margin-bottom:20px; }
                  .card { background:#fff; border:1px solid #d8d8d6; border-radius:4px;
                          padding:18px 22px; margin-bottom:16px; }
                  .row { display:flex; justify-content:space-between;
                         padding:5px 0; border-bottom:1px solid #f0f0ee; }
                  .row:last-child { border-bottom:none; }
                  .l { color:#4a4a4a; }
                  .v { color:#1c1c1c; font-weight:700; }
                  .risk { font-size:20px; font-weight:700; margin:8px 0; }
                  .risk-critical { color:#8a1a1a; }
                  .warn { background:#fff5f0; border-left:3px solid #8a1a1a;
                          padding:10px 14px; margin:10px 0; color:#4a1a1a; }
                  .hint { color:#666; font-size:12.5px; }
                  .scenario { margin:14px 0; padding:10px 12px;
                              background:#f7f7f5; border-radius:4px; }
                  .scenario-name { font-weight:700; margin-bottom:4px; }
                  table.answers { width:100%; border-collapse:collapse;
                                  margin:6px 0 14px 0; font-size:13px; }
                  table.answers th, table.answers td {
                       border-bottom:1px solid #ececea; padding:7px 8px;
                       text-align:left; vertical-align:top; }
                  table.answers th { background:#f0f0ee; }
                  .crit { color:#8a1a1a; }
                  .uncertain { color:#8a1a1a; font-size:11px; }
                  .footer { color:#777; font-size:12px; text-align:center;
                            margin-top:24px; padding-top:12px; border-top:1px solid #d8d8d6; }
                </style>
                """;
    }
}