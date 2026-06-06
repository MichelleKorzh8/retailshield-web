package ru.finuniv.retailshield.data;

import ru.finuniv.retailshield.model.AnswerOption;
import ru.finuniv.retailshield.model.Industry;
import ru.finuniv.retailshield.model.Question;
import ru.finuniv.retailshield.model.Section;

import java.util.ArrayList;
import java.util.List;

/**
 * Содержание анкеты по методологии работы:
 *  — универсальная часть (разделы А-Д) для всех клиентов,
 *  — отраслевая часть (раздел Е) для трёх типов ритейла:
 *    сетевая розничная торговля, e-commerce, маркетплейсы.
 *
 * Используется конструктор AnswerOption.unconfirmed(...) для вариантов вида
 * «мера заявлена без подтверждения» — они срабатывают по таблице А.26.
 *
 * Поле manageabilityGroup у вопросов Г2, Г3, Г5, Д5 — это критические условия
 * восстановимости и мониторинга для расчёта компонента «управляемость» индекса
 * киберстрахуемости (Блок 1, раздел 10.1, правка 12).
 */
public final class QuestionnaireData {

    private QuestionnaireData() {
    }

    /**
     * Универсальная часть анкеты: разделы А, Б, В, Г, Д с весами
     * 0,10 / 0,30 / 0,15 / 0,20 / 0,25 (Блок 1, формула 7.2).
     * Возвращает свежие экземпляры — состояние выборов в session,
     * а не в статике.
     */
    public static List<Section> buildUniversalSections() {
        List<Section> sections = new ArrayList<>();
        sections.add(buildSectionA());
        sections.add(buildSectionB());
        sections.add(buildSectionV());
        sections.add(buildSectionG());
        sections.add(buildSectionD());
        return sections;
    }

    /**
     * Отраслевой раздел Е для всех трёх типов ритейла. Список вопросов
     * одинаков (одна и та же отрасль страхового продукта), но базовый TEF
     * у разных типов разный (определяется в Industry).
     */
    public static Section buildRetailSection(Industry industry) {
        if (industry == null || !industry.isRetailFamily()) {
            return null;
        }
        List<Question> qs = new ArrayList<>();

        qs.add(new Question("Е1",
                "Изоляция POS-терминалов и кассовых систем в отдельной сетевой зоне",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("Полная изоляция, отдельный VLAN/сегмент", 0),
                        AnswerOption.unconfirmed("Заявлена изоляция, но без сегментации на уровне маршрутизации", 4),
                        AnswerOption.confirmed("Частичная изоляция", 5),
                        AnswerOption.confirmed("Изоляции нет, POS в общей сети", 10)
                )));

