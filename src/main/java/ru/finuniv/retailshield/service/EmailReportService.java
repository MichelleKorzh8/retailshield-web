package ru.finuniv.retailshield.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.model.AnswerOption;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.Question;
import ru.finuniv.retailshield.model.Section;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Сервис автоматической отправки отчётов команде разработчиков по email.
 *
 * После прохождения клиентом анкеты ResultController вызывает этот сервис,
 * который собирает HTML-отчёт со всеми расчётами и ответами и отправляет
 * его на адрес, указанный в переменной окружения MAIL_TO.
 *
 * Параметры подключения и адрес получателя берутся из переменных
 * окружения через application.properties (никогда не хранятся в коде).
 *
 * Если отправка отключена (retailshield.report.enabled=false) или
 * параметры пусты — сервис молча игнорирует вызов, чтобы локальная
 * разработка без настроенного SMTP не падала.
 */
@Service
public class EmailReportService {

    private final JavaMailSender mailSender;
    private final String recipient;
    private final String senderUsername;
    private final boolean enabled;

    private final DecimalFormat money;
    private final DecimalFormat score;

    @Autowired
    public EmailReportService(JavaMailSender mailSender,
                              @Value("${retailshield.report.recipient:}") String recipient,
                              @Value("${spring.mail.username:}") String senderUsername,
                              @Value("${retailshield.report.enabled:true}") boolean enabled) {
        this.mailSender = mailSender;
        this.recipient = recipient;
        this.senderUsername = senderUsername;
        this.enabled = enabled;

        DecimalFormatSymbols ru = new DecimalFormatSymbols(new Locale("ru", "RU"));
        ru.setDecimalSeparator(',');
        ru.setGroupingSeparator(' ');
        this.money = new DecimalFormat("#,##0", ru);
        this.score = new DecimalFormat("0.00", ru);
    }

