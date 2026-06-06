package ru.finuniv.retailshield.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Правила логической согласованности ответов анкеты (Блок 1, раздел 6.1,
 * таблица А.26). Реализация правок 2 и 13.
 *
 * Каждое правило срабатывает, когда в ответах одновременно присутствуют
 * указанные пары вопрос+вариант (триггер). Последствие срабатывания
 * определяется уровнем правила:
 *   HARD_BLOCK    — комбинация логически невозможна, отклоняется на валидации;
 *   CONFIRMATION  — запрашивается подтверждение пользователя;
 *   UNCERTAINTY   — заявленная мера остаётся, но добавляется флаг K_неопр.
 *
 * Дельта на K_неопр приплюсовывается к 1,00 за каждое сработавшее правило
 * уровня UNCERTAINTY и обрезается по диапазону [K_UNCERTAINTY_MIN; K_UNCERTAINTY_MAX].
 */
public final class ConsistencyRules {

    private ConsistencyRules() {
    }

    public enum Level {
        HARD_BLOCK,
        CONFIRMATION,
        UNCERTAINTY
    }

    /** Триггер правила: вопрос с одним из перечисленных индексов выбран как ответ. */
    public record Trigger(String questionId, Set<Integer> selectedOptionIndexes) {
    }

    /** Само правило. */
    public record Rule(
            String id,
            String description,
            Level level,
            List<Trigger> triggers,
            double uncertaintyDelta
    ) {
        public Rule(String id, String description, Level level,
                    List<Trigger> triggers) {
            this(id, description, level, triggers, 0.05);
        }
    }

    public static List<Rule> getAll() {
        List<Rule> rules = new ArrayList<>();

        // === Жёсткие блокировки ===

        rules.add(new Rule("R1",
                "MFA на критических системах не используется (Б5), но удалённый "
                        + "доступ открыт с MFA — невозможная комбинация.",
                Level.HARD_BLOCK,
                List.of(
                        new Trigger("Б5", Set.of(2))   // не используется
                )));

        rules.add(new Rule("R2",
                "EDR/антивирус отсутствует (Б2), но непрерывный мониторинг "
                        + "(сервисная опция) при этом отмечен как подключённый — "
                        + "технически невозможно.",
                Level.HARD_BLOCK,
                List.of(
                        new Trigger("Б2", Set.of(2))   // отсутствует
                )));

        // === Запрос подтверждения ===

        rules.add(new Rule("R3",
                "Заявлено ежедневное резервное копирование (Г1), но изолированное "
                        + "хранение копий отсутствует (Г2). Уточнить, действительно ли "
                        + "копии хранятся в одной среде с production.",
                Level.CONFIRMATION,
                List.of(
                        new Trigger("Г1", Set.of(0)),  // ежедневно
                        new Trigger("Г2", Set.of(2))   // изоляция отсутствует
                )));

        rules.add(new Rule("R4",
                "Заявлен план непрерывности (Г4) и RTO менее 4 часов (Г5), "
                        + "но тестирование восстановления не проводится (Г3). "
                        + "Уточнить статус.",
                Level.CONFIRMATION,
                List.of(
                        new Trigger("Г4", Set.of(0)),
                        new Trigger("Г5", Set.of(0)),
                        new Trigger("Г3", Set.of(2))
                )));

        // === Повышение K_неопр ===

        rules.add(new Rule("R5",
                "Заявлено наличие MFA для всех пользователей (Б5), но обучение "
                        + "по кибергигиене не проводится (В4). Заявленная защита "
                        + "переоценивает реальную устойчивость — повышение K_неопр.",
                Level.UNCERTAINTY,
                List.of(
                        new Trigger("Б5", Set.of(0)),
                        new Trigger("В4", Set.of(2))
                ),
                0.05));

        rules.add(new Rule("R6",
                "Заявлена полная сегментация сети (Б1), но отсутствует "
                        + "сканирование уязвимостей (Б3) — сегментация без верификации "
                        + "не подтверждается.",
                Level.UNCERTAINTY,
                List.of(
                        new Trigger("Б1", Set.of(0)),
                        new Trigger("Б3", Set.of(2))
                ),
                0.05));

        rules.add(new Rule("R7",
                "Использование SOC заявлено (Д5), но обновления безопасности "
                        + "применяются с задержкой свыше 90 дней (Б9) — типовая работа "
                        + "SOC включает контроль патч-менеджмента.",
                Level.UNCERTAINTY,
                List.of(
                        new Trigger("Д5", Set.of(0, 1)),
                        new Trigger("Б9", Set.of(2))
                ),
                0.05));

        rules.add(new Rule("R8",
                "Требования ИБ закреплены в договорах с подрядчиками (Д2), "
                        + "но фактическая проверка защищённости подрядчиков не проводится (Д3) — "
                        + "договорные обязательства без верификации недостаточны.",
                Level.UNCERTAINTY,
                List.of(
                        new Trigger("Д2", Set.of(0)),
                        new Trigger("Д3", Set.of(2))
                ),
                0.05));

        rules.add(new Rule("R9",
                "Заявлено шифрование критических данных (Б6), но СКЗИ "
                        + "не используется при наличии требования (Б8) — реализация "
                        + "шифрования не подтверждена сертифицированными средствами.",
                Level.UNCERTAINTY,
                List.of(
                        new Trigger("Б6", Set.of(0)),
                        new Trigger("Б8", Set.of(2))
                ),
                0.05));

        rules.add(new Rule("R10",
                "Заявлен принцип минимальных привилегий (В5), но фишинг-тесты "
                        + "не проводятся (В2) — реализация принципа без поведенческой "
                        + "верификации остаётся декларативной.",
                Level.UNCERTAINTY,
                List.of(
                        new Trigger("В5", Set.of(0)),
                        new Trigger("В2", Set.of(2))
                ),
                0.05));

        return rules;
    }
}