        qs.add(new Question("Е2",
                "Соответствие требованиям PCI DSS при приёме банковских карт",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Не применимо, карты не принимаются", 0),
                        AnswerOption.confirmed("Сертификация PCI DSS пройдена", 0),
                        AnswerOption.unconfirmed("Соответствие на уровне SAQ, без сертификации", 4),
                        AnswerOption.confirmed("Требования не соблюдаются", 10)
                )));

        qs.add(new Question("Е3",
                "Защита и резервирование складских систем WMS",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Полная защита, бэкапы, отдельный сегмент", 0),
                        AnswerOption.confirmed("Защита частичная", 5),
                        AnswerOption.confirmed("Защита отсутствует", 10)
                )));

        qs.add(new Question("Е4",
                "Защита платёжных шлюзов и интеграций с банками-эквайерами",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("Полная (TLS, мониторинг, фрод-детекция)", 0),
                        AnswerOption.confirmed("Базовая (только TLS)", 5),
                        AnswerOption.confirmed("Недостаточная", 10)
                )));

        qs.add(new Question("Е5",
                "Защита личных кабинетов покупателей от credential stuffing "
                        + "(captcha, ограничение попыток, мониторинг подозрительных входов)",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Полный набор мер", 0),
                        AnswerOption.confirmed("Часть мер реализована", 5),
                        AnswerOption.confirmed("Защиты практически нет", 10)
                )));

        qs.add(new Question("Е6",
                "Защита публичного API маркетплейса/мобильного приложения "
                        + "(rate limiting, аутентификация, мониторинг)",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Не применимо, нет публичного API", 0),
                        AnswerOption.confirmed("Полная защита", 0),
                        AnswerOption.confirmed("Базовая", 5),
                        AnswerOption.confirmed("Слабая или отсутствует", 10)
                )));

        qs.add(new Question("Е7",
                "Управление доступом продавцов на платформе "
                        + "(MFA, разграничение прав, аудит действий)",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Не применимо", 0),
                        AnswerOption.confirmed("Полный контроль", 0),
                        AnswerOption.confirmed("Частичный", 5),
                        AnswerOption.confirmed("Контроля нет", 10)
                )));

        qs.add(new Question("Е8",
                "Фрод-мониторинг транзакций в пиковые периоды (распродажи, праздники)",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Круглосуточный автоматический", 0),
                        AnswerOption.confirmed("В рабочее время", 5),
                        AnswerOption.confirmed("Не проводится", 10)
                )));

        return new Section("Е",
                "Отраслевая часть: ритейл и маркетплейсы (" + industry.getLabel() + ")",
                0.0, qs);
    }

    // ============================================================
    // ===================== РАЗДЕЛ А =============================
    // ============================================================
    private static Section buildSectionA() {
        List<Question> qs = new ArrayList<>();

        qs.add(new Question("А1", "Размер компании по выручке", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Микро (до 50 млн ₽)", 0),
                        AnswerOption.confirmed("Малый (50-500 млн ₽)", 2),
                        AnswerOption.confirmed("Средний (500 млн – 2 млрд ₽)", 4),
                        AnswerOption.confirmed("Крупный (2-5 млрд ₽)", 6),
                        AnswerOption.confirmed("Очень крупный (свыше 5 млрд ₽)", 8)
                )));

        qs.add(new Question("А2", "Статус оператора персональных данных по ФЗ-152", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Не оператор ПДн", 0),
                        AnswerOption.confirmed("Оператор, уведомление подано, политика опубликована", 2),
                        AnswerOption.confirmed("Оператор, документы не приведены в порядок", 8)
                )));

        qs.add(new Question("А3", "Объём обрабатываемых субъектов ПДн", 1.0, false,
                List.of(
                        AnswerOption.confirmed("До 10 тыс.", 0),
                        AnswerOption.confirmed("10-100 тыс.", 3),
                        AnswerOption.confirmed("100 тыс. – 1 млн", 6),
                        AnswerOption.confirmed("Свыше 1 млн", 9)
                )));

        qs.add(new Question("А4", "Категория обрабатываемых ПДн", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Только общедоступные/иные", 0),
                        AnswerOption.confirmed("Иные, требующие защиты", 3),
                        AnswerOption.confirmed("Биометрические или специальные", 8)
                )));

        qs.add(new Question("А5", "Статус субъекта критической информационной инфраструктуры (ФЗ-187)",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("Не относится к КИИ", 0),
                        AnswerOption.confirmed("Субъект КИИ, категорирование проведено", 2),
                        AnswerOption.confirmed("Субъект КИИ, категорирование не проведено", 10)
                )));

        qs.add(new Question("А6", "Введён ли режим коммерческой тайны", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Не применимо", 0),
                        AnswerOption.confirmed("Введён", 0),
                        AnswerOption.confirmed("Не введён, хотя есть охраняемая информация", 5)
                )));

        qs.add(new Question("А7", "Проверки регуляторов (РКН, ФСТЭК, ФСБ) за 24 месяца", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Не было", 0),
                        AnswerOption.confirmed("Были, без нарушений", 3),
                        AnswerOption.confirmed("Были, с нарушениями", 8)
                )));

        qs.add(new Question("А8", "Были ли подтверждённые киберинциденты за 12 месяцев", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Не было", 0),
                        AnswerOption.confirmed("Были", 5)
                )));

        qs.add(new Question("А9", "Действующий или продлеваемый полис киберстрахования", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Полис действует, продлевается", -5),
                        AnswerOption.confirmed("В процессе оформления", 0),
                        AnswerOption.confirmed("Никогда не было", 0)
                )));

        return new Section("А", "Общие сведения и регуляторный профиль", 0.10, qs);
    }

    // ============================================================
    // ===================== РАЗДЕЛ Б =============================
    // ============================================================
    private static Section buildSectionB() {
        List<Question> qs = new ArrayList<>();

        qs.add(new Question("Б1",
                "Сегментация сети между веб-инфраструктурой, корпоративной и платёжной средами",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("Полная", 0),
                        AnswerOption.unconfirmed("Заявлена, но без верификации сканированием", 3),
                        AnswerOption.confirmed("Частичная", 5),
                        AnswerOption.confirmed("Отсутствует", 10)
                )));

        qs.add(new Question("Б2", "EDR или антивирус с централизованным управлением на всех узлах",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("EDR на всех узлах", 0),
                        AnswerOption.confirmed("Антивирус с централизованным управлением", 3),
                        AnswerOption.confirmed("Отсутствует", 10)
                )));

        qs.add(new Question("Б3", "Сканирование уязвимостей и их устранение", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Ежеквартально, уязвимости устраняются", 0),
                        AnswerOption.confirmed("Сканирование без устранения", 5),
                        AnswerOption.confirmed("Не проводится", 10)
                )));

        qs.add(new Question("Б4", "Межсетевые экраны и WAF для веб-приложений", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Полное покрытие", 0),
                        AnswerOption.confirmed("Частичное", 5),
                        AnswerOption.confirmed("Отсутствует", 10)
                )));

        qs.add(new Question("Б5", "MFA для критических систем и удалённого доступа", 2.0, true,
                List.of(
                        AnswerOption.confirmed("Для всех пользователей", 0),
                        AnswerOption.confirmed("Только для администраторов", 5),
                        AnswerOption.confirmed("Не используется", 10)
                )));

        qs.add(new Question("Б6", "Шифрование критических данных в покое и в передаче", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Полное", 0),
                        AnswerOption.unconfirmed("Заявлено, но без подтверждения СКЗИ", 3),
                        AnswerOption.confirmed("Частичное", 5),
                        AnswerOption.confirmed("Не используется", 10)
                )));

        qs.add(new Question("Б7", "Применение сертифицированных СЗИ для систем с ПДн или КИИ",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Не применимо", 0),
                        AnswerOption.confirmed("Используются", 0),
                        AnswerOption.confirmed("Не используются, хотя требуется", 10)
                )));

        qs.add(new Question("Б8", "Применение СКЗИ, сертифицированных ФСБ России", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Не применимо", 0),
                        AnswerOption.confirmed("Используются", 0),
                        AnswerOption.confirmed("Не используются, хотя требуется", 8)
                )));

        qs.add(new Question("Б9", "Своевременность применения критических обновлений", 1.0, false,
                List.of(
                        AnswerOption.confirmed("В течение 30 дней", 0),
                        AnswerOption.confirmed("30-90 дней", 5),
                        AnswerOption.confirmed("Свыше 90 дней", 10)
                )));

        return new Section("Б", "Техническая защита инфраструктуры", 0.30, qs);
    }

    // ============================================================
    // ===================== РАЗДЕЛ В =============================
    // ============================================================
    private static Section buildSectionV() {
        List<Question> qs = new ArrayList<>();

        qs.add(new Question("В1", "Назначен ответственный за информационную безопасность", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Назначен и реально работает", 0),
                        AnswerOption.confirmed("Назначен формально", 5),
                        AnswerOption.confirmed("Не назначен", 10)
                )));

        qs.add(new Question("В2", "Регулярные фишинг-тесты сотрудников", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Ежеквартально", 0),
                        AnswerOption.confirmed("Один раз в год", 5),
                        AnswerOption.confirmed("Не проводятся", 10)
                )));

        qs.add(new Question("В3", "Доля сотрудников, поддающихся фишингу (по последнему тесту)",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Менее 5%", 0),
                        AnswerOption.confirmed("5-15%", 3),
                        AnswerOption.confirmed("Свыше 15%", 8),
                        AnswerOption.confirmed("Тестов не проводилось", 10)
                )));

        qs.add(new Question("В4", "Обучение по кибергигиене и подписание правил ИБ", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Ежегодное обучение и NDA", 0),
                        AnswerOption.confirmed("Только при найме", 5),
                        AnswerOption.confirmed("Не проводится", 10)
                )));

        qs.add(new Question("В5", "Реализация принципа минимальных привилегий", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Реализован", 0),
                        AnswerOption.unconfirmed("Заявлен, но без верификации", 3),
                        AnswerOption.confirmed("Частично", 5),
                        AnswerOption.confirmed("Не реализован", 10)
                )));

        return new Section("В", "Безопасность персонала и процессы", 0.15, qs);
    }

    // ============================================================
    // ===================== РАЗДЕЛ Г =============================
    // ============================================================
    private static Section buildSectionG() {
        List<Question> qs = new ArrayList<>();

        qs.add(new Question("Г1", "Регулярность резервного копирования критических систем",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("Ежедневно", 0),
                        AnswerOption.confirmed("Еженедельно", 5),
                        AnswerOption.confirmed("Нерегулярно", 10)
                )));

        qs.add(new Question("Г2",
                "Изолированное хранение резервных копий (правило 3-2-1)",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("Полное соблюдение", 0),
                        AnswerOption.confirmed("Частичное", 5),
                        AnswerOption.confirmed("Отсутствует", 10)
                ),
                "RECOVERABILITY"));

        qs.add(new Question("Г3",
                "Тестирование восстановления из резервных копий",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Ежеквартально, успешно", 0),
                        AnswerOption.confirmed("Проводилось, были проблемы", 5),
                        AnswerOption.confirmed("Не проводилось", 10)
                ),
                "RECOVERABILITY"));

        qs.add(new Question("Г4", "План непрерывности и восстановления (DRP/BCP)", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Есть и регулярно тестируется", 0),
                        AnswerOption.unconfirmed("Существует как формальный документ без тестов", 3),
                        AnswerOption.confirmed("Существует как формальный документ", 5),
                        AnswerOption.confirmed("Отсутствует", 10)
                )));

        qs.add(new Question("Г5",
                "Целевой показатель времени восстановления (RTO) критических сервисов",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Менее 4 часов", 0),
                        AnswerOption.confirmed("4-24 часа", 3),
                        AnswerOption.confirmed("Свыше 24 часов", 6),
                        AnswerOption.confirmed("Не определён", 10)
                ),
                "RECOVERABILITY"));

        return new Section("Г", "Резервное копирование и устойчивость", 0.20, qs);
    }

    // ============================================================
    // ===================== РАЗДЕЛ Д =============================
    // ============================================================
    private static Section buildSectionD() {
        List<Question> qs = new ArrayList<>();

        qs.add(new Question("Д1",
                "Использование облачных сервисов с учётом требований ФЗ-152 о локализации ПДн",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Не используются", 0),
                        AnswerOption.confirmed("Используются с соблюдением локализации", 3),
                        AnswerOption.confirmed("Используются без проверки требований", 8)
                )));

        qs.add(new Question("Д2",
                "Требования ИБ в договорах с подрядчиками "
                        + "(право аудита, MFA, бэкапы, уведомление об инцидентах за 24 ч)",
                2.0, true,
                List.of(
                        AnswerOption.confirmed("Закреплены с правом аудита", 0),
                        AnswerOption.unconfirmed("Закреплены формально", 5),
                        AnswerOption.confirmed("Не закреплены", 10)
                )));

        qs.add(new Question("Д3", "Проверка фактической защищённости подрядчиков", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Регулярная", 0),
                        AnswerOption.confirmed("По запросу", 5),
                        AnswerOption.confirmed("Не проводится", 10)
                )));

        qs.add(new Question("Д4", "Наличие публичного сайта с онлайн-сервисами", 1.0, false,
                List.of(
                        AnswerOption.confirmed("Нет", 0),
                        AnswerOption.confirmed("Только информационный", 3),
                        AnswerOption.confirmed("Полнофункциональный e-commerce", 8)
                )));

        qs.add(new Question("Д5",
                "Использование SOC (собственного или внешнего)",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Круглосуточный SOC", 0),
                        AnswerOption.confirmed("В рабочее время", 3),
                        AnswerOption.confirmed("Не используется", 10)
                ),
                "MONITORING"));

        qs.add(new Question("Д6", "Разработана ли модель угроз согласно методике ФСТЭК 2021 года",
                1.0, false,
                List.of(
                        AnswerOption.confirmed("Разработана и актуализируется", 0),
                        AnswerOption.unconfirmed("Разработана формально без актуализации", 3),
                        AnswerOption.confirmed("Разработана формально", 5),
                        AnswerOption.confirmed("Не разработана", 10)
                )));

        return new Section("Д", "Внешние риски, подрядчики и комплаенс", 0.25, qs);
    }
}