    /**
     * Отправляет отчёт об оценке клиенту-разработчику. Вызывается из
     * ResultController после успешного расчёта результатов.
     *
     * Не бросает исключений: если отправка не удалась, ошибка логируется
     * в консоль, но пользователю результаты показываются как обычно.
     */
    public void sendReport(AssessmentResult result, List<Section> allSections) {
        if (!enabled || recipient == null || recipient.isBlank()
                || senderUsername == null || senderUsername.isBlank()) {
            System.out.println("[EmailReportService] Отправка отключена или не настроена. Пропуск.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String companyName = result.getCompany().getName() == null
                    ? "Неизвестный клиент" : result.getCompany().getName();
            String subject = "Отчёт по оценке: " + companyName
                    + " (" + result.getRiskClass().getLabel() + ")";

            helper.setFrom(senderUsername);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(buildEmailBody(result), true);

            // Прикладываем полный отчёт как HTML-файл
            byte[] reportBytes = buildFullHtmlReport(result, allSections)
                    .getBytes(StandardCharsets.UTF_8);
            String attachmentName = "Отчёт_" + sanitizeFileName(companyName) + ".html";
            helper.addAttachment(attachmentName,
                    new ByteArrayResource(reportBytes), "text/html; charset=UTF-8");

            mailSender.send(message);
            System.out.println("[EmailReportService] Отчёт отправлен на " + recipient);
        } catch (Exception ex) {
            System.err.println("[EmailReportService] Ошибка отправки: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Короткое сводное письмо с ключевыми числами и приложенным полным отчётом. */
    private String buildEmailBody(AssessmentResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Новая оценка клиента</h2>");
        sb.append("<p><b>Дата:</b> ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .append("</p>");

        sb.append("<h3>Сведения о клиенте</h3><ul>");
        sb.append("<li>Компания: ").append(esc(r.getCompany().getName())).append("</li>");
        sb.append("<li>Отрасль: ").append(esc(r.getCompany().getIndustry().getLabel())).append("</li>");
        sb.append("<li>Годовая выручка: ").append(money.format(r.getCompany().getAnnualRevenueRub())).append(" ₽</li>");
        sb.append("<li>Размер: ").append(esc(r.getCompany().getSizeLabel())).append("</li>");
        sb.append("<li>Пакет: ").append(esc(r.getCompany().getPackageType().getLabel())).append("</li>");
        sb.append("<li>Страховая сумма: ").append(money.format(r.getCompany().getInsuredSum())).append(" ₽</li>");
        sb.append("</ul>");

        sb.append("<h3>Результаты</h3><ul>");
        sb.append("<li>Класс риска: <b>").append(esc(r.getRiskClass().getLabel())).append("</b></li>");
        sb.append("<li>Итоговый риск-скор: ").append(score.format(r.getScoreTotal())).append(" / 10</li>");
        sb.append("<li>Индекс киберстрахуемости: ").append(score.format(r.getCii())).append(" / 10</li>");
        if (r.getRiskClass().isAcceptable()) {
            sb.append("<li>Годовая премия: <b>").append(money.format(r.getAnnualPremium())).append(" ₽</b></li>");
            sb.append("<li>Loss Ratio: ").append(score.format(r.getLossRatio())).append("</li>");
        } else {
            sb.append("<li><b>Отказ в стандартных условиях</b></li>");
            if (r.isStopFactorTriggered()) {
                sb.append("<li>Причина: ").append(esc(r.getStopFactorReason())).append("</li>");
            }
        }
        sb.append("</ul>");

        sb.append("<p>Полный отчёт со всеми ответами клиента, "
                + "сценарным анализом и разбивкой по разделам приложен к этому письму.</p>");

        sb.append("<hr><p style='color:#777;font-size:12px;'>"
                + "Автоматическое уведомление от прототипа «Ритейл.Щит Актив». "
                + "Команда КиберКаска, Финансовый университет, 2026.</p>");
        return sb.toString();
    }

    /** Полный HTML-отчёт (та же логика, что в ReportController.writeReport). */
    private String buildFullHtmlReport(AssessmentResult r, List<Section> allSections) {
        StringBuilder w = new StringBuilder();
        w.append("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">");
        w.append("<title>Отчёт по оценке: ").append(esc(r.getCompany().getName())).append("</title>");
        w.append(styles()).append("</head><body><main>");

        // Шапка
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        w.append("<h1>Отчёт по оценке киберрисков</h1>");
        w.append("<div class=\"meta\">Компания: ").append(esc(r.getCompany().getName()))
                .append(" &middot; Пакет: ").append(esc(r.getCompany().getPackageType().getLabel()))
                .append(" &middot; Дата формирования: ").append(now).append("</div>");

        // Класс риска
        w.append("<section class=\"card\"><h2>Класс риска и скор</h2>");
        String cls = "risk" + (r.getRiskClass().isAcceptable() ? "" : " risk-critical");
        w.append("<div class=\"").append(cls).append("\">Класс риска: ")
                .append(esc(r.getRiskClass().getLabel())).append("</div>");
        row(w, "Итоговый Score_total", score.format(r.getScoreTotal()) + " / 10");
        if (r.getKUncertainty() > 1.0) {
            row(w, "Коэффициент неопределённости K_неопр", score.format(r.getKUncertainty()));
        }
        row(w, "Решение андеррайтера", esc(r.getRiskClass().getUnderwriterDecision()));
        if (r.isStopFactorTriggered()) {
            w.append("<div class=\"warn\">Сработал стоп-фактор: ")
                    .append(esc(r.getStopFactorReason())).append("</div>");
        }
        w.append("</section>");

        // Индекс киберстрахуемости
        w.append("<section class=\"card\"><h2>Индекс киберстрахуемости (CII)</h2>");
        w.append("<div class=\"risk\">").append(score.format(r.getCii())).append(" / 10</div>");
        row(w, "Измеримость", score.format(r.getCiiMeasurability()));
        row(w, "Прогнозируемость", score.format(r.getCiiPredictability()));
        row(w, "Управляемость", score.format(r.getCiiManageability()));
        row(w, "Тарифицируемость", score.format(r.getCiiInsurability()));
        w.append("</section>");

        // Сведения о компании
        w.append("<section class=\"card\"><h2>Сведения о компании</h2>");
        row(w, "Название", esc(r.getCompany().getName()));
        row(w, "Отрасль", esc(r.getCompany().getIndustry().getLabel()));
        row(w, "Годовая выручка", money.format(r.getCompany().getAnnualRevenueRub()) + " ₽");
        row(w, "Размер бизнеса", esc(r.getCompany().getSizeLabel()));
        row(w, "История инцидентов", esc(r.getCompany().getLossHistory().getLabel()));
        row(w, "Страховая сумма", money.format(r.getCompany().getInsuredSum()) + " ₽");
        row(w, "Тарифный пакет", esc(r.getCompany().getPackageType().getLabel()));
        row(w, "Внешняя экспозиция", esc(r.getCompany().getExposure().getLabel()));
        row(w, "Внешний аудит ИБ", r.getCompany().isHasExternalAudit() ? "есть" : "нет");
        w.append("</section>");

        // Скоры по разделам
        w.append("<section class=\"card\"><h2>Скоры по разделам анкеты</h2>");
        for (Map.Entry<String, Double> e : r.getSectionScores().entrySet()) {
            row(w, "Раздел " + e.getKey(), score.format(e.getValue()));
        }
        w.append("</section>");

        // FAIR
        w.append("<section class=\"card\"><h2>FAIR: количественная оценка ущерба</h2>");
        row(w, "TEF", score.format(r.getTef()));
        row(w, "V (уязвимость)", score.format(r.getVulnerability()));
        row(w, "LEF", score.format(r.getLef()));
        row(w, "LM", money.format(r.getLossMagnitude()) + " ₽");
        row(w, "ALE", money.format(r.getAle()) + " ₽");
        if (r.getCascadeFactor() > 1.0) {
            row(w, "CascadeFactor", score.format(r.getCascadeFactor()));
            row(w, "ALE_cascade", money.format(r.getAleCascade()) + " ₽");
        }
        w.append("</section>");

        // BI
        w.append("<section class=\"card\"><h2>Убыток от перерыва деятельности</h2>");
        row(w, "BI (годовой)", money.format(r.getBusinessInterruptionLoss()) + " ₽");
        if (r.isBiUsedIndustryEstimate()) {
            w.append("<div class=\"hint\">Использован отраслевой ориентир.</div>");
        }
        w.append("</section>");

        // Премия
        w.append("<section class=\"card\"><h2>Расчёт страховой премии</h2>");
        if (!r.getRiskClass().isAcceptable()) {
            w.append("<div class=\"warn\">Премия не рассчитывается — клиент в классе Critical.</div>");
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
            w.append("<div class=\"risk\">Годовая премия: ")
                    .append(money.format(r.getAnnualPremium())).append(" ₽</div>");
            row(w, "Эффективная ставка", score.format(r.getEffectiveRate() * 100) + "%");
            row(w, "Loss Ratio", score.format(r.getLossRatio()));
        }
        w.append("</section>");

        // Сценарный анализ
        if (r.getScenarios() != null && !r.getScenarios().isEmpty()) {
            w.append("<section class=\"card\"><h2>Сценарный анализ</h2>");
            for (var sc : r.getScenarios().values()) {
                w.append("<div class=\"scenario\">");
                w.append("<div class=\"scenario-name\">").append(esc(sc.name)).append("</div>");
                w.append("<div class=\"hint\">").append(esc(sc.description)).append("</div>");
                row(w, "Сценарный риск-скор", score.format(sc.scenarioScore));
                row(w, "ALE по сценарию", money.format(sc.totalAle) + " ₽");
                w.append("</div>");
            }
            w.append("</section>");
        }

        // Ответы на вопросы
        w.append("<section class=\"card\"><h2>Ответы на вопросы анкеты</h2>");
        for (Section s : allSections) {
            w.append("<h3>Раздел ").append(esc(s.getCode())).append(". ")
                    .append(esc(s.getTitle())).append("</h3>");
            w.append("<table class=\"answers\">");
            w.append("<thead><tr><th>ID</th><th>Вопрос</th><th>Ответ</th><th>Балл</th></tr></thead><tbody>");
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
                w.append("<tr>");
                w.append("<td>").append(esc(q.getId()))
                        .append(q.isCritical() ? " <span class=\"crit\">★</span>" : "")
                        .append("</td>");
                w.append("<td>").append(esc(q.getText())).append("</td>");
                w.append("<td>").append(answerText).append("</td>");
                w.append("<td>").append(scoreText).append("</td>");
                w.append("</tr>");
            }
            w.append("</tbody></table>");
        }
        w.append("</section>");

        w.append("</main></body></html>");
        return w.toString();
    }

    private void row(StringBuilder w, String label, String value) {
        w.append("<div class=\"row\"><span class=\"l\">").append(esc(label))
                .append("</span><span class=\"v\">").append(value).append("</span></div>");
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String sanitizeFileName(String s) {
        String safe = s.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return safe.isEmpty() ? "report" : safe;
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
                </style>
                """;
    }
}